package settlement.stats.colls;

import game.time.TIME;
import game.time.TIMECYCLE;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import util.text.D;
import init.type.BUILDING_PREFS;
import init.type.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.water.pool.ROOM_POOL;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.standing.StatStanding;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.stat.STATFacade;
import settlement.stats.stat.StatCollection;
import settlement.tilemap.floor.Floors.Floor;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;

import java.util.Arrays;
import game.time.TIMECYCLE;
public class StatsEnv extends StatCollection{
	/// #!# static at the top, to make the UILogistics analysis work. resources | days
	public static int[][] sum_res = new int[100][16];     /// Needs to be at least as big as the # of resources. Just saying the # of resources was problematic
	public static int[][] sum_emp = new int[100][16];	/// Instead of counting up the resources, it counts up the # of people carrying
	public static int interval;
	public static boolean first_run=true;
	int prev_day= 0;

	///

	public final STAT BUILDING_PREF;
	public final STAT ROAD_PREF;
	public final STAT POOL_PREF;
	public final STAT CLIMATE;
	public final STAT OTHERS;
	public final STAT CANNIBALISM;
	public final STAT UNBURRIED;

	public final STAT ACCESS_ROAD;
	
	private static CharSequence ¤¤name = "Environment";
	private static CharSequence ¤¤desc = "External factors";
	static {
		D.ts(StatsEnv.class);
	}
	
	public StatsEnv(StatsInit init){
		super(init, "ENVIRONMENT", ¤¤name, ¤¤desc);
		
		ACCESS_ROAD = new STATData("ROAD_ACCESS", init, init.count.new DataBit("ENV_ROADA"));
		ACCESS_ROAD.info().icon = UI.icons().m.wheel;
		ROAD_PREF = new STATData("ROAD_PREF", init, init.count.new DataByte("ROAD_PREF"));
		ROAD_PREF.info().icon = UI.icons().m.wheel.twin(UI.icons().m.expand);
		BUILDING_PREF = new STATData("BUILDING_PREF", init, init.count.new DataNibble("BUILDING_PREF"));
		BUILDING_PREF.info().icon = UI.icons().m.building;
		POOL_PREF = new STATData("POOL_PREF", init, init.count.new DataNibble("POOL_PREF"));
		POOL_PREF.info().icon = UI.icons().m.water;
		init.onArrivalStats.add(ACCESS_ROAD);
		init.onArrivalStats.add(ROAD_PREF);
		init.onArrivalStats.add(BUILDING_PREF);
		init.onArrivalStats.add(POOL_PREF);
		
		CLIMATE = new STATFacade("CLIMATE", init) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				if (r == null) {
					double m = 0;
					for (Race rr : RACES.all()) {
						m += rr.population().climate(SETT.ENV().climate())*STATS.POP().POP.data(s).get(rr);
					}
					double p = STATS.POP().POP.data(s).get(null);
					if (p == 0)
						return m > 0 ? 1 : 0;
					return m/p;
				}
				return r.population().climate(SETT.ENV().climate());
			}
		};
		CLIMATE.standing = new StatStanding(CLIMATE, 1.0);
		CLIMATE.info().setMatters(true, false);
		CLIMATE.info().icon = UI.icons().s.heat;
		
		OTHERS = new STATFacade("OTHERS", init) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				if (r == null) {
					double p = 0;
					for (int ri = 0; ri < RACES.all().size(); ri++) {
						p += getDD(s, RACES.all().get(ri), daysBack)*STATS.POP().POP.data(s).get(RACES.all().get(ri), daysBack);
					}
					if (p == 0)
						return 0;
					return p/STATS.POP().POP.data(s).get(null, daysBack);
				}
				
				double pop = STATS.POP().POP.data(s).get(r, daysBack);
				if (pop == 0)
					return 1.0;
				pop = 0;
				double tot = 0;
				for (Race rr : RACES.all()) {
					double p = STATS.POP().POP.data(s).get(rr, daysBack);
					pop += p;
					tot += p*r.pref().race(rr);
				}
				if (pop == 0)
					return 1;
				tot /= pop;
				return CLAMP.d(tot, 0, 1);
			}
		};
		OTHERS.standing = new StatStanding(OTHERS, 1.0);
		OTHERS.info().setMatters(true, false);
		OTHERS.info().icon = UI.icons().m.descrimination;
		
		CANNIBALISM = new STATFacade("CANNIBALISM", init) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				return SETT.ROOMS().CANNIBAL.cannHistory().getD(daysBack);
			}
		};
		CANNIBALISM.info().setMatters(true, false);
		CANNIBALISM.info().icon = UI.icons().s.death;
		
		UNBURRIED = new STATFacade("UNBURRIED", init) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				double pop = 1.0 + STATS.POP().POP.data(null).get(null, daysBack);
				return 40*SETT.THINGS().corpses.addedHistory.get(daysBack)/pop; 
			}
			
			
		};
		UNBURRIED.info().setInt();
		UNBURRIED.info().setMatters(true, false);	
		UNBURRIED.info().icon = UI.icons().m.disease;
		
		init.updatable.add(updater);
	}
	
	private final StatUpdatableI updater = new StatUpdatableI() {
		
		
		
		@Override
		public void update16(Humanoid h, int updateI, boolean day, int ui) {
			//////////////////////////////// #!#
			///  Figure out what day it is
			int curr_day =  ((int) TIME.currentSecond()) / (int) TIME.days().bitSeconds();
			/// #!# Update int[][] (sum_res[resource][interval of time checked]) with the amount someone is carrying for each day
			/// #!#  sum_emp is the number of employees picking up each resource

			/// #!# Start of running the mod is first_run=true so interval = 0, prev_day = day
			if (first_run){interval = 0; first_run=false; prev_day = curr_day;}

			/// When the day isn't the same using  TIME.currentSecond()
			if (prev_day != curr_day) {
				///  #!# Add to the interval, but reset once the interval length is exceeded.
				interval++;
				if ( interval >= sum_res[0].length ){ interval=0; }

				/// remove the previous results of that column
				for (int i = 0; i < sum_res.length; i++) {
						sum_res[i][interval] = 0;
						sum_emp[i][interval] = 0;
				}
			}

			/// Add what they are carrying to the array.
			RESOURCE resource = h.ai().resourceCarried();
			if (resource != null) sum_res[resource.index()][interval] += h.ai().resourceA();
			if (resource != null) sum_emp[resource.index()][interval] += 1;
			///  prev_day = last value of day for the next run
			prev_day = curr_day;

			//////////////////////////////// #!#
			Induvidual i = h.indu();
			
			{
				double res = h.race().pref().structure(BUILDING_PREFS.get(h.tc().x(), h.tc().y()));
				for (DIR d : DIR.ORTHO) {
					res += h.race().pref().structure(BUILDING_PREFS.get(h.tc().x()+d.x(), h.tc().y()+d.y()));
				}
				res /= 5;
				
				int d = (int) Math.ceil((0x0F*res));
				int n = BUILDING_PREF.indu().get(h.indu());
				
				if (d > n*2) {
					BUILDING_PREF.indu().inc(i, 2);
				}else if (d > n) {
					BUILDING_PREF.indu().inc(i, 1);
				}else if(d < n && (updateI&0x07) == 0) {
					BUILDING_PREF.indu().inc(i, -1);
				}	
			}
			
			Room r = SETT.ROOMS().map.get(h.physics.tileC());
			
			if (r == null) {
				int current = ROAD_PREF.indu().get(i);
				int tar = 0; 
				
				double deg = 1-SETT.FLOOR().degrade.get(h.tc().x(), h.tc().y());
				
				Floor f = SETT.FLOOR().getter.get(h.physics.tileC());
				if (f != null && f.isRoad) {
					tar = (int) Math.ceil(deg*255*f.pref(h.race()));
					ACCESS_ROAD.indu().set(i, deg > 0.5 ? 1 : 0);
				}else {
					ACCESS_ROAD.indu().set(i, 0);
				}
				
				if (tar > current) {
					current+= 128;
					current = CLAMP.i(current, 0, tar);
				}else if (tar < current) {
					current-= 48;
					current = CLAMP.i(current, tar, 255);
				}
				ROAD_PREF.indu().set(i, current);
				
			}else if (r.blueprint() instanceof ROOM_POOL) {
				ROOM_POOL p = (ROOM_POOL) r.blueprint();
				double d = h.race().pref().pool(p);
				POOL_PREF.indu().setD(i, d);
			}
//			
//			if (SETT.ENV().environment.ROUNDNESS.area().get(h.physics.tileC()) > 0) {
//				ROUNDNESS.indu().set(i, SETT.ENV().environment.ROUNDNESS.get(h.physics.tileC()) > 0 ? 1 : 0);	
//			}
			
		}
		
	
		
	};

	
}
