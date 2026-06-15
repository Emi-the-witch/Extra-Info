package view.sett.ui.subject;

import game.GAME;
import game.battle.div.Div;
import game.tourism.TOURISM;
import init.constant.Config;
import init.race.appearence.RPortrait;
import init.settings.S;
import init.type.CRIME_PUNISHMENTS;
import init.type.CRIME_PUNISHMENTS.PUNISHMENT;
import init.type.HCLASSES;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.types.prisoner.AIModule_Prisoner;
import settlement.entity.humanoid.ai.types.tourist.AIModule_Tourist;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import util.text.DicTime;
import world.army.AD;

import static java.lang.Math.round;

class UISubjectHoverer {

	private Induvidual indu;
	private Humanoid hum;

	private static CharSequence ¤¤Sentenced = "Sentenced to be:";
	private static CharSequence ¤¤ClickToChange = "Click to change punishment.";
	private static CharSequence ¤¤JudgedNo = "Pleads innocence. Wants to try case in court.";
	private static CharSequence ¤¤Judged = "Has been found guilty in a court.";
	private static CharSequence ¤¤Soldier = "Serving in your armies abroad.";
	private static CharSequence ¤¤SoldierDiv = "Enlisted in the division: {0}, in the army {1}.";
	private static CharSequence ¤¤SoldierReturn = "Is currently returning to the capitol, will arrive in home in {0} days.";
	private static CharSequence ¤¤yearsOld = "{0} years of age";
	private static CharSequence ¤¤Attraction = "Attraction";
	private static CharSequence ¤¤Service = "Service";
	private static CharSequence ¤¤none = "---";

	static {
		D.ts(UISubjectHoverer.class);
	}

	private GuiSection s = new GuiSection();

	public UISubjectHoverer() {

		s.addRightC(8, new GStat() {

			@Override
			public void update(GText text) {
				text.lablify();
				text.clear().add(STATS.APPEARANCE().name(indu));
				text.setMaxWidth(300);
				text.setMultipleLines(false);
			}
		}.increase());

		s.add(new GStat() {

			@Override
			public void update(GText text) {
				text.add(indu.race().info.namePosessive);
				text.s().add(hum == null ? HTYPES.SOLDIER().name :  hum.title());
				if (hum == null) {
					text.add(',').add(¤¤yearsOld);
					text.insert(0, (int)STATS.POP().age.years.getD(indu));
				}
				CharSequence extra = null;
				HTYPE t = indu.hType();
				if (t == HTYPES.SLAVE())
					extra = indu.clas().name;
				else if (t == HTYPES.PRISONER()) {

					if (STATS.LAW().prisonerType.get(indu).cl == HCLASSES.SLAVE()) {
						extra = Str.TMP.clear().s().add('(').add(HCLASSES.SLAVE().name).add(')');
					}
				}else if (t.parent() !=  t) {
					extra = Str.TMP.clear().add(STATS.POP().age.years.getD(indu), 1).s().add(DicTime.¤¤Years);
				}

				if (extra != null)
					text.s().add('(').add(extra).add(')');

			}
		}, 0, s.body().y2()+2);

		s.addDown(2, new GStat() {

			@Override
			public void update(GText text) {

				if (hum == null)
					text.add(¤¤Soldier);
				else
					hum.ai().getOccupation(hum, text);

			}
		});

		s.addRelBody(8, DIR.W, new SPRITE() {

			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			}

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				STATS.APPEARANCE().portraitRender(r, indu, X1, Y1, 2);
			}

			@Override
			public int width() {
				return RPortrait.P_WIDTH*2;
			}

			@Override
			public int height() {
				return RPortrait.P_HEIGHT*2;
			}
		});

		s.body().setWidth(550);
	}

	void hover(Humanoid h, GBox text) {
		if (h == null)
			return;
		this.hum = h;
		this.indu = h.indu();





		if (h.indu().hostile() && !S.get().developer) {
			text.error(HTYPES.ENEMY().name);
			return;
		}

		text.add(s);
		text.NL();
		//////////////////////////////////////////////////#!#
		// use BioLines.java for most of these gets.
		// Add more stuff to the hover-over UI for individuals

		CharSequence output;

		// Age Display
		if (h.indu() != null){
			output = "Age: " + (int) round(STATS.POP().age.years.getD(h.indu()));
			text.add(GFORMAT.text(text.text(), output));
			text.NL();
		}

		// Religion display
		if (STATS.RELIGION().getter.get(h.indu()) != null){
			output = "Religion: " + STATS.RELIGION().getter.get(h.indu()).religion.info.name;
			text.add(GFORMAT.text(text.text(), output));
			text.NL();
		}

		// Division display (if valid)
		if (STATS.BATTLE().DIV.get(h) != null){
			if (STATS.BATTLE().DIV.get(h).info != null) {
				output = "Division: " + STATS.BATTLE().DIV.get(h).info.name();
				text.add(GFORMAT.text(text.text(), output));
				text.NL();
			}
		}

		// Workplace display
		if (STATS.WORK().EMPLOYED.get(h) != null){
			text.add(GFORMAT.text(text.text(), "Works at " + STATS.WORK().EMPLOYED.get(h).name()));
			text.NL();
		}

		// Homeless display (say nothing if they have a home)
		if ( !STATS.HOME().GETTER.has(h) ){
			text.add(GFORMAT.text(text.text(), "Homeless!"));
			text.NL();
		}

		// Gender
//		if ( STATS.APPEARANCE().gender.get(h.indu()) == 1 ){
//			text.add(GFORMAT.text(text.text(), "Female"));
//			text.NL();
//		}
//		if ( STATS.APPEARANCE().gender.get(h.indu()) == 0 ){
//			text.add(GFORMAT.text(text.text(), "Male"));
//			text.NL();
//		}

		// Education
//		text.add(GFORMAT.text(text.text(), (int) (round( STATS.EDUCATION().total(h.indu())*100))+ "% Educated"));
//		text.NL();

		// Indoctrination

		//////////////////////////////////////////////////#!#
		if (SProblem.problem(h) != null) {
			text.add(text.text().errorify().add(SProblem.problem(h)));
			text.NL();
		}else if (SProblem.warning(h) != null) {
			text.add(text.text().warnify().add(SProblem.warning(h)));
			text.NL();
		}

		if (h.indu().hType() == HTYPES.PRISONER()) {
			text.text(¤¤Sentenced);
			PUNISHMENT p = AIModule_Prisoner.punishment(h, h.ai());
			text.textLL(p.name);
			if (p == CRIME_PUNISHMENTS.PRISON()) {
				GText t = text.text();
				t.add('(');
				DicTime.setDays(t, AIModule_Prisoner.DATA().prisonTimeLeft.get(h.ai()));
				t.add(')');
				text.add(t);
			}

			text.NL(4);

			if (AIModule_Prisoner.DATA().judged.get(h.ai()) == 0 && STATS.LAW().prisonerType.get(h.indu()).isJudged) {
				if (AIModule_Prisoner.DATA().judged.get(h.ai()) == 0)
					text.error(¤¤JudgedNo);
				else
					text.text(¤¤Judged);
				text.NL(4);
			}

			text.textL(¤¤ClickToChange);

		}else if (h.indu().hType() == HTYPES.TOURIST()) {

			text.textLL(SETT.ROOMS().INN.info.name);
			text.NL();
			text.add(text.text().add(AIModule_Tourist.inn(h) == null ? ¤¤none : AIModule_Tourist.inn(h).name()));
			text.NL(8);

			text.textLL(¤¤Attraction);
			text.NL();
			text.text(TOURISM.attraction(h.indu()).info.name);
			text.NL(8);

			text.textLL(¤¤Service);
			text.NL();
			text.text(TOURISM.service(h.indu()) == null ? "?" : TOURISM.service(h.indu()).name);
			text.NL(8);

			text.textLL(Dic.¤¤Curr);
			text.NL();
			text.add(GFORMAT.iBig(text.text(), TOURISM.credits(h.race())));
			text.NL(8);
		}

		h.ai().hoverInfoSet(h, text);

		text.NL(8);





//		RoomInstance ins = STATS.WORK().EMPLOYED.get(h);
//		if (ins != null) {
//			text.add(GFORMAT.f(text.text(), h.race().bonus().get(ins.blueprintI().bonuses())));
//		}

	}

	void hover(Induvidual h, GBox text) {

		if (h == null)
			return;
		this.hum = null;
		this.indu = h;

		text.add(s);
		text.NL();

		for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
			int m = AD.cityDivs().soldiers(di);
			for (int i = 0; i < m; i++) {
				Induvidual ii = AD.cityDivs().getSoldier(i, di);
				if (ii == h) {


					Div div =  GAME.ARMIES().player().divisions().get(di);
					GText t = text.text();
					t.add(¤¤SoldierDiv);
					t.insert(0, div.info.name());
					t.insert(1, AD.cityDivs().attachedArmy(div).name);
					text.textL(t);

					if (AD.cityDivs().daysToReturn(div) > 0) {
						t = text.text();
						t.add(¤¤SoldierReturn);
						t.insert(0, AD.cityDivs().daysToReturn(div), 1);
						text.textL(t);
					}


				}

			}


		}



	}

}
