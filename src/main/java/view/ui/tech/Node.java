package view.ui.tech;

import game.boosting.BUtil;
import game.boosting.BoostSpec;
import game.boosting.Boostable;
import game.faction.FACTIONS;
import game.faction.player.PTech;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TECH.TechRequirement;
import init.tech.TECHS;
import init.tech.TechCost;
import init.tech.TechCurrency;
import init.type.HCLASS_RACE;
import init.value.Lock;
import settlement.main.SETT;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.main.RoomBlueprintImp;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.Tuple;
import snake2d.util.sets.Tuple.TupleImp;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.ui.tech.NodeBoosts.tEntry;
import view.ui.tech.NodeBoosts.upEntry;

final class Node extends ClickableAbs {

	public final static int WIDTH = 80;
	public final static int HE2IGHT = 92+12;
	private static final COLOR Cdormant = COLOR.WHITE100.shade(0.3);
	private static final COLOR Chovered = COLOR.WHITE100.shade(0.8);
	private static final COLOR Cfinished = new ColorImp(10, 120, 120);

	private static ColorImp rim = new ColorImp();


	private static CharSequence ¤¤Relock = "¤Hold {0} and click to disable this technology. The following points will be added to your frozen pool:";
	private static CharSequence ¤¤unlocked = "Unlocked";
	private static CharSequence ¤¤available = "Available to be unlocked";
	private static CharSequence ¤¤locked = "Locked by Requirements";
	private static CharSequence ¤¤afford = "Unable to Afford";
	private static CharSequence ¤¤workValueGood = "This technology will be a profitable investment, increasing production as if you employed {0} workers, ({1} workers per point spent). However... there might be other technologies that are better investments.";
	private static CharSequence ¤¤workValueBad = "Your affected industries are too small for this technology to be a good investment. Currently you get {0} more workers, equivalent to {1} workers per tech point spent. You should increase the size of your industries to make this tech worthwhile";

	final static LIST<COLOR> cols = new ArrayList<COLOR>(
			new ColorImp(50, 255, 50).shade(0.5),
			new ColorImp(50, 255, 255).shade(0.5),
			new ColorImp(255, 255, 50).shade(0.5),
			new ColorImp(255, 50, 255).shade(0.5)
	);

	final static LIST<DIR> dirs = new ArrayList<DIR>(
			DIR.SW,
			DIR.SE,
			DIR.NW,
			DIR.NE,
			DIR.S,
			DIR.E,
			DIR.N,
			DIR.W

	);

	static {
		D.ts(Node.class);
	}



	private final ArrayListGrower<Tuple.TupleImp<Edge, Integer>> edges = new ArrayListGrower<>();
	private final ArrayListGrower<Node> parents = new ArrayListGrower<>();
	public int hoverI;

	public final TECH tech;

	private final NodeBoosts upgradeBoost;

	Node(TECH tech, NodeBoosts upgradeBoost) {
		this.tech = tech;
		body.setDim(WIDTH, HEIGHT());
		this.upgradeBoost = upgradeBoost;
	}

	public static int HEIGHT() {
		return 92 + 12 + 16*((TECHS.COSTS().size()-1)/2);
	}

	public void addEdge(Node parent, Edge e, int mm) {
		parents.add(parent);
		for (TupleImp<Edge, Integer> ee : edges) {
			if (ee.a() == e) {
				ee.b = ee.b | mm;
				return;
			}
		}
		edges.add(new TupleImp<Edge, Integer>(e, mm));

	}

	public void hover() {
		for (Tuple<Edge, Integer> e : edges) {
			e.a().hover(e.b());
		}
		for (Node n : parents)
			n.hover();
		hoverI = VIEW.renI + 1;
	}

	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {

		isSelected |= FACTIONS.player().tech.level(tech) == tech.levelMax;

		{
			double levels = tech.levelMax;
			int level = FACTIONS.player().tech.level(tech);

			if (level == levels)
				rim.set(Cfinished);
			else
				rim.set(COLOR.WHITE65);

			for (int i = 1; i <= 6; i++) {
				ColorImp.TMP.interpolate(GCOLOR.UI().bg(), rim, 1.0-i/6.0);
				ColorImp.TMP.renderFrame(r, body, i, 1);
			}


			double d = level / levels;
			int y1 = (int) (body.y2()-body.height()*d);

			if (d > 0) {
				for (int i = 6; i >= 1; i--) {
					ColorImp.TMP.interpolate(GCOLOR.UI().bg(), Cfinished, 1.0-i/6.0);
					ColorImp.TMP.render(r,  body.x1()-i,  body.x2()+i, y1, body.y2()+i);
				}
			}
		}




		isHovered |= hoverI == VIEW.renI;

		GCOLOR.UI().bg(isActive, false, isHovered).render(r, body);
		if (isHovered) {
//        COLOR col = col(isHovered, isSelected);
//        col.render(r, body, -4);
			//////////////////////////////////////////////////////////////////////////////// #!#
//        tech.Tech_CostBenefit.update(tech); // #!# Update tech's Cost Benefits
			COLOR col = tech.Tech_CostBenefit.col(isHovered, tech); // MODIFIED Color change function
			col.render(r, body,-4);
			///////////////////////////////////////////////////////////////////////////////// #!#
			GCOLOR.UI().bg(isActive, false, isHovered).render(r, body, -7);
		}


		tech.icon().renderC(r, body.cX(), body.cY()-18);
		Str.TMP.clear();

		{

			int x = 0;
			int y = 0;


			for (TechCurrency cu : TECHS.COSTS()) {
				Str.TMP.clear();


				int wi = ((body.width()-16)/2);
				int cx = body.cX()+(x == 0 ? -1 : 1)*wi/2;
				int cy = body.y2()-8-16*y-8;

				boolean has = false;
				for (TechCost c : tech.costs) {
					if (c.cu == cu) {

						int l = Math.min(FACTIONS.player().tech.level(tech)+1, tech.levelMax);
						int am = FACTIONS.player().tech.costLevel(c.amount, tech, l);
						if (am > 0) {
							Str.TMP.add(am);
							has = true;

						}
					}

				}

				if (has) {
					(has ? OPACITY.O50 : OPACITY.O25).bind();
					cols.getC(cu.index).render(r, cx-wi/2, cx+wi/2, cy-8, cy+8);
					OPACITY.unbind();

					UI.FONT().S.renderC(r, cx, cy, Str.TMP, 1);
				}




				x++;
				if (x > 1) {
					x = 0;
					y++;
				}

			}
			OPACITY.unbind();


		}



//		for (TechCost c : tech.costs) {
//			int ci = c.cu.index;
//			int l = Math.min(FACTIONS.player().tech.level(tech)+1, tech.levelMax);
//			int am = FACTIONS.player().tech.costLevel(c.amount, tech, l);
//			if (am > 0) {
//				DIR dd = dirs.getC(ci);
//				Str.TMP.add(am);
//				int w = UI.FONT().S.width(Str.TMP);
//				int dw = (body.width()-16-w)/2;
//				int dh = (body.height()-16-UI.FONT().S.height())/2;
//				cols.getC(ci).bind();
//				UI.FONT().S.renderC(r, body.cX()+dd.x()*dw, body.cY()+dd.y()*dh, Str.TMP, 1);
//			}
//		}

//		cols.getC(ci).bind();
//		UI.FONT().S.renderCX(r, body.cX(), body.y2()-22, Str.TMP, 1);
		COLOR.unbind();

		if (!isSelected) {
			if (!FACTIONS.player().tech.canUnlockNext(tech)) {
				(FACTIONS.player().tech.level(tech) > 0 ? OPACITY.O35 : OPACITY.O66).bind();
				COLOR.BLACK.render(r, body, 0);
				OPACITY.unbind();
			}
		}

	}

	private COLOR col(boolean hovered, boolean selected) {

		if (hovered)
			return Chovered;
		if (selected)
			return Cfinished;
		return Cdormant;
	}

	@Override
	public boolean hover(COORDINATE mCoo) {
		if (super.hover(mCoo)) {
			Node.this.hover();
			hoverInfoGet(VIEW.hoverBox());
			return true;
		}
		return false;
	}



	@Override
	public void hoverInfoGet(GUI_BOX text) {
		GBox b = (GBox) text;
		text.title(tech.name());
		text.text(tech.desc());
		b.NL();
		PTech t = FACTIONS.player().tech();

		if (t.level(tech) == tech.levelMax){
			b.add(b.text().normalify2().add(¤¤unlocked));

		}else if (!tech.requires.passes(FACTIONS.player()))
			b.add(b.text().errorify().add(¤¤locked));
		else if (!t.canAffordNext(tech))
			b.add(b.text().errorify().add(¤¤afford));
		else if (t.canUnlockNext(tech)){
			b.add(b.text().warnify().add(¤¤available));
		}else{
			b.add(b.text().errorify().add(Dic.¤¤Access));
		}
		b.NL();

		{

			b.sep();

			if (tech.levelMax == 1) {

			} else {
				b.textLL(Dic.¤¤Level);
				b.add(GFORMAT.iofkNoColor(b.text(), t.level(tech), tech.levelMax));
				b.NL(8);
			}


			b.tab(7);
			b.textLL(Dic.¤¤Cost);
			b.tab(10);
			b.textLL(Dic.¤¤Allocated);
			b.NL();

			for (TechCost c : tech.costs) {

				b.add(c.cu.bo.icon);
				b.textL(c.cu.bo.name, 6);

				b.tab(7);


				int cost = t.costLevelNext(c.amount, tech);

				if (t.level(tech) >= tech.levelMax) {
					b.add(b.text().add('-'));
				}
				else if (t.currs().get(c.cu.index).available() < cost)
					b.add(GFORMAT.iBig(b.text(), cost).errorify());
				else
					b.add(GFORMAT.iBig(b.text(), cost));

				b.tab(10);
				b.add(GFORMAT.iBig(b.text(), t.costTotal(c, tech)));

				b.NL();
			}
			b.sep();
		}

		{
			LIST<TechRequirement> rr = tech.requires();

			int am = 0;
			for (TechRequirement r : rr)
				if (r.level > 0)
					am++;

			tech.requires.hover(text, FACTIONS.player());

			if (am > 0) {
				if (tech.requires.all().size() == 0)
					b.textLL(Dic.¤¤Requires);
				b.NL();
				for (TechRequirement r : rr) {
					if (r.level <= 0)
						continue;
					b.add(UI.icons().s.vial);
					GText te = b.text();
					te.add(r.tech.tree.name);
					te.add(':').s();
					te.add(r.tech.name());
					if (r.tech.levelMax > 1) {
						te.s().add(GFORMAT.toNumeral(r.level));
					}
					if (t.level(r.tech) >= r.level && r.tech.requires.passes(FACTIONS.player()))
						te.normalify2();
					else
						te.errorify();
					b.add(te);
					b.NL();

				}
			}
		}
		b.NL(8);



		tech.lockers.hover(text);


		boolean totHas = false;
		double tot = 0;
		for (Lock<?> l : tech.lockers.all()) {
			if (upgradeBoost.upgradeBoost.containsKey(l.lockable.key)) {
				totHas = true;
				upEntry am = upgradeBoost.upgradeBoost.get(l.lockable.key);
				tot += boostValue(am.blue, am.bo, am.value, false);
			}
		}

		b.NL(8);

		if (tech.boosters.all().size() > 0) {
			b.textLL(Dic.¤¤Effects);
			b.tab(7);
			b.textLL(Dic.¤¤Current);
			b.tab(9);
			b.textLL(Dic.¤¤Next);
			b.NL();

			for (BoostSpec bb : tech.boosters.all()) {
				b.add(bb.boostable.cat.icon);
				b.add(bb.boostable.icon);
				b.text(bb.boostable.name, 22);
				b.tab(7);
				double v = bb.booster.to();
				if (bb.booster.isMul)
					v -= 1;
				v *= t.level(tech);
				if (bb.booster.isMul)
					v += 1;
				b.add(bb.booster.format(b.text(), v));

				if (t.level(tech) < tech.levelMax) {
					v = bb.booster.to();
					if (bb.booster.isMul)
						v -= 1;
					v *= t.level(tech) + 1;
					if (bb.booster.isMul)
						v += 1;

					b.tab(9);
					b.add(bb.booster.format(b.text(), v));

					double tt = boostValue(bb);
					if (tt >= 0) {
						tot += tt;
						totHas = true;
					}
				}

				b.NL();
			}
			totHas = true;
			b.NL(4);
		}

		if (totHas || tot > 0) {
			b.sep();
			GText tt = b.text();
			tt.add(tot < 1 ? ¤¤workValueBad : ¤¤workValueGood).insert(0, tot, 1);
			if (tot < 1)
				tt.errorify();
			int cost = 0;
			for (TechCost c : tech.costs)
				cost += t.costLevelNext(c.amount, tech);
			tt.insert(1, tot/cost, 2);
			b.add(tt);
			b.sep();
		}


		b.NL();


		b.NL();

		if (t.level(tech) > 0) {
			GText te = b.text();
			te.add(¤¤Relock);
			te.insert(0, KEYS.MAIN().UNDO.repr());
			b.error(te);
			b.NL();

			for (TechCost c : tech.costs) {

				b.add(c.cu.bo.icon);
				b.textL(c.cu.bo.name);
				b.tab(7);
				b.add(GFORMAT.iIncr(b.text(), t.costLevel(c.amount, tech, t.level(tech))));
				b.NL();

			}
		}
		/////////////////////////////////////#!#
		// Reset game time clock if you push shift
		if (KEYS.MAIN().UNDO.isPressed()){ tech.Tech_CostBenefit.CUR_TIME = -1; }
		tech.Tech_CostBenefit.update(tech); // #!# Update tech's Cost Benefits
		tech.Extra.output(tech, b);
		/////////////////////////////////////#!#
	}

	private double boostValue(BoostSpec bb) {
		tEntry e = upgradeBoost.tools.get(bb.boostable.key);
		if (e != null) {
			double max = e.value.maxAm;
			double am =  e.value.boosts.all().get(0).booster.to()*bb.booster.to()/max;

			return boostValue(e.blue, e.bo, am, false);
		}


		RoomBlueprintImp r = SETT.ROOMS().bonus.get(bb.boostable);
		if (r == null)
			return -1;

		if (!(r instanceof INDUSTRY_HASER))
			return -1;

		if (((INDUSTRY_HASER)r).industries().get(0).outs().size() == 0)
			return -1;

		double res = boostValue(r, bb.boostable, bb.booster.to(), bb.booster.isMul);
		return res;


	}

	private double boostValue(RoomBlueprintImp r, Boostable bo, double increase, boolean isMul) {





		double employees = r.employment().employed();
		double current = bo.get(HCLASS_RACE.clP());
		double next = current;
		if (isMul) {
			next = BUtil.value(bo.all(), HCLASS_RACE.clP(), bo.baseValue, increase, bo.minValue);
		}else {
			next = BUtil.value(bo.all(), HCLASS_RACE.clP(), bo.baseValue + increase, 1, bo.minValue);
		}

		double res = employees*(next-current)/current;


		return res;


	}



	@Override
	protected void clickA() {
		if (KEYS.MAIN().UNDO.isPressed())
			VIEW.UI().tech.prompt.forget(tech);
		else
			VIEW.UI().tech.prompt.unlock(tech);
		super.clickA();
	}

}