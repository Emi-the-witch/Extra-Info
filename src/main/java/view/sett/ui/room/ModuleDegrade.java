package view.sett.ui.room;

import game.faction.FACTIONS;
import game.time.TIME;
import init.resources.RESOURCE;
import init.settings.S;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.text.D;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.text.Str;
import util.data.GETTER;
import util.dic.Dic;
import util.gui.misc.*;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModuleDegrade implements ModuleMaker {
	/////////////////////////////////////////////////////////////////////////////////////////////////
	///#!# Building Maintenance now includes the denari value for the annual maintenance estimate
	////////////////////////////////////////////////////////////////////////////////////////////////
	private final CharSequence ¤¤DEGRADE_AVE = "¤Average degradation amongst these rooms. Degradation affects a room negatively.";

	private final CharSequence ¤¤RoomType = "¤Room Type";
	private final CharSequence ¤¤Lock = "¤The technology for the room is locked, and it can't be maintained.";
	private final CharSequence ¤¤badIsolation = "¤Room is poorly insulated!";
	private final CharSequence ¤¤maintenance = "¤Required Maintenance (year)";

	public ModuleDegrade(Init init) {
		D.t(this);
//		sDEGRADE = init.d("DEGRADE");
//		sDEGRADE_AVE = init.d("DEGRADE_AVE");
//		sDEGRADE_DESC = init.d("DEGRADE_DESC");
	}
	

	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		
		if (p instanceof RoomBlueprintIns<?>) {
			RoomBlueprintIns<?> pp = (RoomBlueprintIns<?>) p;
			if (pp.degrades()) {
				l.add(new Hover());
				l.add(new I(pp));
			}
		}
		
	}
	
	private final class Hover extends UIRoomModule {
		
		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			if (room.degrader(rx, ry) != null) {
				box.text(Dic.¤¤Degrade);
				box.add(GFORMAT.percInv(box.text(), room.getDegrade(rx, ry)));
				box.add(UI.icons().s.hammer);
				box.add(GFORMAT.i(box.text(), room.degrader(rx, ry).jobs()));
				box.NL(2);
				if (!room.constructor().blue().reqs.passes(FACTIONS.player()))
					box.error(¤¤Lock);
				else if (room.upgrade(rx, ry) > 0 && !room.constructor().blue().upgrades().requires(room.upgrade(rx, ry)).passes(FACTIONS.player()))
					box.error(¤¤Lock);
				box.NL(2);
			}
			if(room.constructor() != null && room.constructor().needsIsolation()) {
				box.text(SETT.ROOMS().isolation.info.name);
				box.add(GFORMAT.perc(box.text(), room.isolation(rx, ry)));
				box.NL(2);
			}
		}

		@Override
		public void problem(Stack<Str> free, LISTE<CharSequence> errors, LISTE<CharSequence> warnings, Room room, int rx, int ry) {
			if(room.getDegrade(rx, ry) > 0.25) {
				errors.add(Dic.¤¤Degrade);
			}
			if(room.constructor() != null && room.constructor().mustBeIndoors() && room.isolation(rx, ry) < 1.0) {
				warnings.add(¤¤badIsolation);
			}
		}
	}
	
	private final class I extends UIRoomModule {
		
		private final RoomBlueprintIns<?> blue;
		
		I(RoomBlueprintIns<?> blue){
			this.blue = blue;
		}
		
		@Override
		public void appendManageScr(GGrid grid, GGrid text, GuiSection extra) {
			grid.add(new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.percInv(text, (blue.degradeAverage()));
					
				}
			}.hh(SPRITES.icons().s.degrade).hoverInfoSet(¤¤DEGRADE_AVE));
		}

		@Override
		public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
				LISTE<UIRoomBulkApplier> appliers) {
			sorts.add(new GTSort<RoomInstance>(Dic.¤¤Degrade) {
				
				@Override
				public int cmp(RoomInstance current, RoomInstance cmp) {
					return Double.compare(current.getDegrade(), cmp.getDegrade());
				}

				@Override
				public void format(RoomInstance h, GText text) {
					GFORMAT.perc(text, h.getDegrade());
				}
				
			});
			sorts.add(new GTSort<RoomInstance>(SETT.ROOMS().isolation.info.name) {
				
				@Override
				public int cmp(RoomInstance current, RoomInstance cmp) {
					return Double.compare(current.isolation(current.mX(), current.mY()), cmp.isolation(cmp.mX(), cmp.mY()));
				}

				@Override
				public void format(RoomInstance h, GText text) {
					GFORMAT.perc(text, h.isolation(h.mX(), h.mY()));
				}
				
			});
			
		}
		
		@Override
		public void appendPanelIcon(LISTE<RENDEROBJ> section, GETTER<RoomInstance> get) {
			
			GStat s = new GStat() {
				@Override
				public void update(GText text) {
					GFORMAT.percInv(text, get.get().getDegrade());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					
					b.title(Dic.¤¤Degrade);
					b.text(Dic.¤¤DegradeDesc);
					b.NL();
					b.textLL(Dic.¤¤Current);
					b.tab(6);
					b.add(GFORMAT.percInv(b.text(), get.get().getDegrade()));
					
					if (S.get().developer) {
						b.add(GFORMAT.f(b.text(), get.get().degrader(get.get().mX(), get.get().mX()).getSecret(), 4));
					}
					b.sep();
					b.NL(8);
					
					RoomInstance ins = get.get();
					ROOM_DEGRADER deg = get.get().degrader(get.get().mX(), get.get().mY());
					int area = ins.area();
					double iso = ins.isolation(get.get().mX(), get.get().mY());
					double boost = SETT.MAINTENANCE().speed();
					
					double ram = 0;
					for (int i = 0; i < deg.resSize(); i++) {
						ram += deg.resAmount(i);
					}
					
					b.textLL(¤¤maintenance);
					b.NL();
					
					col(b, Dic.¤¤Base, '*', SETT.MAINTENANCE().tilesPerDay*TIME.years().bitConversion(TIME.days()));
					col(b, ¤¤RoomType, '*', deg.base());
					col(b, Dic.¤¤Area, '*', area);
					col(b, SETT.ROOMS().isolation.info.name, '*', 1 + (1-iso)*2);
					col(b, Dic.¤¤Boosts, '*', boost);

					double tot = ROOM_DEGRADER.rate(boost, deg.base(), iso, ram, ins.area())*TIME.years().bitConversion(TIME.days());
					double resr = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, ram)*TIME.years().bitConversion(TIME.days());

					b.tab(6);
					b.add(b.text().add('=').s().add((int)(tot-resr)));
					
					
					
					b.sep();
					
					for (int i = 0; i < deg.resSize(); i++) {
						if (deg.resAmount(i) <= 0)
							continue;
						RESOURCE res = deg.res(i);
						b.add(res.icon());
						b.textL(res.name);
						b.tab(6);
						b.add(GFORMAT.f0(b.text(), -ROOM_DEGRADER.rateResource(boost, deg.base(), iso, deg.resAmount(i))*TIME.years().bitConversion(TIME.days())));
						b.NL();
					}
					
					b.sep();
					
					
					b.NL(8);
					b.textLL(Dic.¤¤Total);
					b.tab(6);
					b.add(GFORMAT.f(b.text(), tot, 2));

					/////////////////////////////////////////////////////////#!#
					// Maintenance costs in denars!
					b.sep();
					b.textLL("Annual maintenance cost in denars if imported:");
					b.NL(8);
					double maint_cost = 0;
					for (int i = 0; i < deg.resSize(); i++) {
						if (deg.resAmount(i) <= 0)
							continue;
						RESOURCE res = deg.res(i);
						b.add(res.icon());
						b.textL(res.name);
						b.tab(6);
						b.add(GFORMAT.f0(b.text(), -ROOM_DEGRADER.rateResource(boost, deg.base(), iso, deg.resAmount(i))*TIME.years().bitConversion(TIME.days())*FACTIONS.player().trade.pricesBuy.get(res)));
						maint_cost += (-ROOM_DEGRADER.rateResource(boost, deg.base(), iso, deg.resAmount(i))*TIME.years().bitConversion(TIME.days())*FACTIONS.player().trade.pricesBuy.get(res));
						b.textLL(" $");
						b.NL();
					}

					b.sep();
					b.textLL(Dic.¤¤Total);
					b.add(GFORMAT.f0(b.text(), maint_cost));
					b.textLL(" $");
					b.sep();
					b.NL(8);
/////////////////////////////////////////////////////////#!#
				}
				
			
			};
			

			
			section.add(new GHeader.HeaderHorizontal(SPRITES.icons().s.degrade, s)); 
		}
		
		private void col(GBox b, CharSequence header, char append, double value) {
			b.textLL(header);
			b.tab(6);
			GText t = b.text();
			t.add(append);
			b.add(GFORMAT.f(t, value, 2));
			b.NL();
		}
		
	}

	





	
}
