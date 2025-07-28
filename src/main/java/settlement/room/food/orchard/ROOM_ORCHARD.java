package settlement.room.food.orchard;

import game.time.TIME;
import init.resources.RESOURCES;
import init.resources.RES_AMOUNT;
import init.type.TERRAINS;
import settlement.path.finders.SFinderRoomService;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.RoomBoost;
import settlement.room.industry.module.IndustryRegion;
import settlement.room.main.BonusExperience.RoomExperienceBonus;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.main.util.RoomInitData;
import settlement.room.water.RoomIrrigated;
import settlement.room.water.RoomIrrigated.ROOM_IRRIGATED;
import settlement.weather.WeatherMoisture;
import snake2d.util.MATH;
import snake2d.util.datatypes.AREA;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import view.sett.ui.room.UIRoomModule;
import world.map.regions.Region;
import world.map.regions.RegionInfo;

import java.io.IOException;

public class ROOM_ORCHARD extends RoomBlueprintIns<Instance> implements INDUSTRY_HASER, ROOM_IRRIGATED{

	public final static String type = "ORCHARD";
	
	final static double TILES_PER_WORKER = TIME.workSeconds/(OTile.WORK_TIME+TIME.workSecondsWalkNext);
	
	
	final Constructor constructor;
	final Industry productionData;
	public final LIST<Industry> indus;
	final OTile tile;
	public final RES_AMOUNT auxRes;
	private byte year = -1;
	public final Time time;
	public final double AmountPerTile = TIME.years().bitConversion(TIME.days())/TILES_PER_WORKER;
	private final RoomIrrigated irri;
	public ROOM_ORCHARD(RoomInitData data, String key, RoomCategorySub cat, int index) throws IOException {
		super(index, data, key, cat);
		
		constructor = new Constructor(this, data);
		pushBo(data.data(), type, true);
		
		RoomBoost mo = WeatherMoisture.makeBoost(); 
		
		productionData = new Industry(this, data.data(), new RoomBoost[] {constructor.fertility, mo}, bonus());
		
		new IndustryRegion(productionData, 0.5) {
			
			@Override
			public double occurence(Region reg) {
				return reg.info.terrain(TERRAINS.FOREST()) + RegionInfo.vFer().getAi(reg);
			}
		};
		
		indus = new ArrayList<>(productionData);
		this.time = new Time(data);
		auxRes = new RES_AMOUNT.Abs(RESOURCES.map().read("EXTRA_RESOURCE", data.data()), data.data().i("EXTRA_RESOURCE_AMOUNT"));
		tile = new OTile(this);
		new RoomExperienceBonus(this, data.data(), bonus());
		irri = new RoomIrrigated(this, bonus, 0.5, 1) {
			
			@Override
			public double needed(AREA area) {
				return area.area()/2;
			}
			
			@Override
			protected double irrigation(RoomInstance ins) {
				return ((Instance)ins).irri;
			}
			
		};
		productionData.AIMul = 0.75;
	}
	
	OTile tile(int tx, int ty) {
		return tile.get(tx, ty);
	}

	@Override
	protected void update(float ds) {
		
	}
	
	
	@Override
	public Furnisher constructor() {
		return constructor;
	}
	
	@Override
	public SFinderRoomService service(int tx, int ty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void saveP(FilePutter saveFile){
		productionData.save(saveFile);
		saveFile.b(year);
	}
	
	@Override
	protected void loadP(FileGetter saveFile) throws IOException{
		productionData.load(saveFile);
		year = saveFile.b();
	}
	
	@Override
	protected void clearP() {
		productionData.clear();
		year = -1;
	}
	
	@Override
	public boolean degrades() {
		return false;
	}

	@Override
	public void appendView(LISTE<UIRoomModule> mm) {
		mm.add(new Gui(this).make());
	}

	@Override
	public LIST<Industry> industries() {
		return indus;
	}
	
	@Override
	public boolean industryIgnoreUI() {
		return true;
	}
	
	public boolean event(int tx, int ty, double severity) {
		Instance ins = getter.get(tx, ty);
		if (ins != null) {
			return ins.event();
		}
		return false;
	}
	
	public static class Time {
		public final int DAYS_TILL_GROWTH;
		public final int ripeDay;
		public final int deadDay;
		public final int days = (int)TIME.years().bitConversion(TIME.days());
		
		Time(RoomInitData data){
			DAYS_TILL_GROWTH = data.data().i("DAYS_TILL_GROWTH", 8, 1024);
			ripeDay = (int) (days*data.data().d("RIPE_AT_PART_OF_YEAR", 0, 1));
			deadDay = MATH.mod(ripeDay + 3, days);
			
			
		}
		
		public boolean isRipe() {
			return isRipe(dayI());
		}
		
		public boolean isRipe(double day) {
			return MATH.isWithin(day, ripeDay, deadDay);
		}
		
		public boolean isDeadDay() {
			return dayI() == deadDay;
		}
		
		public double fruit() {
			return fruit(day());
		}
		
		private double fruit(double day) {

			double rd = (int) (ripeDay+days-2);
			double dd = (int) (deadDay+days-1);
			if (deadDay < ripeDay) {
				dd+=TIME.years().bitConversion(TIME.days());
			}
			
			double di = day + days;
			
			if (di > rd) {
				if (di < dd)
					return CLAMP.d(0.5*(di-rd), 0, 1);
				else if (di > dd)
					return CLAMP.d(1.0-(di-dd), 0, 1);
				return 1.0;
			}
			return 0;
		}
		
		public double day() {
			return TIME.years().bitPartOf()*TIME.years().bitConversion(TIME.days());
		}
		
		public int dayI() {
			return (int) day();
		}
		
		public int daysTillHarvest() {
			return MATH.distance(dayI(), ripeDay, days);
		}
		
		public int daysTillHarvest(int nextTreeDays) {
			int day = TIME.days().bitCurrent();
			day += nextTreeDays;
			int dy = (int) TIME.years().bitConversion(TIME.days());
			int dday = day%dy;
			return day + MATH.distance(dday, ripeDay, days);
		}
		
	}

	@Override
	public RoomIrrigated irrigation() {
		return irri;
	}
	
}
