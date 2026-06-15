package view.ui.economy;

import game.faction.FACTIONS;
import game.faction.FWorth.WINT;
import game.faction.npc.FactionNPC;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.trade.TR;
import init.trade.TRADABLE;
import init.type.HCLASS_RACE;
import snake2d.SPRITE_RENDERER;
import util.data.GETTER;
import util.gui.common.UIPickerRace;
import util.gui.table.GTableBuilder;
import util.text.D;
import init.type.HCLASSES;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.IntImp;
import util.text.Dic;
import util.dic.ExtraInfoDic;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.ui.goods.UIGoodsExport;
import view.ui.goods.UIGoodsImport;
import view.ui.manage.IFullView;

import static view.ui.goods.UIProduction.*;

public final class UITreasury extends IFullView {
	/////////////////////////////////////////////////////////////////////////////////////////////////
	///#!# Adds the Production, Consumption, Sum, Net Trade for town and individual use
	////////////////////////////////////////////////////////////////////////////////////////////////
	private static CharSequence ¤¤unused = "Show resources not actively traded";
	private static CharSequence ¤¤import = "Show resources that are imported.";
	private static CharSequence ¤¤export = "Show resources that are exported.";
	private static CharSequence ¤¤economy = "Economy & Trade";
	private static CharSequence ¤¤priceDesc = "The average global price, the total production rate without boosts, and the price multiplied with the production rate. This might give you a sense of what industries are profitable for you. The last two columns shows your current average bonus of selected race.";

	private GScrollRows ta;

	static {
		D.ts(UITreasury.class);
	}

	public UITreasury() {
		super(¤¤economy, UI.icons().l.coin);

		section.body().setWidth(WIDTH).setHeight(1);
		final IntImp ii = new IntImp();
		GETTER_IMP<TRADABLE> gres = new GETTER_IMP<>();


		GuiSection s = new GuiSection() {

			@Override
			public boolean hover(COORDINATE mCoo) {
				ii.set(-1);
				gres.set(null);
				return super.hover(mCoo);
			}

		};
		s.addDownC(0, new MainChart(HEIGHT, ii, 10));

		s.addRight(32, new MainDetails(ii));

		GuiSection f = new GuiSection();

		GButt.ButtPanel oo = new GButt.ButtPanel(UI.icons().m.coins.resized(Icon.L)) {
			GuiSection s = new Prices();

			@Override
			protected void clickA() {
				VIEW.inters().popup.show(s, this);
			};
		}.pad(2, 4);
		f.addDown(0, oo);
		GButt.ButtPanel unused = new GButt.ButtPanel(UI.icons().m.questionmark.resized(Icon.L)) {
			@Override
			protected void clickA() {
				selectedToggle();
			};
		}.pad(2, 4);
		unused.hoverInfoSet(¤¤unused);
		unused.selectedSet(true);
		f.addDown(0, unused);
		GButt.ButtPanel impot = new GButt.ButtPanel(SETT.ROOMS().IMPORT.icon) {
			@Override
			protected void clickA() {
				selectedToggle();
			};
		}.pad(2, 4);
		impot.hoverInfoSet(¤¤import);
		impot.selectedSet(true);
		f.addDown(0, impot);
		GButt.ButtPanel export = new GButt.ButtPanel(SETT.ROOMS().EXPORT.icon) {
			@Override
			protected void clickA() {
				selectedToggle();
			};
		}.pad(2, 4);
		export.hoverInfoSet(¤¤export);
		export.selectedSet(true);
		f.addDown(0, export);

		s.add(f, s.body().x2() + 32, s.body().y2()-f.body().height());



		UIGoodsImport im = new UIGoodsImport();
		UIGoodsExport ex = new UIGoodsExport(true);
		ArrayList<RENDEROBJ> rows = new ArrayList<RENDEROBJ>(TR.ALL().size());
		for (TRADABLE res : TR.ALL())
			rows.add(new RRow(res, ii, gres, 12, im, ex));

		int height = HEIGHT-s.body().height()-16;
		height = height/rows.get(0).body().height();
		height *= rows.get(0).body().height();
		ta = new GScrollRows(rows, height) {

			@Override
			protected boolean passesFilter(int i, RENDEROBJ o) {
				if (unused.selectedIs())
					return true;
				TRADABLE res = TR.ALL().get(i);
				if (impot.selectedIs() && res.pb().importing())
					return true;
				if (export.selectedIs() && res.ps().exporting() == null)
					return true;
				return false;
			};
		};
		s.add(ta.view(), s.body().x1()-58, s.body().y2()+8);

		s.add(new Factions(HEIGHT), s.body().x2()+16, s.body().y1());
		section.addRelBody(16, DIR.S, s);



	}

	@Override
	public void hoverInfoGet(GUI_BOX box) {
		GBox b = (GBox) box;
		b.title(¤¤economy);

		b.textLL(Dic.¤¤Treasury);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), (long) FACTIONS.player().credits().getD()));
		b.NL();

		for (TRADABLE res : TR.ALL()) {
			if (res.pb().importing()) {
				GText t = b.text();
				CharSequence p = FACTIONS.player().buyer(res).problem();
				if (p != null) {
					b.add(res.icon());
					b.add(t.errorify().add(p));
					b.NL();
				}else {
					p = FACTIONS.player().buyer(res).warning();
					if (p != null) {
						b.add(res.icon());
						b.add(t.warnify().add(p));
						b.NL();
					}
				}
			}

			if (res.ps().exporting() == null) {
				GText t = b.text();
				CharSequence p = FACTIONS.player().seller(res).problem();


				if (p != null) {
					b.add(res.icon());
					b.add(t.errorify().add(p));
					b.NL();
				}else {
					p = FACTIONS.player().seller(res).warning();
					if (p != null) {
						b.add(res.icon());
						b.add(t.warnify().add(p));
						b.NL();
					}
				}
			}


		}
		////////////////////////////#!#
		b.sep();
		// b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Town sum and Individual average:"));
		b.add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.treasuryTop));
		b.NL();
		// b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Production"));b.tab(3);
		b.add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.treasuryMsg1));b.tab(3);
		// b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Consumption"));b.tab(6);
		b.add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.treasuryMsg2));b.tab(6);
		// b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Sum"));b.tab(9);
		b.add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.treasuryMsg3));b.tab(9);
		// b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Net Trade"));
		b.add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.treasuryMsg4));
		b.NL();
		b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) (production())));b.tab(3);
		b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) (consumption())));b.tab(6);
		b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) Math.round(production()+consumption()) ));b.tab(9);
		b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) Math.round(net()) ));
		b.NL();
		double pop = 0;
		for (Race res : RACES.all()) {
			pop += STATS.POP().POP.data(HCLASSES.CITIZEN()).get(res);
		}
		b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) (production()/pop)));b.tab(3);
		b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) (consumption()/pop)));b.tab(6);
		b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) Math.round((production()+consumption())/pop) ));b.tab(9);
		b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) Math.round(net()/pop) ));
		b.sep();

		// b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Press Undo button for more info"));
        // undo button -> {0}
        GText tmp = new GText(UI.FONT().S, 0);
        GFORMAT.text(tmp, ExtraInfoDic.treasuryTip);
        tmp.insert(0, KEYS.MAIN().UNDO.repr());
		b.add(tmp);

		b.NL();

		if (KEYS.MAIN().UNDO.isPressed()) {
			b.sep();
			// b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "The first line of numbers is the town's total, the second line is the average person in town. The production, consumption, and sum values assume the 'world average price' for all items. Net Trade uses the consumption and production values per resource, and it assumes you sell your excess resources and buy any resources you don't regularly make using your currently available trade partner prices."));
			b.add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.treasuryInfo));

		}
		////////////////////////////#!#
	}


	private static class Prices extends GuiSection{

		Prices(){

			UIPickerRace pick = new UIPickerRace();
			pick.set(FACTIONS.player().race().index);

			GTableBuilder bu = new GTableBuilder() {

				@Override
				public int nrOFEntries() {
					return TR.ALL().size();
				}
			};

			bu.column("", Icon.M, new GTableBuilder.GRowBuilder() {

				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new RENDEROBJ.RenderImp(Icon.M) {

						@Override
						public void render(SPRITE_RENDERER r, float ds) {
							TR.ALL().get(ier.get()).icon().render(r, body);
						}
					};
				}
			});

			bu.column(Dic.¤¤Price, 120, new GTableBuilder.GRowBuilder() {

				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {

						@Override
						public void update(GText text) {
							GFORMAT.i(text, FACTIONS.PRICE().get(TR.ALL().get(ier.get())));
						}
					}.r(DIR.NW);
				}
			});



			bu.column(Dic.¤¤Rate, 120, new GTableBuilder.GRowBuilder() {

				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {

						@Override
						public void update(GText text) {
							GFORMAT.f(text, 1.0/SETT.RECIPES().ratesV.vanillaRate(TR.ALL().get(ier.get())));
						}
					}.r(DIR.NW);
				}
			});

			if (S.get().developer) {
				bu.column("dRate", 120, new GTableBuilder.GRowBuilder() {

					@Override
					public RENDEROBJ build(GETTER<Integer> ier) {
						return new GStat() {

							@Override
							public void update(GText text) {
								TRADABLE res = TR.ALL().get(ier.get());
								double rr = 0;
								double p = 0;
								for (FactionNPC f : FACTIONS.NPCs()) {
									p += f.citizens(null);
									rr += f.res(res).rateTot()*f.citizens(null);
								}
								rr /= p;
								double r = SETT.RECIPES().ratesV.vanillaRate(res)/(1.0/rr);
								GFORMAT.f(text, r);
							}
						}.r(DIR.NW);
					}
				});
			}

			bu.column(Dic.¤¤Rate + " x " + Dic.¤¤Price, 120, new GTableBuilder.GRowBuilder() {

				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {

						@Override
						public void update(GText text) {
							GFORMAT.f(text, FACTIONS.PRICE().get(TR.ALL().get(ier.get()))/SETT.RECIPES().ratesV.vanillaRate(TR.ALL().get(ier.get())));
						}
					}.r(DIR.NW);
				}
			});

			bu.column(Dic.¤¤Rate + "*", 120, new GTableBuilder.GRowBuilder() {

				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {

						@Override
						public void update(GText text) {
							GFORMAT.f(text, 1.0/SETT.RECIPES().rates.rateTotal(HCLASS_RACE.clP(pick.race()), TR.ALL().get(ier.get())));
						}
					}.r(DIR.NW);
				}
			});

			bu.column(Dic.¤¤Rate + " x " + Dic.¤¤Price + "*", 120, new GTableBuilder.GRowBuilder() {

				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {

						@Override
						public void update(GText text) {
							GFORMAT.f(text, FACTIONS.PRICE().get(TR.ALL().get(ier.get()))/SETT.RECIPES().rates.rateTotal(HCLASS_RACE.clP(pick.race()), TR.ALL().get(ier.get())));
						}
					}.r(DIR.NW);
				}
			});

			add(bu.create(16, true));

			addRelBody(16, DIR.N, pick.section);

			GText t = new GText(UI.FONT().S, ¤¤priceDesc);
			t.setMaxWidth(400);
			t.setMultipleLines(true);



			addRelBody(4, DIR.N, t);
		}


	}

}

