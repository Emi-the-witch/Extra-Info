package settlement.room.infra.embassy;

import game.time.TIME;
import init.resources.RESOURCE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.JOB_MANAGER;
import settlement.misc.job.SETT_JOB;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.Industry.IndustryResource;
import settlement.room.industry.module.ROOM_PRODUCER;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobPositions;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

import java.io.Serializable;

final class EmbassyInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_PRODUCER{

	private static final long serialVersionUID = 1L;
	final Jobs jobs;
	double skill = 1;
	int skillI = 1;
	
	private long[] pdata;
	Res[] res;
	
	protected EmbassyInstance(ROOM_EMBASSY blueprint, TmpArea area, RoomInit init) {
		super(blueprint, area, init);
		jobs = new Jobs(this);
		
		employees().neededSet((int) Math.ceil(jobs.size()));
		employees().maxSet(jobs.size());


		res = new Res[blueprint.industry.ins().size()];
		for (int i = 0; i < res.length; i++)
			res[i] = new Res();
		
		blueprint.data.incStations(jobs.size());
		pdata = industry().makeData();
		activate();
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator i) {
		i.lit();
		return super.render(r, shadowBatch, i);
	}

	@Override
	protected void updateAction(double updateInterval, boolean day) {
		// add this next line for dayPrev to work for input costs.
		blueprintI().industry.updateRoom(this);
		jobs.searchAgain();
		
		{
			skill = skill/skillI;
			skillI = 1;
			double time = updateInterval*employees().employed()/TIME.workValue;

			for (int ri = 0; ri < res.length; ri++) {
				double prod = industry().ins().get(ri).rateSeconds*skill*time;
				if (prod > res[ri].current)
					prod = res[ri].current;
				int am = industry().ins().get(ri).inc(this, prod);
				res[ri].current -= am;
			}

			
			blueprintI().data.perform(time, skill);
			
		}
	}

	@Override
	protected void loadFix() {
		pdata = industry().makeDataFix(pdata);
		if (res.length != blueprintI().industry.ins().size()) {
			res = new Res[blueprintI().industry.ins().size()];
			for (int i = 0; i < res.length; i++)
				res[i] = new Res();
		}
	}
	

	
	@Override
	protected void activateAction() {

	}

	@Override
	protected void deactivateAction() {
		
	}
	
	@Override
	public JOB_MANAGER getWork() {
		return jobs;
	}
	
	@Override
	protected void dispose() {
		blueprintI().data.incStations(-jobs.size());
		int ri = 0;
		for (Res r : res) {
			if (r.current > 0) {
				SETT.THINGS().resources.create(mX(), mY(), blueprintI().industry.ins().get(ri++).resource, r.current);
			}
		}
	}
	
	public void disableToggle(Res r) {
		if (!r.disabled) {
			int ri = 0;
			for (Res r2 : res) {
				if (r == r2 && r.current > 0) {
					RESOURCE rr =  blueprintI().industry.ins().get(ri++).resource;
					SETT.THINGS().resources.create(mX(), mY(), rr, r.current);
					r.current = 0;
					
				}
			}
		}
		r.disabled = !r.disabled;
		r.reserved = 0;
		r.unreachable = false;
		jobs.resetResourceSearch();
	}
	
	@Override
	public ROOM_EMBASSY blueprintI() {
		return (ROOM_EMBASSY) blueprint();
	}
	
	
	
	static class Jobs extends JobPositions<EmbassyInstance> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Jobs(EmbassyInstance ins) {
			super(ins);
		}
		
		@Override
		protected SETT_JOB get(int tx, int ty) {
			return ins.blueprintI().job.get(tx, ty);
		}

		@Override
		protected boolean isAndInit(int tx, int ty) {
			return ins.blueprintI().job.get(tx, ty) != null;	
			
		}
	}
	
	static class Res implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public int reserved;
		public int current;
		public boolean unreachable = false;
		public boolean disabled = true;
		
	}

	
	
	@Override
	public long[] productionData() {
		return pdata;
	}

	@Override
	public Industry industry() {
		return blueprintI().industry;
	}

	@Override
	public int industryI() {
		return 0;
	}

	@Override
	public double consumptionRate(RoomInstance ins, Humanoid h, Industry in, IndustryResource oo) {
		for (int i = 0; i < blueprintI().industry.ins().size(); i++) {
			if (oo == blueprintI().industry.ins().get(i) && res[i].disabled)
				return 0;
		}
		return ROOM_PRODUCER.super.consumptionRate(ins, h, in, oo);
	}
	
	
}
