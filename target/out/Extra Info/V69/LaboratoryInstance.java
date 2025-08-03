package settlement.room.knowledge.laboratory;

import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.JOB_MANAGER;
import settlement.misc.job.SETT_JOB;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.ROOM_PRODUCER;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobPositions;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

final class LaboratoryInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_PRODUCER{

	private static final long serialVersionUID = 1L;
	final Jobs jobs;
	private long[] pdata;
	
	protected LaboratoryInstance(ROOM_LABORATORY blueprint, TmpArea area, RoomInit init) {
		super(blueprint, area, init);
		jobs = new Jobs(this);
		
		employees().neededSet((int) Math.ceil(blueprint.constructor.workers.get(this)));
		employees().maxSet(jobs.size());

		jobs.randomize();
		activate();
		blueprintI().data.incStations(jobs.size());
		pdata = blueprint.industry.makeData();
	}
	
	@Override
	protected void loadFix() {
		pdata = industry().makeDataFix(pdata);
	}
	
	@Override
	public long[] productionData() {
		return pdata;
	}
	
	@Override
	public Industry industry() {
		return blueprintI().industries().get(0);
	}
	
	@Override
	public int industryI() {
		return 0;
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
	}

	@Override
	protected void activateAction() {

	}

	@Override
	protected void deactivateAction() {

	}



	@Override
	protected void dispose() {
		blueprintI().data.incStations(-jobs.size());
	}
	
	@Override
	public JOB_MANAGER getWork() {
		return jobs;
	}
	
	@Override
	public ROOM_LABORATORY blueprintI() {
		return (ROOM_LABORATORY) blueprint();
	}
	
	static class Jobs extends JobPositions<LaboratoryInstance> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Jobs(LaboratoryInstance ins) {
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

}
