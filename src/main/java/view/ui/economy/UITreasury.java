package view.ui.economy;

import game.faction.FACTIONS;
import game.faction.FWorth.WINT;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.text.D;
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
import util.dic.Dic;
import util.dic.ExtraInfoDic;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.keyboard.KEYS;
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
	
	private GScrollRows ta;
	
	static {
		D.ts(UITreasury.class);
	}
	
	public UITreasury() {
		super(¤¤economy, UI.icons().l.coin);
		
		section.body().setWidth(WIDTH).setHeight(1);
		final IntImp ii = new IntImp();
		GETTER_IMP<RESOURCE> gres = new GETTER_IMP<>();
		
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
		GButt.ButtPanel unused = new GButt.ButtPanel(UI.icons().m.questionmark.resized(Icon.L)) {
			@Override
			protected void clickA() {
				selectedToggle();
			};
		}.pad(2, 4);
		unused.hoverInfoSet(¤¤unused);
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
		
		s.addRelBody(4, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, (int)FACTIONS.WORTH().faction());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				for (WINT d : FACTIONS.WORTH().faction) {
					b.add(d.icon);
					b.textL(d.info.name);
					b.tab(6);
					b.add(GFORMAT.iIncr(b.text(), d.player()));
					b.NL();
					b.text(d.info.desc);
					b.NL(5);
				}
			};
			
		}.hh(Dic.¤¤NetWorth));
		
		UIGoodsImport im = new UIGoodsImport();
		UIGoodsExport ex = new UIGoodsExport(true);
		ArrayList<RENDEROBJ> rows = new ArrayList<RENDEROBJ>(RESOURCES.ALL().size());
		for (RESOURCE res : RESOURCES.ALL())
			rows.add(new RRow(res, ii, gres, 10, im, ex));
		
		int height = HEIGHT-s.body().height()-16;
		height = height/rows.get(0).body().height();
		height *= rows.get(0).body().height();
		ta = new GScrollRows(rows, height) {
			
			@Override
			protected boolean passesFilter(int i, RENDEROBJ o) {
				RESOURCE res = RESOURCES.ALL().get(i);
				if (impot.selectedIs() && SETT.ROOMS().IMPORT.tally.capacity.get(res) > 0)
					return true;
				if (export.selectedIs() && SETT.ROOMS().EXPORT.tally.capacity.get(res) > 0)
					return true;
				if (unused.selectedIs() && SETT.ROOMS().IMPORT.tally.capacity.get(res) == 0 && SETT.ROOMS().EXPORT.tally.capacity.get(res) == 0)
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
		
		for (RESOURCE res : RESOURCES.ALL()) {
			if (SETT.ROOMS().IMPORT.tally.capacity.get(res) > 0) {
				GText t = b.text();
				CharSequence p = SETT.ROOMS().IMPORT.tally.problem(res, false);
				if (p == null) {
					p = SETT.ROOMS().EXPORT.tally.problem(res, false);
				}
				
				if (p != null) {
					b.add(res.icon());
					b.add(t.errorify().add(p));
					b.NL();
				}else {
					p = SETT.ROOMS().IMPORT.tally.warning(res, false);
					if (p == null) {
						p = SETT.ROOMS().IMPORT.tally.problem(res, false);
					}
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

}
