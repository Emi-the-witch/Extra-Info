package settlement.room.food.farm;

import game.time.TIME;

public class Util {

	public Util() {
		
	}
	
	public static double prospect(FarmInstance ins) {
		
		double base = base(ins)*ins.industry().outs().get(0).rate;

		double skill = ins.tData.skill();
		double work = ins.tData.work();
		return base*skill*work*TIME.years().bitConversion(TIME.days());
		
	}
	
	public static double base(FarmInstance ins) {
		
		double area = ins.area()/ROOM_FARM.WORKERPERTILE;
		return area;
		
	}
	
	public static int prevHarvest(FarmInstance ins) {
		
		ROOM_FARM b = ins.blueprintI();
		Time t = b.time;
		if (t.dayI() < t.dayDeath)
			return (int) b.industries().get(0).outs().get(0).yearPrev.get(ins);
		else
			return (int) b.industries().get(0).outs().get(0).year.get(ins);
	}
}
