package settlement.room.food.farm;

import game.faction.FACTIONS;
import game.time.TIME;
import init.sprite.SPRITES;
import util.text.D;
import settlement.main.SETT;
import settlement.room.industry.module.IndustryResource;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.main.RoomInstance;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.data.GETTER;
import util.text.Dic;
import util.text.DicTime;
import util.gui.misc.*;
import util.gui.table.GStaples;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.main.VIEW;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

import static view.sett.ui.room.ProfitCalc.*;
import static view.sett.ui.room.ProfitCalc.ppp3;

class Gui extends UIRoomModuleImp<FarmInstance, ROOM_FARM> {

	private static CharSequence ¤¤estimated = "¤Estimated Harvest (year)";
	private static CharSequence ¤¤daysToHarvest = "¤Days until harvest";
	private static CharSequence ¤¤baseValue = "¤Base Value";
	private static CharSequence ¤¤workValue = "¤Work Value";
	private static CharSequence ¤¤workValueD = "¤Each day of the year a farm needs tending to. Workers failing to tend to all the tiles will lead to low yields.";
	
	private static CharSequence ¤¤HarvestYear = "¤This Year";
	private static CharSequence ¤¤HarvestPrev = "¤Last Year";
	
	private static CharSequence ¤¤skill = "¤Bonus";
	private static CharSequence ¤¤skillD = "¤The average bonus accumulated during the year. This determines the output of the harvest.";
	
	private static CharSequence ¤¤skillCurrent = "¤Bonus (current)";
	private static CharSequence ¤¤skillCurrentD = "¤The bonus that is currently being added to the Farm.";
	
	private static CharSequence ¤¤cycle = "¤Cycle";
	private static CharSequence ¤¤reseed = "¤reseed";
	private static CharSequence ¤¤reseedD = "¤Reseed the farm with another crop. New farm must still be constructed.";
	//////////////////////////#!#/
	private static CharSequence Profit1 = "Yesterday's Profit";
	private static CharSequence Profit2 = "Yesterday's Value added";
	private static CharSequence Profit3 = "Profit per person";
	/////////////////////////////////
	static {
		D.ts(Gui.class);
	}
	
	Gui(ROOM_FARM s) {
		super(s);
	}
	
	private GuiSection rebuilds = new GuiSection();
	
	@Override
	public void hover(GBox box, FarmInstance i) {
		super.hover(box, i);
		box.NL();
		box.text(blueprint.constructor.fertility.name());
		box.add(GFORMAT.perc(box.text(), blueprint.constructor.fertility.get(i)));
		
		box.NL();
		box.text(¤¤estimated);
		box.add(GFORMAT.i(box.text(), (int)Math.ceil(Util.prospect(i))));
		
		box.space();
	}
	
	@Override
	protected void appendMain(GGrid icons, GGrid text, GuiSection sExtra) {
		
		IndustryResource res = blueprint.industries().get(0).outs().get(0);
		
		text.add(new GHeader(Dic.¤¤Production));
		
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(¤¤estimated);
				
			};
			
		};
		
		s.add(res.resource.icon(), 0, 0);
		
		s.addRightC(4, new GStat() {
			
			@Override
			public void update(GText text) {
				int am = 0;
				for (int i = 0; i < blueprint.instancesSize(); i++) {
					FarmInstance ins = blueprint.getInstance(i);
					am += Util.prospect(ins);
				}
				GFORMAT.i(text, am);
			}
		});
		
		s.addRightC(64, new GStat() {
			
			@Override
			public void update(GText text) {
				int prev = 0;
				int am = 0;
				for (int i = 0; i < blueprint.instancesSize(); i++) {
					FarmInstance ins = blueprint.getInstance(i);
					prev += Util.prevHarvest(ins);
					am += Util.prospect(ins);
				}
				GFORMAT.iIncr(text, am-prev);
			}
		});
		
		text.add(s);
		
		GStaples st = new GStaples(res.history().historyRecords()) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				int i = res.history().historyRecords()-1-stapleI;
				int am = res.history().get(i);
				GText t = box.text();
				DicTime.setDaysAgo(t, i);
				box.add(t);
				box.NL(2);
				box.add(GFORMAT.i(box.text(), am));
			}
			
			@Override
			protected double getValue(int stapleI) {
				return res.history().get(res.history().historyRecords()-1-stapleI);
			}
		};
		

		st.body().setWidth(180).setHeight(64);
		text.add(st);
		
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<FarmInstance> getter, int x1, int y1) {
		
		GuiSection s = new GuiSection();
		
		s.add(prod(getter));
		
		RENDEROBJ o = new GStat() {
			@Override
			public void update(GText text) {
				DicTime.setDays(text, blueprint.time.daysToHarvest());
			}
		}.hh(SPRITES.icons().s.clock).hoverInfoSet(¤¤daysToHarvest);
		s.addRightC(32, o);
		s.body().incrW(64);
		
		s.addRelBody(4, DIR.N, new GHeader(Dic.¤¤Production));
		
		section.addRelBody(8, DIR.S, s);
		
		s = new GuiSection();
		
//		s.add(new GStat() {
//			
//			@Override
//			public void update(GText text) {
//				GFORMAT.perc(text, getter.get().tData.fertility());
//			}
//		}.hv(Fertility.¤¤name, blueprint.constructor.fertility.desc()));
			
		
		s.addRightC(32, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, getter.get().tData.work());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(¤¤workValue);
				b.text( ¤¤workValueD);
				b.NL();
				b.textLL(DicTime.¤¤Today);
				b.tab(4);
				b.add(GFORMAT.perc(b.text(), CLAMP.d(getter.get().tData.workday(), 0, 1)));
			};
			
		}.hv(¤¤workValue));
		
		s.addRightC(32, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f1(text, getter.get().tData.skill());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(¤¤skill);
				b.text(¤¤skillD);
				b.NL(8);
				b.textLL(Dic.¤¤Value);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), getter.get().tData.skill()));
				
				b.NL(4);
				b.textLL(¤¤skillCurrent);
				b.NL();
				b.text(¤¤skillCurrentD);
				b.NL();
				IndustryUtil.hoverBoosts(b, 1.0, getter.get().industry(), getter.get().industry().bonus(), getter.get());

			};
			
		}.hv(¤¤skill));
		
		section.addRelBody(16, DIR.S, s);
//////////////////////////////////////#!#
//		s = new GuiSection();
//		s.add()


		// #!# Add profit panel1
		{
			GuiSection all = new GuiSection() {
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					refresh2(getter, blueprint.industries().get(0).outs().get(0));

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
				RENDEROBJ s1 = profit1_display(getter);
				all.addDownC(2, s1);
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
					refresh2(getter, blueprint.industries().get(0).outs().get(0));

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
				RENDEROBJ s2 = profit2_display(getter);
				all.addDownC(2, s2);
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
					refresh2(getter, blueprint.industries().get(0).outs().get(0));

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
				RENDEROBJ s3 = profit3_display(getter);
				all.addDownC(2, s3);
			}
			all.addRelBody(2, DIR.N, new GHeader(Profit3));

			section.addRelBody(4, DIR.S, all);
		}
		// End of profit panel









//////////////////////////////////
		s = new GuiSection();

		
		s.addRightC(32, new GStat() {
			
			@Override
			public void update(GText text) {
				text.add(getter.get().tData.cName());
			}
		}.hv(¤¤cycle));
		section.addRelBody(4, DIR.S, s);
		
		ACTION rebuild = new ACTION() {
			@Override
			public void exe() {
				rebuilds = new GuiSection();
				int i = 0;
				for (ROOM_FARM f : SETT.ROOMS().FARMS) {
					if (f.isAvailable(SETT.ENV().climate())) {
						CLICKABLE ss = new GButt.ButtPanel(f.iconBig()) {
							
							@Override
							protected void clickA() {
								if (f.reqs.passes(FACTIONS.player()) && f != getter.get().blueprintI()) {
									VIEW.inters().popup.close();
									getter.get().changeTo(f);
									
								}
								
							};
							
							@Override
							protected void renAction() {
								activeSet(f.reqs.passes(FACTIONS.player()) && f != getter.get().blueprintI() && f.constructor.isIndoors == getter.get().blueprintI().constructor.isIndoors);
							};
							
						}.hoverSet(f.info);
						rebuilds.add(ss, (i%6)*ss.body().width(), (i/6)*ss.body().height());
						i++;
					}
				}
				
			}
		};
		{
			rebuild.exe();
			SETT.addGeneratorHook(rebuild);
		}
		
		CLICKABLE c = new GButt.ButtPanel(¤¤reseed) {
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(rebuilds, this);
				super.clickA();
			}
		}.pad(8, 4).hoverInfoSet(¤¤reseedD);
		
		section.addRelBody(16, DIR.S, c);
	
		
		
	}
	
	private RENDEROBJ prod(GETTER<FarmInstance> getter) {
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				FarmInstance ins = getter.get();

				b.textL(¤¤baseValue);
				b.tab(6);
				b.add(GFORMAT.f(b.text(), Util.base(ins)));
				b.NL();
				
				b.textL(Dic.¤¤ProductionRate);
				b.tab(6);
				b.add(GFORMAT.f(b.text(), blueprint.productionData.outs().get(0).rate));
				b.NL();

				b.textL(¤¤workValue);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), ins.tData.work()));
				b.NL();
				
				b.textL(¤¤skill);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), ins.tData.skill()));
				b.NL();
				
				
				b.NL(4);
				
				b.textLL(¤¤estimated);
				b.tab(6);
				b.add(GFORMAT.f1(b.text(), Util.prospect(ins)));
				b.NL();
				
				b.NL(16);
				b.textL(¤¤HarvestYear);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), (int)ins.blueprintI().indus.get(0).outs().get(0).year.get(ins)));
				b.NL();
				
				b.NL(2);
				b.textL(¤¤HarvestPrev);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), (int)ins.blueprintI().indus.get(0).outs().get(0).yearPrev.get(ins)));
				b.NL();
				
			}
			
		};
		
		s.add(blueprint.crop.resource.icon(), 0, 0);
		GStat stat = new GStat() {
			
			@Override
			public void update(GText text) {
				double am = Util.prospect(getter.get())/TIME.years().bitConversion(TIME.days());
				GFORMAT.f0(text, am);
			}
		};
		
		s.addRightC(6, stat);
		
		s.body().incrW(64);
		return s;
	}
	
	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
			LISTE<UIRoomBulkApplier> appliers) {
	}
	////////////////////////////////////////////////#!# Profit calculation's output revenue in terms of selling
	private RENDEROBJ profit1_display(GETTER<FarmInstance> get) {
		GuiSection s = new GuiSection();


		HOVERABLE h = new GStat() {

			@Override
			public void update(GText text) {
				refresh2(get, blueprint.industries().get(0).outs().get(0));
				GFORMAT.iIncr(text, (int) profit1);
			}
		}.r();

		s.addRightC(6, h);
		s.body().incrW(48);
		s.pad(4);
		return s;
	}
	//	/////////////////////////////////////////////#!# Profit calculation's output revenue in terms of using it for yourself
	private RENDEROBJ profit2_display(GETTER<FarmInstance> get) {
		GuiSection s = new GuiSection();


		HOVERABLE h = new GStat() {

			@Override
			public void update(GText text) {
				refresh2(get, blueprint.industries().get(0).outs().get(0));
				GFORMAT.iIncr(text, (int) profit2);
			}
		}.r();

		s.addRightC(6, h);
		s.body().incrW(48);
		s.pad(4);
		return s;
	}
	//	/////////////////////////////////////////////#!# Profit per person weighed average on for sale vs for self
	private RENDEROBJ profit3_display(GETTER<FarmInstance> get) {
		GuiSection s = new GuiSection();


		HOVERABLE h = new GStat() {

			@Override
			public void update(GText text) {
				refresh2(get, blueprint.industries().get(0).outs().get(0));
				GFORMAT.iIncr(text, (int) ppp3);
			}
		}.r();

		s.addRightC(6, h);
		s.body().incrW(48);
		s.pad(4);
		return s;
	}

}
