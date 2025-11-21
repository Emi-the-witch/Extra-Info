package settlement.room.food.farm;

import game.time.TIME;
import init.resources.RESOURCE;
import settlement.entity.animal.ANIMAL_ROOM_RUINER;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.JOB_MANAGER;
import settlement.misc.job.SETT_JOB;
import settlement.path.AVAILABILITY;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.industry.module.ROOM_PRODUCER_INSTANCE;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.construction.ConstructionInit;
import settlement.room.main.job.JobIterator;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;

import static settlement.main.SETT.ROOMS;

final public class FarmInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_PRODUCER_INSTANCE,ANIMAL_ROOM_RUINER {

	private static final long serialVersionUID = 1L;
	private long[] produceData;
	final Tile.IData tData = new Tile.IData(this);
	
	public double irri = 0;
	private double irriNext = 0;
	private short irriI = 0;
	
	private final JobIterator jobmanager = new JobIterator(this) {
		private static final long serialVersionUID = 1L;

		@Override
		protected SETT_JOB init(int tx, int ty) {
			return blueprintI().tile(tx, ty).job();
		}
	};

	FarmInstance(ROOM_FARM p, TmpArea area, RoomInit init) {
		super(p, area, init);

		for (COORDINATE c : body()) {
			if (is(c)) {
				irri += SETT.ENV().map.WATER_SWEET.get(c);
				p.tile(c.x(), c.y()).init(c, this);
			}
			
		}
		double w = Math.ceil(p.constructor.workers.get(this));
		int jobs = (int) Math.ceil(w);
		employees().maxSet((int) (jobs*1.5));
		employees().neededSet(jobs);
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
			tData.updateDay();
		}
		
	}

	@Override
	protected boolean renderBelow(Renderer r, ShadowBatch shadowBatch, RenderIterator i) {
		blueprintI().tile(i.tx(), i.ty()).renderTill(r, shadowBatch, i);
		return false;
	}

	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderIterator it) {
		blueprintI().tile(it.tx(), it.ty()).render(r, shadowBatch, it);
		return false;
	}
	
	public RESOURCE getCrop() {
		return blueprintI().crop.resource;
	}

	@Override
	public boolean canBeGraced(int tx, int ty) {
		return blueprintI().tile(tx, ty).destroyTileCan();
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
	public ROOM_FARM blueprintI() {
		return (ROOM_FARM) blueprint();
	}

	@Override
	public boolean acceptsWork() {
		return true;
	}

	@Override
	protected AVAILABILITY getAvailability(int tile) {
		return AVAILABILITY.ROOM;
	}

	@Override
	public void destroyTile(int tx, int ty) {
		if (destroyTileCan(tx, ty)) {
			blueprintI().tile(tx, ty).destroyTile();
		}
	}

	@Override
	public boolean destroyTileCan(int tx, int ty) {
		return blueprintI().tile(tx, ty).destroyTileCan();
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
	
	public void changeTo(ROOM_FARM f) {
		ConstructionInit init = new ConstructionInit(0, f.constructor, null, 0, makeState(mX(), mY(), true));
		TmpArea a = remove(mX(), mY(), false, this, true);
		
		ROOMS().construction.createClean(a, init);
		
	}
	
	@Override
	public void updateTileDay(int tx, int ty) {
		blueprintI().tile(tx, ty).updateDay();
		if (irriI >= area()) {
			irri = irriNext;
			irriNext = 0;
			irriI = 0;
		}
		irriI++;
		irriNext += SETT.GROUND().MOISTURE_CURRENT.get(tx, ty);
		
	}
	
	@Override
	public double productionRate(RoomInstance ins, Humanoid h, Industry in, IndustryResource oo) {
		if (employees().employed() == 0)
			return 0;
		return Util.prospect((FarmInstance) ins)/(employees().employed()*TIME.years().bitConversion(TIME.days()));
	}

}
