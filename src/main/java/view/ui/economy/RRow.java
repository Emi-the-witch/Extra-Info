package view.ui.economy;

import game.GAME;
import game.faction.FACTIONS;
import init.trade.TRADABLE;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import util.colors.GCOLOR;
import util.data.GETTER.GETTERE;
import util.data.INT.INTE;
import util.dic.ExtraInfoDic;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import util.text.DicTime;
import view.ui.goods.UIGoodsExport;
import view.ui.goods.UIGoodsImport;

/////////////////////////////////////////////////////////////////////////////////////////////////
///#!# Adds the 3-year and 1-year sums onto each resource in the treasury UI.
////////////////////////////////////////////////////////////////////////////////////////////////
final class RRow extends GuiSection {

	public static final COLOR colorExport = new ColorImp(100, 90, 70);
	public static final COLOR colorInport = new ColorImp(80, 80, 100);

	private final int w;
	private static int amount = STATS.DAYS_SAVED;
	private static final int height = 60;

	private final GStaples[] dias;
	private INTE hi;
	private final TRADABLE res;
	private final GETTERE<TRADABLE> rcurrent;

	private static CharSequence ¤¤Imports = "Imports";
	private static CharSequence ¤¤Exports = "Exports";
	private static CharSequence ¤¤Lowest = "Lowest";
	private static CharSequence ¤¤Highest = "Highest";
	private static CharSequence ¤¤Unit = "Unit";
	//private static CharSequence ¤¤CapacityN = "¤Trade Capacity";
	//private static CharSequence ¤¤CapacityNDesc = "¤The combined daily trade capacity of your trade partners, and how much that you have currently traded. Once the capacity is exceeded, the factions will add expensive tariffs to your trade to protect their economies.";

	static {
		D.ts(RRow.class);
	}

	RRow(TRADABLE r, INTE hi, GETTERE<TRADABLE> rcurrent, int w, UIGoodsImport im, UIGoodsExport ex) {
		this.res = r;
		this.hi = hi;
		this.w = w;
		this.rcurrent = rcurrent;
		dias = new GStaples[] {
				new TradeDiagram(r),
				new RRowPriceDia(r, GCOLOR.UI().BAD.hovered, FACTIONS.player().trade.pricesBuy, height),
				new RRowPriceDia(r, GCOLOR.UI().GOOD.hovered, FACTIONS.player().trade.pricesSell, height),
		};

		addRelBody(0, DIR.E, dias[0]);

//		addRelBody(0, DIR.E, new Caps());


		addRelBody(12, DIR.E, dias[1]);
		addRelBody(0, DIR.E, UIGoodsImport.miniControl(r, im));

		addRelBody(12, DIR.E, dias[2]);
		addRelBody(0, DIR.E, UIGoodsExport.mini(r, ex));


		addRelBody(8, DIR.W, res.icon().scaled(2));

		pad(2, 6);
	}

	@Override
	public boolean hover(COORDINATE mCoo) {
		boolean b = super.hover(mCoo);
		for (GStaples ss : dias) {
			if (ss.hoveredIs()) {
				hi.set(ss.hoverI());
				rcurrent.set(res);
			}
		}
		if (hi.get() >= 0 && rcurrent.get() == res) {
			for (GStaples ss : dias) {
				ss.setHovered(hi.get());
			}
		}
		return b;
	}

	@Override
	public void hoverInfoGet(GUI_BOX text) {
		if (hi.get() < 0) {
			super.hoverInfoGet(text);
			return;
		}
		if (rcurrent.get() != res) {
			super.hoverInfoGet(text);
			return;
		}
		GBox b = (GBox) text;
		b.title(res.names);

		int si = amount-hi.get()-1;

		{
			GText t = b.text();
			t.lablify();
			DicTime.setAgo(t, si*GAME.player().res().time.bitSeconds());
			b.add(t);
			b.sep();
		}

		{
			b.textLL(Dic.¤¤Stored);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), FACTIONS.player().seller(res).storedHistorically(si)));
			b.NL();
			b.textLL(Dic.¤¤avePrice);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), FACTIONS.player().trade.pricesAve.history(res).get(si)));
			b.NL();
			b.sep();

		}
		{
			b.textLL(¤¤Imports);
			b.NL();
			b.add(b.text().add(Dic.¤¤Price).s().add('(').add(¤¤Lowest).add(')'));
			b.tab(6);
			b.add(GFORMAT.i(b.text(), FACTIONS.player().trade.pricesBuy.history(res).get(si)));
			b.NL();
			b.add(b.text().add(Dic.¤¤Bought));
			b.tab(6);
			b.add(GFORMAT.i(b.text(), FACTIONS.player().trade.unitsImported.history(res).get(si)));
			b.NL();
			b.add(b.text().add(Dic.¤¤Earnings).s().add('/').s().add(¤¤Unit));
			b.tab(6);
			b.add(GFORMAT.iIncr(b.text(), -FACTIONS.player().trade.priceImported.history(res).get(si)));
			b.NL();
			b.add(b.text().add(Dic.¤¤Earnings));
			b.tab(6);
			b.add(GFORMAT.iIncr(b.text(), -FACTIONS.player().trade.outImported.history(res).get(si)));
			b.NL();

			b.NL(8);

			b.textLL(¤¤Exports);
			b.NL();
			b.add(b.text().add(Dic.¤¤Price).s().add('(').add(¤¤Highest).add(')'));
			b.tab(6);
			b.add(GFORMAT.i(b.text(), FACTIONS.player().trade.pricesSell.history(res).get(si)));
			b.NL();
			b.add(b.text().add(Dic.¤¤Sold));
			b.tab(6);
			b.add(GFORMAT.i(b.text(), FACTIONS.player().trade.unitsExported.history(res).get(si)));
			b.NL();
			b.add(b.text().add(Dic.¤¤Earnings).s().add('/').s().add(¤¤Unit));
			b.tab(6);
			b.add(GFORMAT.iIncr(b.text(), FACTIONS.player().trade.priceExported.history(res).get(si)));
			b.NL();
			b.add(b.text().add(Dic.¤¤Earnings));
			b.tab(6);
			b.add(GFORMAT.iIncr(b.text(), FACTIONS.player().trade.inExported.history(res).get(si)));
			b.NL();

			b.sep();
			b.textL(Dic.¤¤Total);
			b.tab(6);
			b.add(GFORMAT.iIncr(b.text(), GAME.player().trade.inExported.history(res).get(si)-GAME.player().trade.outImported.history(res).get(si)));
			b.NL();

		}


	}



	private class TradeDiagram extends GStaples {

		private final TRADABLE res;
		private GStat tprofits = new GStat() {

			@Override
			public void update(GText text) {
				/////////////////////////////////////////////#!#
				// Add the last year and last three-year history to the text over each resource bought or sold
				CharSequence output;

				// output = "3 Year ";
				output = ExtraInfoDic.year3;
				GFORMAT.text(text, output);
				GFORMAT.iIncr(text, (GAME.player().trade.inExported.history(res).getPeriodSum(-48,0)-GAME.player().trade.outImported.history(res).getPeriodSum(-48,0)) );


				// output =  "   1 Year ";
				output =  ExtraInfoDic.year1;
				GFORMAT.text(text, output);
				GFORMAT.iIncr(text, (GAME.player().trade.inExported.history(res).getPeriodSum(-16,0)-GAME.player().trade.outImported.history(res).getPeriodSum(-16,0)) );


				// GFORMAT.text(text, "   Yesterday ");
				GFORMAT.text(text, ExtraInfoDic.yesterday);
				GFORMAT.iIncr(text, GAME.player().trade.inExported.history(res).get(1)-GAME.player().trade.outImported.history(res).get(1));
				/////////////////////////////////////////////#!#
				GFORMAT.iIncr(text, GAME.player().trade.inExported.history(res).get(1)-GAME.player().trade.outImported.history(res).get(1));
			}
		}.bg();

		TradeDiagram(TRADABLE res){
			super(amount, false);
			this.res = res;
			body().setWidth(w*amount).setHeight(height);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {

			super.render(r, ds, hoveredIs());
			tprofits.render(r, body().x1()+w, body().y1()+ w/2);
		}

		@Override
		protected double getValue(int stapleI) {
			stapleI = amount-1-stapleI;
			return Math.abs(GAME.player().trade.inExported.history(res).get(stapleI)-GAME.player().trade.outImported.history(res).get(stapleI));
		}

		@Override
		protected void hover(GBox box, int stapleI) {


		}

		@Override
		protected void setColor(ColorImp c, int x, double value) {
			x = amount-1-x;
			if (GAME.player().trade.inExported.history(res).get(x)-GAME.player().trade.outImported.history(res).get(x) < 0)
				c.set(GCOLOR.UI().BAD.normal);
			else
				c.set(GCOLOR.UI().GOOD.normal);

		}
	}

//	private class Caps extends HoverableAbs {
//
//		Caps(){
//			super(32, height);
//		}
//
//		@Override
//		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
//			double has = 0;
//			double tot = 0;
//			for (FactionNPC f : RD.DIST().neighs()) {
//				if (DIP.get(f).trades) {
//					tot += f.res(res).playerTradeLimit();
//					has += Math.abs(f.res(res).playerTraded());
//				}
//			}
//			if (tot == 0) {
//				GMeter.renderH(r, GMeter.C_ORANGE, 0, body());
//
//			}else {
//				double t = CLAMP.d(has/tot, 0, 1);
//				GMeter.renderH(r, t >= 1 ? GMeter.C_RED : GMeter.C_ORANGE, t, body);
//
//			}
//		}
//
//		@Override
//		public void hoverInfoGet(GUI_BOX text) {
//			// TODO Auto-generated method stub
//			super.hoverInfoGet(text);
//
//			GBox b = (GBox) text;
//			text.title(¤¤CapacityN);
//			text.text(¤¤CapacityNDesc);
//			text.NL(8);
//
//			double has = 0;
//			double tot = 0;
//			for (FactionNPC f : RD.DIST().neighs()) {
//				if (DIP.get(f).trades) {
//					int t = (int) f.res(res).playerTradeLimit();
//					int h = (int) Math.abs(f.res(res).playerTraded());
//					tot += t;
//					has += h;
//					b.add(f.banner().MEDIUM);
//					b.textL(f.name);
//					b.tab(7);
//					b.add(GFORMAT.iofk(b.text(), h, t));
//					b.NL();
//				}
//			}
//
//			b.textLL(Dic.¤¤Total);
//			b.tab(7);
//			b.add(GFORMAT.iofk(b.text(), (int)has, (int)tot));
//			b.NL();
//		}
//
//	}

}
