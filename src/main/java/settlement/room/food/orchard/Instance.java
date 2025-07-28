package settlement.room.food.orchard;

import settlement.entity.animal.ANIMAL_ROOM_RUINER;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.JOB_MANAGER;
import settlement.misc.job.SETT_JOB;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.ROOM_PRODUCER;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.construction.ConstructionInit;
import settlement.room.main.job.JobIterator;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

import static settlement.main.SETT.ROOMS;

final public class Instance extends RoomInstance implements JOBMANAGER_HASER, ROOM_PRODUCER, ANIMAL_ROOM_RUINER {

	private static final long serialVersionUID = 1L;
	private long[] produceData;
	private double skill;
	private double skillPrev;
	private int skillI;
	public final float base;
	public float irri;
	private float irriNext;
	private short irriI;
	
	
	private final JobIterator jobmanager = new JobIterator(this) {
		private static final long serialVersionUID = 1L;

		@Override
		protected SETT_JOB init(int tx, int ty) {
			OTile t = blueprintI().tile(tx, ty);
			if (t != null)
				return t.job();
			return null;
		}
	};

	Instance(ROOM_ORCHARD p, TmpArea area, RoomInit init) {
		super(p, area, init);
		double t = 0;
		for (COORDINATE c : body()) {
			if (is(c)) {
				irri += SETT.ENV().map.WATER_SWEET.get(c);
				if (p.tile.init(c.x(), c.y(), this))
					t ++;
			}
			
		}
		base = (float) (t/ROOM_ORCHARD.TILES_PER_WORKER);
		int jobs = (int) Math.ceil(base);
		employees().maxSet((int) (jobs*1.25));
		employees().neededSet((int) jobs);
		produceData = p.productionData.makeData();
		activate();
		
		
	}

	@Override
	protected void loadFix() {
		produceData = blueprintI().productionData.makeDataFix(produceData);
	}
	
	
	@Override
	protected void updateAction(double updateInterval, boolean day) {

		blueprintI().productionData.updateRoom(this);
		
		if (day) {
			jobmanager.searchAgain();
			
			if (blueprintI().time.isDeadDay()) {
				skillPrev = skill();
				skill = 0;
				skillI = 0;
			}
			
		}
		
	}

	
	@Override
	protected boolean canRemoveAndRemoveAction(int tx, int ty, boolean scatter, Object obj, boolean forced) {
		if (scatter) {
			for (COORDINATE c : body()) {
				if (is(c)) {
					OTile t = blueprintI().tile.getM(c.x(), c.y());
					if (t != null)
						t.chop();
				}
				
			}
		}
		return true;
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		int d = SETT.ROOMS().fData.spriteData2.get(it.tile());
		if (d != 0) {
			blueprintI().constructor.sEdge.render(r, shadowBatch, d, it, 0, false);
		}
		
		return super.render(r, shadowBatch, it);
	}
	
	@Override
	public boolean canBeGraced(int tx, int ty) {
		OTile t = blueprintI().tile(tx, ty);
		return t != null && t.destroyTileCan();
	}

	@Override
	public void grace(int tx, int ty) {
		blueprintI().tile(tx, ty).destroyTile();
	}

	@Override
	protected void activateAction() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void deactivateAction() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public JOB_MANAGER getWork() {
		return jobmanager;
	}

	@Override
	public ROOM_ORCHARD blueprintI() {
		return (ROOM_ORCHARD) blueprint();
	}

	@Override
	public boolean acceptsWork() {
		return true;
	}

	@Override
	public void destroyTile(int tx, int ty) {
		if (destroyTileCan(tx, ty)) {
			blueprintI().tile(tx, ty).destroyTile();
		}
	}

	@Override
	public boolean destroyTileCan(int tx, int ty) {
		OTile t = blueprintI().tile(tx, ty);
		return t != null && t.destroyTileCan();
	}

	@Override
	public ROOM_DEGRADER degrader(int tx, int ty) {
		return null;
	}

	@Override
	public long[] productionData() {
		return produceData;
	}


	@Override
	public Industry industry() {
		return blueprintI().industries().get(0);
	}


	@Override
	public int industryI() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void incSkill(double skill) {
		this.skill += skill;
		this.skillI ++;
	}
	
	public void changeTo(ROOM_ORCHARD f) {
		ConstructionInit init = new ConstructionInit(0, f.constructor, null, 0, makeState(mX(), mY(), false));
		TmpArea a = remove(mX(), mY(), false, this, true);
		
		ROOMS().construction.createClean(a, init);
		
	}
	
	@Override
	public void updateTileDay(int tx, int ty) {
		
		OTile t = blueprintI().tile(tx, ty);
		if (t != null)
			t.updateDay();
		
		if (irriI >= area()) {
			irri = irriNext;
			irriNext = 0;
			irriI = 0;
		}
		irriI ++;
		irriNext += SETT.GROUND().MOISTURE_CURRENT.get(tx, ty);
		
		
	}
	

	public double skill() {
		if (skillI == 0)
			return skillPrev;
		return skill/skillI;
	}


	public boolean event() {
		boolean ff = false;
		for (COORDINATE c : body()) {
			if (is(c) && RND.rBoolean()) {
				OTile t = blueprintI().tile.getM(c.x(), c.y());
				if (t != null)
					ff |= t.kill();
			}
			
		}
		return ff;
	}
	
}
