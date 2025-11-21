package view.sett.ui.subject;

import game.tourism.TOURISM;
import init.race.appearence.RPortrait;
import init.settings.S;
import init.sprite.UI.Icon;
import util.text.D;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.types.prisoner.AIModule_Prisoner;
import settlement.entity.humanoid.ai.types.tourist.AIModule_Tourist;
import settlement.main.SETT;
import settlement.room.service.nursery.ROOM_NURSERY;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.Processing.Punishment;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import util.text.Dic;
import util.text.DicTime;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;

import static java.lang.Math.round;

class UIHoverer {
	/////////////////////////////////////////////////////////////////////////////////////////////////
	///#!# Adds text to each person for a quick glance at who the person is and what they're doing.
	// #!# could add "wants to do next" using the priority system for second place, but low priority
	////////////////////////////////////////////////////////////////////////////////////////////////
	private Humanoid h;
	private static CharSequence ¤¤Sentenced = "Sentenced to be:";
	private static CharSequence ¤¤ClickToChange = "Click to change punishment.";
	private static CharSequence ¤¤JudgedNo = "Pleads innocence. Wants to try case in court.";
	private static CharSequence ¤¤Judged = "Has been found guilty in a court.";
	
	private static CharSequence ¤¤Attraction = "Attraction";
	private static CharSequence ¤¤Service = "Service";
	private static CharSequence ¤¤none = "---";
	
	static {
		D.ts(UIHoverer.class);
	}
	
	private GuiSection s = new GuiSection();
	
	public UIHoverer() {
		
		s.addRightC(8, new GStat() {
			
			@Override
			public void update(GText text) {
				text.lablify();
				text.clear().add(STATS.APPEARANCE().name(h.indu()));
				text.setMaxWidth(300);
				text.setMultipleLines(false);
			}
		}.increase());
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				text.add(h.race().info.namePosessive);
				text.s().add(h.title());
				
				CharSequence extra = null;
				if (h.indu().hType() == HTYPES.SLAVE())
					extra = h.indu().clas().name;
				else if (h.indu().hType() == HTYPES.PRISONER())
					extra = STATS.LAW().prisonerType.get(h.indu()).title;
				
				if (extra != null)
					text.s().add('(').add(extra).add(')');
				
			}
		}, 0, s.body().y2()+2);

		s.addDown(2, new GStat() {

			@Override
			public void update(GText text) {
				h.ai().getOccupation(h, text);

			}
		});
		
		s.addRelBody(8, DIR.W, new SPRITE() {
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				if (h.indu().hType() == HTYPES.CHILD()) {
					for (ROOM_NURSERY ro : SETT.ROOMS().NURSERIES) {
						if (ro.race == h.race()) {
							int w = X2-X1;
							int h = Y2-Y1;
							w -= Icon.HUGE;
							h -= Icon.HUGE;
							w /= 2;
							h /= 2;
							ro.icon.renderScaled(r, X1+w, Y1+h, 2);
							return;
						}
					}
					return;
				}
				STATS.APPEARANCE().portraitRender(r, h.indu(), X1, Y1, 2);
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
		this.h = h;

		if (h == null)
			return;


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
		text.add(GFORMAT.text(text.text(), (int) (round( STATS.EDUCATION().total(h.indu())*100))+ "% Educated"));
		text.NL();


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
			Punishment p = AIModule_Prisoner.punishment(h, h.ai());
			text.textLL(p.name);
			if (p == LAW.process().prison) {
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
			text.text(TOURISM.service(h.indu()).name);
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
}
