package view.sett.ui.room;

import settlement.environment.SettEnvMap.SettEnv;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomInstance;
import settlement.room.main.furnisher.Furnisher;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LISTE;
import util.data.GETTER;
import util.dic.Dic;
import util.gui.misc.GBox;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import view.sett.ui.room.Modules.ModuleMaker;

import java.util.Objects;

import static view.sett.ui.room.CanalCounter.countCanals;

final class ModuleConstructor implements ModuleMaker {
	

	public ModuleConstructor(Init init) {
	
	}
	
	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		if (p instanceof RoomBlueprintImp) {
			RoomBlueprintImp pp = (RoomBlueprintImp) p;
			if (pp.constructor() != null)
				l.add(new I());
		}
	}

	private final class I extends UIRoomModule {
		

		
		I(){

		}

		@Override
		public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
				LISTE<UIRoomBulkApplier> appliers) {
		}

		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			Furnisher f = room.constructor();
			if (f != null) {
				boolean has = false;
				for (SettEnv e : SETT.ENV().map.all()) {
					if (f.envValue(e)) {
						if (!has) {
							box.NL(8);
							box.textL(Dic.¤¤Emits);
							box.NL();
						}
						box.text(e.info.name);
						box.NL();
						if(Objects.equals(room.name(rx,ry),"Canal")){
							box.textL("Canal length: "+ countCanals(room, rx, ry) );
							box.NL();
						}
//						box.textL("I'm right here");
//						box.NL();
//						box.textL(room.name(rx,ry));
//						box.NL();
					}
				}
				if (has)
					box.NL(8);
			}
			
			
		}

		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
			// TODO Auto-generated method stub
			
		}


		
	}
	
	



	
}
