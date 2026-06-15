package view.sett.ui.room;

import settlement.room.main.Room;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.text.Str;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GGrid;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;

public abstract class UIRoomModule {
	
	public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
		
	}
	
	public void appendPanelIcon(LISTE<RENDEROBJ> section, GETTER<RoomInstance> get) {
		
	}
	
	public void appendManageScr(GGrid icons, GGrid text, GuiSection extra) {
		
	}
	
	public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts, LISTE<UIRoomBulkApplier> appliers) {
		
	}
	
	public void appendButt(GuiSection butt, GETTER<RoomInstance> get) {
		
	}
	
	public void hover(GBox box, Room i, int rx, int ry) {
		
	}
	
	public void problem(Stack<Str> free, LISTE<CharSequence> errors, LISTE<CharSequence> warnings, Room r, int rx, int ry) {
		
	}
	
	public static class UIRoomModuleImp<T extends RoomInstance, B extends RoomBlueprintIns<T>>  {
		// Make it public!
		public final B blueprint;
		
		public UIRoomModuleImp(B blueprint) {
			this.blueprint = blueprint;
		}
		
		protected void appendPanel(GuiSection section, GGrid g, GETTER<T> getter, int x1, int y1) {
			
		}
		
		protected void appendMain(GGrid icons, GGrid text, GuiSection sExtra) {
			
		}
		
		protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts, LISTE<UIRoomBulkApplier> appliers) {
			
		}
		
		protected void appendTableButt(GuiSection s, GETTER<RoomInstance> ins) {
			
		}
		
		protected void hover(GBox box, T i) {
			
		}
		
		protected void problem(T i, Stack<Str> free, LISTE<CharSequence> errors, LISTE<CharSequence> warnings) {
			
		}
		
		public UIRoomModule make() {
			return new UIRoomModule() {
				
				
				
				@Override
				public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
					GETTER<T> getter = new GETTER<T>(){

						@SuppressWarnings("unchecked")
						@Override
						public T get() {
							return (T) get.get();
						}
						
					};
					GGrid g = new GGrid(section, 2, y1);
					UIRoomModuleImp.this.appendPanel(section, g, getter, x1, y1);
				}
				
				@Override
				public void appendManageScr(GGrid icons, GGrid text, GuiSection extra) {
					UIRoomModuleImp.this.appendMain(icons, text, extra);
				}
				
				@Override
				public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters,
						LISTE<GTSort<RoomInstance>> sorts, LISTE<UIRoomBulkApplier> appliers) {
					UIRoomModuleImp.this.appendTableFilters(filters, sorts, appliers);
				}
				@Override
				public void appendButt(GuiSection s, GETTER<RoomInstance> get) {
					UIRoomModuleImp.this.appendTableButt(s, get);
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public void hover(GBox box, Room room, int rx, int ry) {
					UIRoomModuleImp.this.hover(box, (T)room);
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public void problem(Stack<Str> free, LISTE<CharSequence> errors, LISTE<CharSequence> warnings, Room room, int rx, int ry) {
					UIRoomModuleImp.this.problem((T)room, free, errors, warnings);
				}
				
				
				
			};
		}
		
	}
	
}
