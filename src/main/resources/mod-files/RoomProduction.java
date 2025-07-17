package settlement.room.industry.module;


import game.GAME;
import game.boosting.BOOSTABLES;
import game.boosting.BOOSTING;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.resources.*;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.POP_CL;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.main.ROOMS;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEquip;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.stats.STATS;
import settlement.stats.equip.EquipCivic;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.dic.Dic;
import world.army.AD;
import world.army.ADSupply;
import world.region.RD;

public class RoomProduction {
	//////////////////////////////////////////////////////////////////////////
	// #!# Allow spoilage estimate to include haulers, import/export depots
	//////////////////////////////////////////////////////////////////////////

	private final ArrayList<Res> producers = new ArrayList<Res>(RESOURCES.ALL().size());
	private final ArrayList<Res> consumers = new ArrayList<Res>(RESOURCES.ALL().size());
	private final ArrayList<Res> eaters = new ArrayList<Res>(RESOURCES.ALL().size());
	
	public RoomProduction(ROOMS rooms){
		for(RESOURCE res : RESOURCES.ALL()) {
			producers.add(new Res(res));
			consumers.add(new Res(res));
			eaters.add(new Res(res));
		}
		
		for (RoomBlueprint h : rooms.all()) {
			
			if (h instanceof INDUSTRY_HASER) {
				INDUSTRY_HASER ii = (INDUSTRY_HASER) h;
				for (Industry ins : ii.industries()) {
					for (IndustryResource oo : ins.outs()) {
						SourceR i = new SourceR(oo.resource, (RoomBlueprintImp)h, ins);
						producers.get(i.res.index()).ins.add(i);
						producers.get(i.res.index()).all.add(i);
					}
					for (IndustryResource oo : ins.ins()) {
						SourceR i = new SourceR(oo.resource, (RoomBlueprintImp)h, ins);
						consumers.get(i.res.index()).ins.add(i);
						consumers.get(i.res.index()).all.add(i);
					}
				}	
			}
		}
		
		
		for (ResGEat e : RESOURCES.EDI().all()) {
			
			Source s = new Source(e.resource) {

				@Override
				public double am() {
					return FACTIONS.player().res().out(RTYPE.CONSUMED).history(e.resource).get(1);
				}

				@Override
				public SPRITE icon() {
					return UI.icons().s.human;
				}

				@Override
				public CharSequence name() {
					return Dic.¤¤Consumed;
				}
				
			};
			
			consumers.get(e.resource.index()).all.add(s);
			eaters.get(e.resource.index()).all.add(s);
		}
		
		for (ResGDrink e : RESOURCES.DRINKS().all()) {
			
			Source s = new Source(e.resource) {

				@Override
				public double am() {
					return FACTIONS.player().res().out(RTYPE.CONSUMED).history(e.resource).get(1);
				}

				@Override
				public SPRITE icon() {
					return UI.icons().s.human;
				}

				@Override
				public CharSequence name() {
					return Dic.¤¤Consumed;
				}
				
			};
			
			consumers.get(e.resource.index()).all.add(s);
			eaters.get(e.resource.index()).all.add(s);
		}
		
		for (RESOURCE res : RESOURCES.ALL()) {
			producers.get(res.index()).all.add(new SourceReg(res));
			
			consumers.get(res.index()).all.add(new Source(res) {

				@Override
				public double am() {
					return SETT.MAINTENANCE().estimateGlobal(res);
				}

				@Override
				public SPRITE icon() {
					return SETT.MAINTENANCE().icon;
				}

				@Override
				public CharSequence name() {
					return Dic.¤¤Maintenance;
				}
				
			});
			

			
			consumers.get(res.index()).all.add(new Source(res) {

				@Override
				public double am() {
					////////////////////////////////////////////////#!#
					double d = 0.75*res.degradeSpeed()/(TIME.years().bitConversion(TIME.days())*BOOSTABLES.CIVICS().SPOILAGE.get(POP_CL.clP(null, null)));
					return d * (
						SETT.ROOMS().STOCKPILE.tally().amountTotal(res)*.5 +
						SETT.ROOMS().HAULER.tally.amountTotal(res)      +
						SETT.ROOMS().EXPORT.tally.forSale(res)          +
						SETT.ROOMS().IMPORT.tally.amount.get(res)
					);
					////////////////////////////////////////////////#!#

				}

				@Override
				public SPRITE icon() {
					return SETT.MAINTENANCE().icon;
				}

				@Override
				public CharSequence name() {
					return Dic.¤¤Spoilage;
				}
				
			});
		}
		

		
		for (RoomEquip e : rooms.employment.equip.ALL) {
			consumers.get(e.resource.index()).all.add(new Source(e.resource) {

				@Override
				public double am() {
					return e.currentTotal()*e.degradePerDay;
				}

				@Override
				public SPRITE icon() {
					return UI.icons().s.hammer;
				}

				@Override
				public CharSequence name() {
					return Dic.¤¤Equipped;
				}
				
			});
		}
		
		BOOSTING.connecter(new ACTION() {
			
			@Override
			public void exe() {
				for (EquipCivic e : STATS.EQUIP().civics()) {
					
					consumers.get(e.resource.index()).all.add(new Source(e.resource) {

						@Override
						public double am() {
							return e.stat().data().get(null)*e.wearPerYear/TIME.years().bitConversion(TIME.days());
						}

						@Override
						public SPRITE icon() {
							return UI.icons().s.citizen;
						}

						@Override
						public CharSequence name() {
							return Dic.¤¤Equipped;
						}
						
					});
					
				}
				
				for (ADSupply sup : AD.supplies().all) {
					consumers.get(sup.res.index()).all.add(new Source(sup.res) {

						@Override
						public double am() {
							
							if (SETT.ROOMS().SUPPLY.tally.crates.total(sup.res) <= 0)
								return 0;
							
							
							
							return sup.usedPerDay*sup.target().faction(FACTIONS.player());
						}

						@Override
						public SPRITE icon() {
							return UI.icons().s.sword;
						}

						@Override
						public CharSequence name() {
							return Dic.¤¤Supplies;
						}
						
					});
				}
				
				
				for (RES_AMOUNT e : RACES.res().homeResMax(null)) {
					
					final ArrayListGrower<POP_CL> con = new ArrayListGrower<>();
					final ArrayListGrower<Integer> conRI = new ArrayListGrower<>();

					for (HCLASS cl : HCLASSES.ALL()) {
						for (Race ra : RACES.all()) {
							int ri = 0;
							for (RES_AMOUNT rr : ra.home().clas(cl).resources()) {
								
								
								if (rr.resource() == e.resource()) {
									con.add(POP_CL.clP(ra, cl));
									conRI.add(ri);
								}
								ri++;
							}
						}
					}
					
					consumers.get(e.resource().index()).all.add(new Source(e.resource()) {
						
						@Override
						public double am() {
							double am = 0;
							for (int i = 0; i < con.size(); i++) {
								am += STATS.HOME().current(con.getC(i).cl, con.get(i).race, conRI.get(i))*STATS.HOME().rate(con.getC(i).cl, con.get(i).race);
							}
							
							return am/TIME.years().bitConversion(TIME.days());
						}

						@Override
						public SPRITE icon() {
							return UI.icons().s.house;
						}

						@Override
						public CharSequence name() {
							return STATS.HOME().materials.info().name;
						}
						
					});
				}
					
				for (ROOM_TEMPLE t : SETT.ROOMS().TEMPLES.ALL) {
					if (t.resource != null) {
						consumers.get(t.resource.index()).all.add(new Source(t.resource) {

							@Override
							public double am() {
								double a = 0;
								for (int i = 0; i < t.instancesSize(); i++) {
									a += t.getInstance(i).sacrifices(); 
								}
								return a;
							}

							@Override
							public SPRITE icon() {
								return t.icon;
							}

							@Override
							public CharSequence name() {
								return t.info.names;
							}
							
						});
					}
					
				}
				
				
				for (Res r : producers)
					r.init();
				for (Res r : consumers)
					r.init();
			}
		});
		

	}
	
	private void update(int ticks) {
		ENTITY[] es = SETT.ENTITIES().getAllEnts();
		
		int tott = ticks*200;
		if (tott < 0 || tott > es.length)
			tott= es.length;
		
		for (int i = 0; i < tott; i++) {
			
			if (ui >= es.length) {
				
				for (Res r : producers) {
					double tot = 0;
					for (SourceR in : r.ins) {
						in.am = in.old;
						in.old = 0;
						tot += in.am;
					}
					for (Source in : r.all) {
						if (in instanceof SourceReg)
							tot += in.am();
					}
					r.am = tot;
				}
				for (Res r : consumers) {
					double tot = 0;
					for (SourceR in : r.ins) {
						in.am = in.old;
						in.old = 0;
						tot += in.am;
					}
					r.am = tot;
				}
				
				ui = 0;
				break;
			}
			
			ENTITY e = es[ui];
			if (e != null && e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				RoomInstance ins = STATS.WORK().EMPLOYED.get(h);
				if (ins != null && ins instanceof ROOM_PRODUCER) {
					ROOM_PRODUCER p = (ROOM_PRODUCER) ins;
					Industry in = p.industry();
					for (IndustryResource oo : in.outs()) {
						RESOURCE res = oo.resource;
						double d = p.productionRate(ins, h, in, oo);
						for (SourceR ii : producers.get(res.index()).ins) {
							if (ii.blue == in.blue && ii.ins == in) {
								ii.old += d;
							}
						}
					}
					for (IndustryResource oo : in.ins()) {
						RESOURCE res = oo.resource;
						double d = p.consumptionRate(ins, h, in, oo);
						for (SourceR ii : consumers.get(res.index()).ins) {
							if (ii.blue == in.blue && ii.ins == in) {
								ii.old += d;
							}
						}
					}
				}
			}
			ui++;
			
		}
	}
	
	int ui = 0;
	int upI = 0;
	
	public double produced(RESOURCE res) {
		if (Math.abs(upI-GAME.updateI()) > 1) {
			update(Math.abs(upI-GAME.updateI()));
			upI = GAME.updateI();
		}
		return producers.get(res.index()).am;
	}
	
	public double consumed(RESOURCE res) {
		if (Math.abs(upI-GAME.updateI()) > 1) {
			update(Math.abs(upI-GAME.updateI()));
			upI = GAME.updateI();
		}
		return consumers.get(res.index()).am;
	}
	
	public LIST<Source> producers(RESOURCE res) {
		return producers.get(res.index()).all;
	}
	
	public LIST<Source> consumers(RESOURCE res) {
		return consumers.get(res.index()).all;
	}
	
	public LIST<Source> eaters(RESOURCE res) {
		return eaters.get(res.index()).all;
	}
	
	private class Res {
		
		private final ArrayListGrower<Source> all = new ArrayListGrower<>();
		private final ArrayListGrower<SourceR> ins = new ArrayListGrower<>();
		private double am;
		
		Res(RESOURCE res){

		}
		
		private void init() {
			for (int i1 = 0; i1 < ins.size(); i1++) {
				for (int i2 = 0; i2 < ins.size(); i2++) {
					if (i2 == i1)
						continue;
					if (ins.get(i1).blue == ins.get(i2).blue) {
						ins.get(i1).multiple = true;
						ins.get(i2).multiple = true;
					}
					
				}
				
			}
		}
		
	}
	
	
	
	public abstract static class Source {
		
		public final RESOURCE res;

		
		Source(RESOURCE res){
			this.res = res;
		}
	
		public abstract double am();
		
		public Industry thereAreMultipleIns() {
			return null;
		}
		
		public abstract SPRITE icon();
		
		public abstract CharSequence name();
		
	}
	
	
	public class SourceReg extends Source{
		
		SourceReg(RESOURCE res){
			super(res);
			
		}
	
		@Override
		public double am() {
			int am = 0;
			for (int i = 0; i < FACTIONS.player().realm().regions(); i++) {
				am += RD.OUTPUT().get(res).getDelivery(FACTIONS.player().realm().region(i));
			}
			return am;
		}
		
		@Override
		public Industry thereAreMultipleIns() {
			return null;
		}
		
		@Override
		public SPRITE icon() {
			return UI.icons().s.money;
		}
		
		@Override
		public CharSequence name() {
			return Dic.¤¤Taxes;
		}
		
	}

	public class SourceR extends Source{
	
		private final RoomBlueprintImp blue;
		private final Industry ins;
		private double old;
		private double am;
		private boolean multiple = false;
		
		SourceR(RESOURCE res, RoomBlueprintImp blue, Industry ins){
			super(res);
			this.blue = blue;
			this.ins = ins;
		}
	
		@Override
		public double am() {
			if (Math.abs(upI-GAME.updateI()) > 1) {
				update(Math.abs(upI-GAME.updateI()));
				upI = GAME.updateI();
			}
			return am;
		}
		
		@Override
		public Industry thereAreMultipleIns() {
			return multiple ? ins : null;
		}
		
		@Override
		public SPRITE icon() {
			return blue.icon.small;
		}
		
		@Override
		public CharSequence name() {
			return blue.info.names;
		}
		
	}
	
	
}
