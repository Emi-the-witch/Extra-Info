package view.sett.ui.room;

import java.util.Arrays;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.time.TIME;
import game.time.TIMECYCLE;
import util.GUTIL;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import util.text.D;
import settlement.main.SETT;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.util.RESOURCE_TILE;
import settlement.overlay.Addable;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.industry.module.ROOM_PRODUCER_INSTANCE;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEmploymentIns;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.text.Str;
import util.data.GETTER;
import util.text.Dic;
import util.text.DicTime;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GChart;
import util.gui.misc.GGrid;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.statistics.HISTORY_INT;
import view.main.VIEW;
import view.sett.ui.room.Modules.ModuleMaker;

import static view.sett.ui.room.ProfitCalc.*;

final class ModuleIndustry implements ModuleMaker {

	private final GChart chart = new GChart();
	private final boolean[] rCheck = new boolean[RESOURCES.ALL().size()];
	private final boolean[] rHas = new boolean[RESOURCES.ALL().size()];
	private static CharSequence ¤¤Production = "¤Production";
	private static CharSequence ¤¤ProductionDesc = "¤The amount that is estimated to be produced each day. The actual production can vary greatly depending on a number of factors.";
	private static CharSequence ¤¤Consumption = "¤Consumption";
	private static CharSequence ¤¤ConsumptionD = "¤Estimation of how many resources are consumed each day.";
	private static CharSequence ¤¤Recipes = "¤Change Recipe";
	private static CharSequence ¤¤RecipesWarning = "¤Note that changing recipe will reset the room.";

	private static CharSequence ¤¤ProducedDay = "¤Produced today";
	private static CharSequence ¤¤ProducedYesterDay = "¤Produced yesterday";
	private static CharSequence ¤¤ProducedNow = "¤Produced This Year";
	private static CharSequence ¤¤ProducedEstimate = "¤Estimated this year";
	private static CharSequence ¤¤ProducedPrevious = "¤Produced last year";

	private static CharSequence ¤¤ConsumedDay = "¤Consumed today";
	private static CharSequence ¤¤ConsumedNow = "¤Consumed This Year";
	private static CharSequence ¤¤ConsumedYEsterday = "¤Consumed Yesterday";
	private static CharSequence ¤¤ConsumedPrevious = "¤Consumed last year";

	private static CharSequence ¤¤FetchWarning = "¤Proximity to raw materials is poor.";

	private static CharSequence ¤¤NoStore = "¤Internal storage is full and production is stalled. Have a warehouse fetch the produce.";

	//////////////////////////#!#/
	private static CharSequence Profit1 = "Yesterday's Profit";
	private static CharSequence Profit2 = "Yesterday's Value added";
	private static CharSequence Profit3 = "Profit per person";
	/////////////////////////////////



	private final InputOverlay overlay = new InputOverlay();

	public ModuleIndustry(Init init) {
		D.t(this);

	}

	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		if (p instanceof INDUSTRY_HASER) {
			INDUSTRY_HASER blue = ((INDUSTRY_HASER) p);
			if (!blue.industryIgnoreUI())
				l.add(new I((RoomBlueprintIns<?>) p, blue.industries()));
		}

	}

	private final class I extends UIRoomModule {

		private final LIST<Industry> ins;
		private final INDUSTRY_HASER indu;
		private final RBITImp m = new RBITImp();
		I(RoomBlueprintIns<?> blue, LIST<Industry> ins) {
			this.ins = ins;
			indu = (INDUSTRY_HASER) blue;
			// this.blue = blue;
		}

		@Override
		public void appendManageScr(GGrid grid, GGrid r, GuiSection sExta) {

			int y1 = r.section.body().y2() + 32;

			LinkedList<RESOURCE> ress = new LinkedList<RESOURCE>();
			m.clear();
			for (Industry i : ins) {
				for (IndustryResource res : i.outs()) {
					if (!m.has(res.resource)) {
						ress.add(res.resource);
						m.or(res.resource);
					}
				}

			}

			if (ress.size() > 0) {

				r.section.add(new GHeader(¤¤Production), r.section.body().x1(), y1);
				GuiSection ins = new GuiSection();

				int ri = 0;
				for (RESOURCE res : ress) {
					IndustryResource[] os = resourcesOut(res);
					if (os == null)
						continue;
					GStat s = new GStat() {
						@Override
						public void update(GText text) {
							int am = 0;
							for (IndustryResource r : os)
								am += r.history().getPeriodSum(-(int)TIME.years().bitConversion(TIME.days()), 0);
							GFORMAT.iIncr(text, am);
						}
					};
					HISTORY_INT hi = new HISTORY_INT() {

						@Override
						public TIMECYCLE time() {
							return os[0].history().time();
						}

						@Override
						public int historyRecords() {
							return os[0].history().historyRecords();
						}

						@Override
						public double getD(int fromZero) {
							return get(fromZero) / (double) max();
						}

						@Override
						public int min() {
							return os[0].history().min();
						}

						@Override
						public int max() {
							return os[0].history().max();
						}

						@Override
						public int get(int fromZero) {
							int am = 0;
							for (IndustryResource i : os)
								am += i.history().get(fromZero);
							return am;
						}
					};

					ins.add(new GHeader.HeaderHorizontal(res.icon(), s) {
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							GBox b = (GBox) text;

							b.title(res.name);
							b.add(text.text().add(¤¤Production).s().add('(').add(Dic.¤¤Total).add(')'));
							b.NL(4);

							b.textLL(¤¤ProducedDay);
							b.tab(7);
							b.add(GFORMAT.i(b.text(), hi.get(0)));
							b.NL();

							b.textLL(¤¤ProducedYesterDay);
							b.tab(7);
							b.add(GFORMAT.i(b.text(), hi.get(1)));
							b.NL();

							int yearStart = (int) (-TIME.days().bitOfYear()*TIME.years().bitConversion(TIME.years()));

							b.textLL(¤¤ProducedNow);
							b.tab(7);
							b.add(GFORMAT.i(b.text(), hi.getPeriodSum(yearStart, 0)));
							b.NL();

							b.textLL(¤¤ProducedPrevious);
							b.tab(7);
							b.add(GFORMAT.i(b.text(), hi.getPeriodSum(-yearStart-(int)TIME.years().bitConversion(TIME.days()), -yearStart)));
							b.NL();

							b.NL(8);
							b.textLL(DicTime.¤¤Days);
							chart.clear();
							chart.add(hi);
							text.NL();
							text.add(chart.sprite);
						}
					}, (ri % 3) * 96, (ri / 3) * 26);
					ri++;
				}

				r.section.addDown(2, ins);
				y1 = r.section.body().y2() + 4;
			}

			ress = new LinkedList<RESOURCE>();
			m.clear();
			for (Industry i : ins) {
				for (IndustryResource res : i.ins()) {
					if (!m.has(res.resource)) {
						ress.add(res.resource);
						m.or(res.resource);
					}
				}

			}


			if (ress.size() > 0) {


				GuiSection ins = new GuiSection();

				int ri = 0;
				for (RESOURCE res : ress) {
					IndustryResource[] os = resourcesIn(res);
					if (os == null)
						continue;

					HISTORY_INT hi = new HISTORY_INT() {

						@Override
						public TIMECYCLE time() {
							return os[0].history().time();
						}

						@Override
						public int historyRecords() {
							return os[0].history().historyRecords();
						}

						@Override
						public double getD(int fromZero) {
							return get(fromZero) / (double) max();
						}

						@Override
						public int min() {
							return os[0].history().min();
						}

						@Override
						public int max() {
							return os[0].history().max();
						}

						@Override
						public int get(int fromZero) {
							int am = 0;
							for (IndustryResource i : os)
								am += i.history().get(fromZero);
							return am;
						}
					};

					GStat s = new GStat() {
						@Override
						public void update(GText text) {
							int am = hi.getPeriodSum(-(int)TIME.years().bitConversion(TIME.days()), 0);
							GFORMAT.iIncr(text, -am);
						}
					};

					ins.add(new GHeader.HeaderHorizontal(res.icon(), s) {
						@Override
						public void hoverInfoGet(GUI_BOX text) {

							GBox b = (GBox) text;

							b.title(res.name);
							b.add(text.text().add(¤¤Consumption).s().add('(').add(Dic.¤¤Total).add(')'));
							b.NL(4);

							b.textLL(¤¤ConsumedDay);
							b.tab(7);
							b.add(GFORMAT.i(b.text(), hi.get(0)));
							b.NL();

							b.textLL(¤¤ConsumedYEsterday);
							b.tab(7);
							b.add(GFORMAT.i(b.text(), hi.get(1)));
							b.NL();

							int yearStart = (int) (-TIME.days().bitOfYear()*TIME.years().bitConversion(TIME.years()));

							b.textLL(¤¤ConsumedNow);
							b.tab(7);
							b.add(GFORMAT.i(b.text(), hi.getPeriodSum(-yearStart, 0)));
							b.NL();



							b.textLL(¤¤ConsumedPrevious);
							b.tab(7);
							b.add(GFORMAT.i(b.text(), hi.getPeriodSum(-(int)TIME.years().bitConversion(TIME.days())-yearStart, -(int)yearStart)));
							b.NL();

							b.NL(8);
							b.textLL(DicTime.¤¤Days);
							chart.clear();
							chart.add(hi);
							text.NL();
							text.add(chart.sprite);

						}
					}, (ri % 3) * 90, (ri / 3) * 26);
					ri++;
				}



				if (ress.size() == 1) {
					GuiSection fuckifuck = new GuiSection();
					fuckifuck.add(new GHeader(¤¤Consumption));
					fuckifuck.addRightC(8, ins);
					r.section.add(fuckifuck, r.section.body().x1(), y1);
				}else {
					r.section.add(new GHeader(¤¤Consumption), r.section.body().x1(), y1);
					r.section.addDown(2, ins);
				}

				y1 = r.section.body().y2() + 4;
			}

		}

		private IndustryResource[] resourcesOut(RESOURCE res) {
			int am = 0;
			for (Industry i : ins) {
				for (IndustryResource r : i.outs())
					if (r.resource == res) {
						am++;
					}
			}
			if (am == 0)
				return null;
			IndustryResource[] o = new IndustryResource[am];
			am = 0;
			for (Industry i : ins) {
				for (IndustryResource r : i.outs())
					if (r.resource == res) {
						o[am++] = r;
					}
			}
			return o;
		}

		private IndustryResource[] resourcesIn(RESOURCE res) {
			int am = 0;
			for (Industry i : ins) {
				for (IndustryResource r : i.ins())
					if (r.resource == res) {
						am++;
					}
			}
			if (am == 0)
				return null;
			IndustryResource[] o = new IndustryResource[am];
			am = 0;
			for (Industry i : ins) {
				for (IndustryResource r : i.ins())
					if (r.resource == res) {
						o[am++] = r;
					}
			}
			return o;
		}

		@Override
		public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters,
									   LISTE<GTSort<RoomInstance>> sorts, LISTE<UIRoomBulkApplier> appliers) {

		}

		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			ROOM_PRODUCER_INSTANCE p = ((ROOM_PRODUCER_INSTANCE) room);
			box.NL();
			int t = 0;
			for (IndustryResource i : p.industry().outs()) {

				box.tab(t * 3);
				box.add(i.resource.icon().small);
				GText te = box.text();
				indu.industryFormatProductionRate(te, i, (RoomInstance) room);
				box.add(te);
				t++;
				if (t == 3) {
					t = 0;
					box.NL();
				}
			}

			box.NL(8);

			for (IndustryResource i : p.industry().ins()) {

				box.tab(t * 3);
				box.add(i.resource.icon().small);
				GText te = box.text();
				indu.industryFormatConsumptionRate(te, i, (RoomInstance) room);
				box.add(te);
				t++;
				if (t == 3) {
					t = 0;
					box.NL();
				}
			}

			box.NL(8);

		}

		@Override
		public void problem(Stack<Str> free, LISTE<CharSequence> errors, LISTE<CharSequence> warnings, Room rr, int rx, int ry) {
			ROOM_PRODUCER_INSTANCE p = ((ROOM_PRODUCER_INSTANCE) rr);
			if (p.industry().outs().size() == 0)
				return;

			RoomInstance room = (RoomInstance) rr;

			Arrays.fill(rCheck, false);
			Arrays.fill(rHas, false);
			for (COORDINATE c : room.body()) {
				if (room.is(c)) {
					RESOURCE_TILE t = room.resourceTile(c.x(), c.y());
					if (t != null && t.resource() != null) {
						rCheck[t.resource().index()] = true;
						if (t.hasRoom())
							rHas[t.resource().index()] = true;
					}
				}
			}

			boolean title = false;


			for (RESOURCE r : RESOURCES.ALL()) {
				if (rCheck[r.index()] && !rHas[r.index()]) {
					if (!title) {
						title = true;
						errors.add(¤¤NoStore);
					}
					errors.add(free.pop().s(4).add(r.name));
				}
			}

			if (rr.blueprint().employment().countInput()) {
				if (room.employees().fetchProximity() < 0.8)
					warnings.add(¤¤FetchWarning);
			}
		}


//		@Override
//		public void problem(GBox box, Room rr, int rx, int ry) {
//			ROOM_PRODUCER_INSTANCE p = ((ROOM_PRODUCER_INSTANCE) rr);
//			if (p.industry().outs().size() == 0)
//				return;
//
//			RoomInstance room = (RoomInstance) rr;
//
//			Arrays.fill(rCheck, false);
//			Arrays.fill(rHas, false);
//			for (COORDINATE c : room.body()) {
//				if (room.is(c)) {
//					RESOURCE_TILE t = room.resourceTile(c.x(), c.y());
//					if (t != null && t.resource() != null) {
//						rCheck[t.resource().index()] = true;
//						if (t.hasRoom())
//							rHas[t.resource().index()] = true;
//					}
//				}
//			}
//
//			boolean title = false;
//
//
//			for (RESOURCE r : RESOURCES.ALL()) {
//				if (rCheck[r.index()] && !rHas[r.index()]) {
//					if (!title) {
//						title = true;
//						box.NL(8);
//						box.add(box.text().errorify().add(¤¤NoStore));
//						box.NL();
//					}
//
//					box.add(r.icon());
//				}
//			}
//		}

		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {

			int resOut = 0;

			for (Industry i : ins)
				resOut = Math.max(resOut, i.outs().size());

			if (resOut > 0) {

				GuiSection all = new GuiSection();

				for (int rii = 0; rii < resOut; rii++) {
					RENDEROBJ s = resOut(rii, get, indu);
					s.body().moveX1Y1((rii % 3) * 90, (rii / 3) * (s.body().height()));
					all.add(s);

				}

				all.addRelBody(2, DIR.N, new GHeader(¤¤Production));

				section.addRelBody(32, DIR.S, all);

			}

			int resIn = 0;

			for (Industry i : ins)
				resIn = Math.max(resIn, i.ins().size());

			if (resIn > 0) {
				boolean ccc = resOut > 0;
				GuiSection all = new GuiSection();

				for (int rii = 0; rii < resIn; rii++) {
					RENDEROBJ s = resIn(rii, get, indu, ccc);
					s.body().moveX1Y1((rii % 3) * 90, (rii / 3) * (s.body().height()));
					all.add(s);
				}

				RENDEROBJ in = new GStat() {

					@Override
					public void update(GText text) {
						GFORMAT.perc(text, get.get().employees().fetchProximity());
					}

					@Override
					public void hoverInfoGet(GBox b) {
						overlay.ins = get.get();
						overlay.add();
						b.title(RoomEmploymentIns.¤¤ProximityInput);
						b.text(RoomEmploymentIns.¤¤¤¤ProximityInputD);
						b.NL();
						b.textL(DicTime.¤¤Today);
						b.add(GFORMAT.perc(b.text(), get.get().employees().fetchProximitySoFar()));
					};

				}.hh(UI.icons().s.clock);

				GuiSection s = new GuiSection();
				s.add(new GHeader(¤¤Consumption));
				if (((RoomBlueprint)indu).employment().countInput())
					s.addRightC(8, in);

				all.addRelBody(2, DIR.N, s);

				section.addRelBody(4, DIR.S, all);
			}







			/////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#

			// #!# Add profit panel1
			{
				GuiSection all = new GuiSection() {
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						refresh(get);

						b.textLL("Profit from buying inputs and selling outputs");
						b.NL();
						b.add(GFORMAT.text(b.text(), "Revenue"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) revenue));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Input costs"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) inputs));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Maintenance"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) maintenance));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Tools"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) tools));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Profit"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) profit1));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Profit per employee"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) ppp1));
						b.NL();
					}
				};
				{
					RENDEROBJ s = profit1_display(get);
					all.addDownC(2, s);
				}
				all.addRelBody(2, DIR.N, new GHeader(Profit1));

				section.addRelBody(4, DIR.S, all);
			}
			// #!# Add profit panel 2
			{
				GuiSection all = new GuiSection(){
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						refresh(get);

						b.textLL("Value added by making things for yourself");
						b.NL();

						b.add(GFORMAT.text(b.text(), "Denari saved by not importing"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) saved));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Input costs"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) inputs));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Maintenance"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) maintenance));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Tools"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) tools));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Value Added"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) profit2));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Profit per employee"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) ppp2));
						b.NL();
					}
				};
				{
					RENDEROBJ s = profit2_display(get);
					all.addDownC(2, s);
				}
				all.addRelBody(2, DIR.N, new GHeader(Profit2));

				section.addRelBody(4, DIR.S, all);
			}

			// #!# Add profit panel 3
			{
				GuiSection all = new GuiSection(){
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						refresh(get);

						b.textLL("Estimate of actual profit based on 'for self' and 'for sale' weighted average." );
						b.NL();
						b.textLL("Assumes you trade for everything this industry doesn't make!");
						b.NL();

						b.add(GFORMAT.text(b.text(), "Percent of value used for self."));
						b.tab(7);
						b.add(GFORMAT.text(b.text(), (int) (percent_for_self*100)+ "%"));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Weighted average revenue"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) weighted_average));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Input costs"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) inputs));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Maintenance"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) maintenance));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Tools"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) tools));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Estimated profit"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) profit3));
						b.NL();

						b.add(GFORMAT.text(b.text(), "Profit per employee"));
						b.tab(7);
						b.add(GFORMAT.iIncr(b.text(), (long) ppp3));
						b.NL();
					}
				};
				{
					RENDEROBJ s = profit3_display(get);
					all.addDownC(2, s);
				}
				all.addRelBody(2, DIR.N, new GHeader(Profit3));

				section.addRelBody(4, DIR.S, all);
			}
			// End of profit panel
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
















			if (ins.size() <= 1)
				return;

			{
				LinkedList<RENDEROBJ> rows = new LinkedList<>();
				for (int ii = 0; ii < ins.size(); ii++) {

					final int k = ii;
					final Industry i = ins.get(k);
					GButt.BSection b = new GButt.BSection() {

						@Override
						protected void clickA() {
							if (i.lockable().passes(FACTIONS.player())) {
								ROOM_PRODUCER_INSTANCE p = ((ROOM_PRODUCER_INSTANCE) g(get));
								p.setIndustry(k);
								VIEW.inters().popup.close();
								if (g(get) instanceof JOBMANAGER_HASER) {
									JOBMANAGER_HASER j = (JOBMANAGER_HASER) g(get);
									j.getWork().resetResourceSearch();
								}
							}
							super.clickA();
						}

						@Override
						public void hoverInfoGet(GUI_BOX text) {
							GBox b = (GBox) text;

							if (activeIs()) {
								b.error(¤¤RecipesWarning);
								b.NL(8);
							}

							b.textL(¤¤Consumption);
							b.NL();
							for (IndustryResource r : i.ins())
								b.text(r.resource.name);
							b.NL(8);
							b.textL(¤¤Production);
							b.NL();
							for (IndustryResource r : i.outs())
								b.text(r.resource.name);
							b.NL(8);

							i.lockable().hover(text, FACTIONS.player());

						}

						@Override
						public void renAction() {
							activeSet(i.lockable().passes(FACTIONS.player()));
						}

					};

					for (IndustryResource r : i.ins()) {
						b.addRightC(48, new GStat() {

							@Override
							public void update(GText text) {
								GFORMAT.f0(text, -r.rate);

							}
						}.hh(r.resource.icon()));
					}

					b.addRightC(48, SPRITES.icons().m.arrow_right);

					for (IndustryResource r : i.outs()) {
						b.addRightC(48, new GStat() {

							@Override
							public void update(GText text) {
								GFORMAT.f0(text, r.rate);

							}
						}.hh(r.resource.icon()));
					}

					b.body().incrW(48);
					b.pad(4);
					b.body().setWidth(450);

					rows.add(b);

				}

				CLICKABLE rr = new GScrollRows(rows, 400).view();
				CLICKABLE c = new GButt.ButtPanel(¤¤Recipes) {
					@Override
					protected void clickA() {
						VIEW.inters().popup.show(rr, this);
					};
				}.pad(6, 3);

				section.addRelBody(8, DIR.S, c);

			}

		}

		private ROOM_PRODUCER_INSTANCE g(GETTER<RoomInstance> g) {
			return (ROOM_PRODUCER_INSTANCE) g.get();
		}

	}
	////////////////////////////// MAKE IT PUBLIC PLEASE
	public static ROOM_PRODUCER_INSTANCE g(GETTER<RoomInstance> g) {
		return (ROOM_PRODUCER_INSTANCE) g.get();
	}
	/// /////////////////////////////
	private static RENDEROBJ resIn(int ri, GETTER<RoomInstance> get, INDUSTRY_HASER indu, boolean outs) {
		GuiSection s = new GuiSection() {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				ROOM_PRODUCER_INSTANCE p = ((ROOM_PRODUCER_INSTANCE) g(get));
				if (ri >= p.industry().ins().size())
					return;

				IndustryResource i = p.industry().ins().get(ri);

				GBox b = (GBox) text;
				b.title(i.resource.name);



				if (outs) {
					b.text(¤¤ConsumptionD);
					b.NL(8);
					IndustryUtil.hoverConsumptionRate(text, i.rate, p.industry(), (RoomInstance) get.get(), i.resource);
				}
				b.NL(8);
				b.textLL(¤¤ConsumedNow);
				b.tab(7);
				b.add(GFORMAT.i(b.text(), i.year.get(p)));
				b.NL();

				b.textLL(¤¤ConsumedPrevious);
				b.tab(7);
				b.add(GFORMAT.i(b.text(), i.yearPrev.get(p)));

				b.NL(8);
			}

			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				ROOM_PRODUCER_INSTANCE p = ((ROOM_PRODUCER_INSTANCE) g(get));
				visableSet(p.industry().ins().size() > ri);
				if (visableIs())
					super.render(r, ds);
			}
		};
		s.add(new RENDEROBJ.RenderImp(Icon.M) {

			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				ROOM_PRODUCER_INSTANCE p = ((ROOM_PRODUCER_INSTANCE) g(get));
				IndustryResource i = p.industry().ins().get(ri);
				i.resource.icon().render(r, body);
			}
		});

		HOVERABLE h = new GStat() {

			@Override
			public void update(GText text) {
				IndustryResource i = ((ROOM_PRODUCER_INSTANCE) g(get)).industry().ins().get(ri);
				RoomInstance ins = (RoomInstance) get.get();
				indu.industryFormatConsumptionRate(text, i, ins);
			}
		}.r();

		s.addRightC(6, h);
		s.body().incrW(48);
		s.pad(4);
		return s;
	}

	private static RENDEROBJ resOut(int ri, GETTER<RoomInstance> get, INDUSTRY_HASER indu) {

		GuiSection s = new GuiSection() {

			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				ROOM_PRODUCER_INSTANCE p = ((ROOM_PRODUCER_INSTANCE) g(get));
				visableSet(p.industry().outs().size() > ri);
				if (visableIs())
					super.render(r, ds);
			}

			@Override
			public void hoverInfoGet(GUI_BOX text) {

				ROOM_PRODUCER_INSTANCE p = ((ROOM_PRODUCER_INSTANCE) g(get));
				if (ri >= p.industry().outs().size())
					return;

				RoomInstance ins = (RoomInstance) get.get();

				IndustryResource i = p.industry().outs().get(ri);

				GBox b = (GBox) text;
				b.title(i.resource.name);

				b.text(¤¤ProductionDesc);
				b.NL(8);

				indu.industryHoverProductionRate(b, i, ins);

				b.NL(8);

				b.textLL(¤¤ProducedNow);
				b.tab(7);
				b.add(GFORMAT.i(b.text(), i.year.get(p)));
				b.NL();

				b.textLL(¤¤ProducedPrevious);
				b.tab(7);
				b.add(GFORMAT.i(b.text(), i.yearPrev.get(p)));
				b.NL();


				double e = indu.industryFormatProductionRate(b.text(), i, ins);

				double pa = TIME.years().bitPartOf();

				int eyear = (int) (i.year.get(p) + e * (1.0 - pa) * TIME.years().bitConversion(TIME.days()));
				b.textLL(¤¤ProducedEstimate);
				b.tab(7);
				b.add(GFORMAT.i(b.text(), eyear));
				b.NL();

				b.NL(8);

			}

		};
		s.add(new RENDEROBJ.RenderImp(Icon.M) {

			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				ROOM_PRODUCER_INSTANCE p = ((ROOM_PRODUCER_INSTANCE) g(get));
				IndustryResource i = p.industry().outs().get(ri);
				i.resource.icon().render(r, body);
			}
		});

		HOVERABLE h = new GStat() {

			@Override
			public void update(GText text) {
				IndustryResource i = ((ROOM_PRODUCER_INSTANCE) g(get)).industry().outs().get(ri);
				RoomInstance ins = (RoomInstance) get.get();
				indu.industryFormatProductionRate(text, i, ins);
			}
		}.r();

		s.addRightC(6, h);

		h = new GStat() {

			@Override
			public void update(GText text) {
				IndustryResource i = ((ROOM_PRODUCER_INSTANCE) g(get)).industry().outs().get(ri);
				RoomInstance ins = (RoomInstance) get.get();
				indu.industryFormatProductionRateEmpl(text, i, ins);
			}
		}.r();

		s.add(h, s.getLast().x1(), s.getLastY2()+1);

		s.body().incrW(48);
		s.pad(4);
		return s;
	}

	private static class InputOverlay extends Addable {

		private RoomInstance ins;

		private final double max = RoomEmploymentIns.FETCH_FREE_SECONDS/(2.0);

		public InputOverlay() {
			super(true, false);
		}

		@Override
		public void initBelow(RenderData data) {
			Flooder f = GUTIL.flooder();
			f.init(this);
			double hi = 0;
			double ii = 1.0/BOOSTABLES.PHYSICS().SPEED.baseValue;

			f.pushSmaller(ins.mX(), ins.mY(), 0);
			while(f.hasMore()) {
				PathTile t = GUTIL.flooder().pollSmallest();
				hi = Math.max(hi, t.getValue());

				for (DIR d : DIR.ALL) {
					if (ins.is(t) && SETT.PATH().coster.player.getCost(t.x(), t.y(), t.x()+d.x(), t.y()+d.y()) > 0) {
						f.pushSmaller(t,d, t.getValue() + SETT.PATH().availability.get(t, d).movementSpeed*ii*d.tileDistance());
					}

				}

			}

			f.done();
			f.init(this);
			for (COORDINATE c : ins.body()) {
				if (ins.is(c))
					f.pushSmaller(c, hi);
			}

			while(f.hasMore()) {
				PathTile t = GUTIL.flooder().pollSmallest();
				if (t.getValue() >= max)
					break;
				for (DIR d : DIR.ALL) {
					if (SETT.PATH().coster.player.getCost(t.x(), t.y(), t.x()+d.x(), t.y()+d.y()) >= 0) {
						f.pushSmaller(t, d, t.getValue() + SETT.PATH().availability.get(t, d).movementSpeed*ii*d.tileDistance());
					}
				}
			}
		}

		@Override
		public void renderBelow(Renderer r, RenderIterator it) {
			double v = 0;
			if (GUTIL.flooder().hasBeenPushed(it.tx(), it.ty())) {
				double vv = GUTIL.flooder().getValue(it.tx(), it.ty())/max;
				v = 1.0 - vv;
				v = CLAMP.d(v*2.0, 0, 1);
			}
			renderUnder(v, r, it, false);
		}

		@Override
		public void finishBelow() {
			GUTIL.flooder().done();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
	//	/////////////////////////////////////////////#!# Profit calculation's output revenue in terms of selling
	private RENDEROBJ profit1_display(GETTER<RoomInstance> get) {
		GuiSection s = new GuiSection();


		HOVERABLE h = new GStat() {

			@Override
			public void update(GText text) {
				refresh(get);
				GFORMAT.iIncr(text, (int) profit1);
			}
		}.r();

		s.addRightC(6, h);
		s.body().incrW(48);
		s.pad(4);
		return s;
	}
	//	/////////////////////////////////////////////#!# Profit calculation's output revenue in terms of using it for yourself
	private RENDEROBJ profit2_display(GETTER<RoomInstance> get) {
		GuiSection s = new GuiSection();


		HOVERABLE h = new GStat() {

			@Override
			public void update(GText text) {
				refresh(get);
				GFORMAT.iIncr(text, (int) profit2);
			}
		}.r();

		s.addRightC(6, h);
		s.body().incrW(48);
		s.pad(4);
		return s;
	}
	//	/////////////////////////////////////////////#!# Profit per person weighed average on for sale vs for self
	private RENDEROBJ profit3_display(GETTER<RoomInstance> get) {
		GuiSection s = new GuiSection();


		HOVERABLE h = new GStat() {

			@Override
			public void update(GText text) {
				refresh(get);
				GFORMAT.iIncr(text, (int) ppp3);
			}
		}.r();

		s.addRightC(6, h);
		s.body().incrW(48);
		s.pad(4);
		return s;
	}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
}
