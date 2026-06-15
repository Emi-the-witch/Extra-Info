package view.sett.ui.standing;

import game.boosting.BOOSTABLES;
import game.time.TIME;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.CAUSE_ARRIVE;
import init.type.CAUSE_ARRIVES;
import init.type.CAUSE_LEAVE;
import init.type.CAUSE_LEAVES;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.HCLASS_RACE;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.stats.POP;
import settlement.stats.STATS;
import settlement.stats.colls.StatsPopulation.StatsDeath.PopData;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.sett.ui.standing.Cats.Cat;
import world.army.AD;

import static java.lang.Math.abs;

final class CatPopulation extends Cat {

	private static CharSequence ¤¤age = "¤Age {0} to {1} : {2} Subjects";
	private static CharSequence ¤¤ageAverage = "¤Average Age:";

	private static CharSequence ¤¤others = "¤Others";
	private static CharSequence ¤¤othersD = "¤Other population that count towards expectations, but not for fulfillment.";

	private static CharSequence ¤¤Soldiers = "¤Soldiers out campaigning";

	private final GETTER<Race> race;

	CatPopulation(HCLASS cl, GETTER<Race> race){
		super(new StatCollection[] {STATS.POP()});
		this.race = race;
		StatCollection c = STATS.POP();

		D.ts(CatPopulation.class);
		titleSet(c.info.name);

		section.add(pop(cl));

		section.addDown(4, popChart(cl, section.body().width()));
		section.addDown(4, new CatPopulationGrowth(cl, race) {

			@Override
			public HCLASS_RACE pop() {
				return HCLASS_RACE.clP(race.get(), cl);

			}

		});


		LinkedList<RENDEROBJ> rens = new LinkedList<RENDEROBJ>();

		for (STAT s : c.all()) {
			if (s.key() == null)
				continue;
			if (s == STATS.POP().age.AGE_DAYS) {
				continue;
			}else
				rens.add(new StatRow(s, cl, race));
		}






		section.addDown(16, new GScrollRows(rens, HEIGHT-section.getLastY2()-32, 0).view());

	}

	private GuiSection pop(HCLASS cl) {
		GuiSection s = new GuiSection();


		int ww = 160;

		for (HTYPE t : HTYPES.ALL()) {
			if ((t.CLASS != cl || t == HTYPES.SOLDIER()))
				continue;
			s.addDown(4, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.i(text, STATS.POP().pop(race.get(), t));
					text.lablifySub();
				}
			}.decrease().hh(t.icon, t.names, ww).hoverInfoSet(t.desc));
		}

		s.addDown(2, GCOLOR.UI().border().makeSprite(150, 1));

		s.addDown(2, new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, STATS.POP().POP.data(cl).get(race.get()));
			}
		}.decrease().hh(cl.names, ww).hoverInfoSet(STATS.POP().POP.info().desc));

		s.addDown(2, new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, POP.tot(cl, race.get())-STATS.POP().POP.data(cl).get(race.get()));
			}

			@Override
			public void hoverInfoGet(GBox b) {

				b.title(¤¤others);
				b.text(¤¤othersD);
				b.sep();

				for (HTYPE t : HTYPES.ALL()) {
					if (t.parent().CLASS != t.CLASS && t.parentClass() == cl)
						add(t, b);

				}

				if (cl == HCLASSES.CITIZEN()) {

					add(AD.cityDivs().total(race.get()), HTYPES.SOLDIER(), ¤¤Soldiers, b);
					add(STATS.LAW().criminals(HCLASSES.CITIZEN(), race.get()), HTYPES.PRISONER(),  HTYPES.PRISONER().desc, b);
					add(HTYPES.RIOTER(), b);
					add(HTYPES.DERANGED(), b);

				}

			};

			private void add(HTYPE t, GBox b) {
				add(STATS.POP().pop(race.get(), t), t, t.desc, b);
			}

			private void add(int am, HTYPE t, CharSequence desc, GBox b) {
				b.add(t.icon);
				b.textLL(t.names);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), am));
				b.NL();
				b.text(desc);
				b.NL(4);
			}

		}.decrease().hh(¤¤others, ww));

		s.addRelBody(48, DIR.E, demo(cl));


		return s;

	}

	private RENDEROBJ demo(HCLASS cl) {

		GuiSection s = new GuiSection();


		if (cl == HCLASSES.CITIZEN()) {
			GStaples staples = new GStaples(STATS.POP().demography().historyRecords()) {
				double demoMax;
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
					demoMax = 0;
					for (int i = 0; i < STATS.POP().demography().historyRecords(); i++)
						if (STATS.POP().demography().getD(race.get(), i) > demoMax)
							demoMax = STATS.POP().demography().getD(race.get(), i);
					super.render(r, ds, isHovered);
				}

				@Override
				protected void hover(GBox text, int stapleI) {
					int k = stapleI;
					text.title(STATS.POP().demography().info().name);
					if (race.get() != null) {
						Text t = text.text();
						t.add(¤¤age);
						double da = race.get().bvalue(BOOSTABLES.PHYSICS().DEATH_AGE);
						{

							int from = (int) ((k)*da/(STATS.POP().demography().historyRecords()-1));
							t.insert(0, from);

							if (k == STATS.POP().demography().historyRecords()-1) {
								t.insert(1, '+');
							}else {
								int to = (int) ((k+1)*da)/(STATS.POP().demography().historyRecords()-1);
								t.insert(1, to);
							}

							t.insert(2, (int) STATS.POP().demography().getD(race.get(), k));
							text.add(t);

							text.NL(8);
						}




					}

				}

				@Override
				protected double getValue(int stapleI) {
					int k = stapleI;
					double am = STATS.POP().demography().getD(race.get(), k);
					if (demoMax > 0) {
						am /= demoMax;
					}
					return am;
				}

				@Override
				protected void setColor(ColorImp c, int stapleI, double value) {
					c.set(GCOLOR.UI().SOSO.hovered);
				}
			};

			staples.body().setWidth(10*STATS.POP().demography().historyRecords());
			staples.body().setHeight(100);
			s.add(staples);
		}

		s.addRelBody(2, DIR.N, new GStat() {

			@Override
			public void update(GText text) {
				double d = STATS.POP().age.AGE_DAYS.data(cl).getD(race.get())/TIME.years().bitConversion(TIME.days());

				GFORMAT.f(text, d);
			}
		}.hh(¤¤ageAverage));

		return s;
	}



	private RENDEROBJ popChart(HCLASS cl, int width) {

		GuiSection ss = new GuiSection();

		GStaples s = new GStaples(STATS.DAYS_SAVED) {

			@Override
			protected void hover(GBox box, int stapleI) {

				box.title(Dic.¤¤Population);
				int i = STATS.DAYS_SAVED - stapleI - 1;
				box.add(box.text().add(-i).s().add(TIME.days().cycleName()));
				box.NL(8);

				box.textLL(Dic.¤¤Population);
				box.tab(7);
				box.add(GFORMAT.iBig(box.text(), (int) getValue(stapleI)));
				box.NL(8);

				//// Add tot variable
				int tot = 0 ;//#!# tot use

				for (int ci = 0; ci < CAUSE_ARRIVES.ALL().size(); ci++) {
					CAUSE_ARRIVE a = CAUSE_ARRIVES.ALL().get(ci);
					int am = STATS.POP().COUNT.enters().get(a.index()).statistics(cl).history(race.get()).get(i);

					if (am > 0) {
						tot += abs(am);  //#!# tot use
						box.textL(a.name);
						box.tab(7);
						box.add(GFORMAT.iIncr(box.text(), am));
						box.NL();
					}
				}

				box.NL(4);

				for (int ci = 0; ci < CAUSE_LEAVES.ALL().size(); ci++) {
					CAUSE_LEAVE a = CAUSE_LEAVES.ALL().get(ci);
					int am = STATS.POP().COUNT.leaves().get(a.index()).statistics(cl).history(race.get()).get(i);

					if (am > 0) {
						tot -= abs(am); //#!# tot use
						box.textL(a.names);
						if (a.defaultStanding() > 0)
							box.tab(6).add(UI.icons().s.angry, GCOLOR.UI().BAD.hovered);
						box.tab(7);
						box.add(GFORMAT.iIncr(box.text(), -am));
						box.NL();
					}
				}

				box.NL(4);
				box.textLL(Dic.¤¤Total);
				box.tab(7);
				box.add(GFORMAT.iIncr(box.text(), (int) (getValue(stapleI)-getValue(stapleI-1))));
				box.NL(4);

				//////////////////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////////////////////////////////////////////
				//#!# Annual sum added
				box.textLL("Annual Sums:");
				box.NL(8);

				tot = 0;


				for (CAUSE_ARRIVE a : CAUSE_ARRIVES.ALL()) {

					int am = STATS.POP().COUNT.enters().get(a.index()).statistics(cl).history(race.get()).getPeriodSum(-i-16,0);
					if (am > 0) {
						tot += abs(am);
						box.textL(a.name);
						box.tab(7);
						box.add(GFORMAT.iIncr(box.text(), am));
						box.NL();
					}

				}

				box.NL(4);

				for (CAUSE_LEAVE a : CAUSE_LEAVES.ALL()) {

					int am = STATS.POP().COUNT.leaves().get(a.index()).statistics(cl).history(race.get()).getPeriodSum(-i-16,0);
					if (am > 0) {
						tot -= abs(am);
						box.textL(a.names);
						if (a.defaultStanding() > 0)
							box.tab(6).add(UI.icons().s.angry, GCOLOR.UI().BAD.hovered);
						box.tab(7);
						box.add(GFORMAT.iIncr(box.text(), -am));
						box.NL();
					}

				}

				box.NL(4);
				box.textLL(Dic.¤¤Total);
				box.add(GFORMAT.iIncr(box.text(), tot));
				box.NL(4);

				box.sep();
				//////////////////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////////////////////////////////////////////

				if (cl == HCLASSES.CITIZEN()) {
					box.sep();
					box.textLL(Dic.¤¤Type);
					box.NL();
					for (STAT t : STATS.POP().TYPE.all()){
						box.textL(t.info().name);
						box.tab(7);
						box.add(GFORMAT.i(box.text(), t.data(cl).get(race.get(), i)));
						box.NL();
					}
				}



			}

			@Override
			protected double getValue(int stapleI) {
				int i = STATS.DAYS_SAVED-stapleI-1;
				if (i >= STATS.DAYS_SAVED)
					i = STATS.DAYS_SAVED-1;
				if (i < 0)
					i = 0;
				int am = STATS.POP().POP.data(cl).get(race.get(), i);

				return am;
			}

			@Override
			protected void setColor(ColorImp c, int stapleI, double value) {
				int i = STATS.DAYS_SAVED-stapleI-1;
				for (int ci = 0; ci < CAUSE_LEAVES.ALL().size(); ci++) {
					CAUSE_LEAVE a = CAUSE_LEAVES.ALL().get(ci);
					if (!a.natural) {
						int am = STATS.POP().COUNT.leaves().get(a.index()).statistics(cl).history(race.get()).get(i);

						if (am > 0) {
							c.set(GCOLOR.UI().BAD.normal);
							return;
						}

					}

				}

				super.setColor(c, stapleI, value);
			}
		};
		s.normalizePlus(true);

		s.body().setWidth(width);
		s.body().setHeight(80);
		ss.add(s);


		final SPRITE[] cols = new SPRITE[STATS.POP().COUNT.leaves().size()];
		for (int i = 0; i < cols.length; i++) {
			int k = i;
			cols[i]= new SPRITE.Imp(Icon.M, Icon.M) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					COLOR.UNIQUE.getC(k).bind();
					SPRITES.icons().m.circle_inner.render(r, X1, Y1);
					COLOR.unbind();
					//SPRITES.icons().m.circle_frame.render(r, X1, Y1);

				}
			};
		}

		GStaples sss = new GStaples(STATS.DAYS_SAVED) {

			@Override
			protected void hover(GBox box, int stapleI) {
				box.title(STATS.POP().WRONGFUL.info().names);
				int i = STATS.DAYS_SAVED - stapleI - 1;
				box.add(box.text().add(-i).s().add(TIME.days().cycleName()));
				box.NL(8);
				int di = 0;
				for (PopData s : STATS.POP().COUNT.leaves()) {
					if (CAUSE_LEAVES.ALL().get(di).defaultStanding() <= 0) {
						di++;
						continue;
					}
					box.add(cols[di]);
					box.textL(s.info().name);
					box.tab(7);
					box.add(GFORMAT.iIncr(box.text(), s.statistics(cl).history(race.get()).get(i)));
					box.NL();
					di++;
				}
			}

			@Override
			protected double getValue(int stapleI) {
				double am = 0;
				int i = STATS.DAYS_SAVED-stapleI-1;
				int di = 0;
				for (PopData s : STATS.POP().COUNT.leaves()) {
					if (CAUSE_LEAVES.ALL().get(di++).defaultStanding() <= 0)
						continue;
					am += s.statistics(cl).history(race.get()).get(i);
				}
				return am;
			}

			@Override
			protected void renderExtra(SPRITE_RENDERER r, COLOR color, int stapleI, boolean hovered, double value,
			                           int x1, int x2, int y1, int y2) {

				double am = 0;
				int i = STATS.DAYS_SAVED-stapleI-1;
				for (PopData s : STATS.POP().COUNT.leaves()) {
					am += s.statistics(cl).history(race.get()).get(i);
				}

				int h = y2-y1;
				if (h <= 0)
					h = 1;
				if (am == 0)
					return;

				int ci = 0;
				for (PopData s : STATS.POP().COUNT.leaves()) {

					double d = s.statistics(cl).history(race.get()).get(i);
					d /= am;
					int hh = (int) Math.ceil(h*d);

					if (hh > 0) {
						ColorImp c = ColorImp.TMP;
						c.set(COLOR.UNIQUE.getC(ci));
						c.shadeSelf(hovered ? 0.75 : 0.55);
						c.render(r, x1, x2, y2-hh, y2);
						c.set(COLOR.UNIQUE.getC(ci));
						c.shadeSelf(hovered ? 1 : 0.80);
						c.render(r, x1+1, x2-1, y2-hh+1, y2-1);
						y2-= hh;
					}
					ci++;

				}

			}
		};


		s.body().setWidth(width);
		s.body().setHeight(80);
		ss.addDown(6, sss);

		return ss;

	}



}
