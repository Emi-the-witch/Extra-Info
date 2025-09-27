package settlement.tilemap.ground;

import game.VERSION;
import game.debug.Profiler;
import init.text.D;
import settlement.main.SETT;
import settlement.tilemap.TileMap;
import settlement.tilemap.TileMap.SMinimapGetter;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.map.MAP_DOUBLE;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.map.MAP_OBJECTE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.sett.IDebugPanelSett;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

import java.io.IOException;

import static settlement.main.SETT.*;

public class Ground extends TileMap.Resource {

	public static final int MOISTURE_MAX = 15;
	public static final double MOISTURE_MAXI = 1.0 / MOISTURE_MAX;
	public final GroundTypes types;

	public final ColorImp dry = new ColorImp();
	public final ColorImp wet = new ColorImp();

	final Bitsmap1D mapTypes = new Bitsmap1D(0, 4, SETT.TAREA);
	final Bitsmap1D mapMoistureBase = new Bitsmap1D(0, 4, SETT.TAREA);
	final Bitsmap1D mapMoistureCurrent = new Bitsmap1D(0, 4, SETT.TAREA);
	private final Bitmap1D edge = new Bitmap1D(SETT.TAREA, false);
	public final Minables minerals = new Minables();

	public Ground(TileMap m) throws IOException {

		types = new GroundTypes();
		new Debug(this);

		/// ////////////////////#!# ADD Soil type to Debug panel
		{
			for(GroundType GT : types.ALL)
			{
				IDebugPanelSett.add(new PlacableMulti("Soil type " + GT.name) {


					//Make the tile Alluvium, the function for a particular tile.
					@Override
					public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
						MAP.set(tx, ty, GT);
					}


					@Override
					public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
						if (!IN_BOUNDS(tx, ty))
							return "";
						if (PATH().solidity.is(tx, ty))
							return "";
						if (ROOMS().map.is(tx, ty))
							return "";
						return null;
					}
				});
			}
		}
		/// ////////////////////#!#
	}



	@Override
	protected void save(FilePutter saveFile) {
		mapTypes.save(saveFile);
		mapMoistureBase.save(saveFile);
		mapMoistureCurrent.save(saveFile);
		dry.save(saveFile);
		wet.save(saveFile);
		minerals.save(saveFile);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		mapTypes.load(saveFile);
		mapMoistureBase.load(saveFile);
		mapMoistureCurrent.load(saveFile);
		dry.load(saveFile);
		wet.load(saveFile);
		minerals.load(saveFile);
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			setEdge(c.x(), c.y());
		}
		setColors(dry, wet, 1.0);
		cc = -1;
		if (VERSION.versionIsBefore(69, 17)) {
			for (int i = 0; i < SETT.TAREA; i++) {
				if (mapTypes.get(i) > types.PASTURE.index) {
					mapTypes.set(i, mapTypes.get(i)-1);
				}
			}
		}
		
	}
	
	@Override
	protected void clearAll() {
		mapTypes.clear();
		mapMoistureBase.clear();
		mapMoistureCurrent.clear();
	}
	
	private double cc = -1;
	
	@Override
	protected void update(float ds, Profiler profiler) {
		double d = SETT.WEATHER().moisture.getD()*0.4;
		if (d != cc) {
			setColors(dry, wet, d);
		}
	}
	
	public void setColors(COLOR dry, COLOR wet, double moist) {

		cc = moist;
		for (GroundType t : types.ALL) {
			t.setColors(dry, wet, moist);
		}

		this.dry.set(dry);
		this.wet.set(wet);
		
	}
	
	public void adjust(int tile, int tx, int ty) {
		int f = mapMoistureCurrent.get(tile);
		int n = (int) (mapMoistureBase.get(tile) + SETT.ENV().map.WATER_SWEET.get(tile)*MOISTURE_MAX);
		n = CLAMP.i(n, 0, MOISTURE_MAX);
		if (f != n) {
			if (f < n) {
				f++;
			}else
				f--;
			mapMoistureCurrent.set(tile, f);
			update(tx, ty);
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (SETT.IN_BOUNDS(tx, ty, d))
					update(tx+d.x(), ty+d.y());
			}
			
		}
	}
	
	public final MAP_OBJECTE<GroundType> MAP = new MAP_OBJECTE<GroundType>() {
		
		@Override
		public GroundType get(int tile) {
			return types.ALL[mapTypes.get(tile)];
		}

		@Override
		public GroundType get(int tx, int ty) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return types.NORMAL;
			return types.ALL[mapTypes.get(tx+ty*SETT.TWIDTH)];
		}

		@Override
		public void set(int tile, GroundType object) {
			
		}

		@Override
		public void set(int x, int y, GroundType object) {
			if (!SETT.IN_BOUNDS(x, y))
				return;
			GroundType old = get(x, y);
			mapTypes.set(x+y*SETT.TWIDTH, object.index);
			if (old != object) {
				update(x, y);
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					DIR d = DIR.ORTHO.get(i);
					if (SETT.IN_BOUNDS(x, y, d))
						update(x+d.x(), y+d.y());
				}
				SETT.TILE_MAP().miniCUpdate(x, y);
			}
			
		}
	};
	
	public void init() {
		
		SETT.ENV().map.initWater();
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			int tile = c.x()+c.y()*SETT.TWIDTH;
			int n = (int) (mapMoistureBase.get(tile) + SETT.ENV().map.WATER_SWEET.get(tile)*MOISTURE_MAX);
			n = CLAMP.i(n, 0, MOISTURE_MAX);
			mapMoistureCurrent.set(tile, n);
		}
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			setEdge(c.x(), c.y());
		}
		
	}
	
	public final MAP_DOUBLEE MOISTURE_BASE = new MAP_DOUBLEE() {

		@Override
		public double get(int tx, int ty) {
			if (SETT.IN_BOUNDS(tx, ty))
				return get(tx+ty*SETT.TWIDTH);
			return 0;
		}
		
		@Override
		public double get(int tile) {
			return mapMoistureBase.get(tile)*MOISTURE_MAXI;
		}
		
		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			if (SETT.IN_BOUNDS(tx, ty))
				set(tx+ty*SETT.TWIDTH, value);
			return this;
		}
		
		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			int v = CLAMP.i((int) Math.round(value*MOISTURE_MAX), 0, MOISTURE_MAX);
			mapMoistureBase.set(tile, v);
			return this;
		}
	};
	
	public final MAP_DOUBLEE MOISTURE_CURRENT = new MAP_DOUBLEE() {

		@Override
		public double get(int tx, int ty) {
			if (SETT.IN_BOUNDS(tx, ty))
				return get(tx+ty*SETT.TWIDTH);
			return 0;
		}
		
		@Override
		public double get(int tile) {
			return mapMoistureCurrent.get(tile)*MOISTURE_MAXI;
		}
		
		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			
			if (!SETT.IN_BOUNDS(tx, ty))
				return this;;
			
			int v = CLAMP.i((int) Math.round(value*MOISTURE_MAX), 0, MOISTURE_MAX);
			int tile = tx+ty*SETT.TWIDTH;
			int o = mapMoistureCurrent.get(tile);
			if (v != o) {
				mapMoistureCurrent.set(tile, v);
				update(tx, ty);
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					DIR d = DIR.ORTHO.get(i);
					if (SETT.IN_BOUNDS(tx, ty, d))
						update(tx+d.x(), ty+d.y());
				}
				
			}
			
			return this;
		}
		
		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			return set(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
		}
	};
	
	public final MAP_DOUBLE MOISTURE_TOT = new MAP_DOUBLE() {

		@Override
		public double get(int tx, int ty) {
			if (SETT.IN_BOUNDS(tx, ty))
				return get(tx+ty*SETT.TWIDTH);
			return 0;
		}
		
		@Override
		public double get(int tile) {
			return MOISTURE_BASE.get(tile) + SETT.ENV().map.WATER_SWEET.get(tile);
		}

	};
	
	private void setEdge(int tx, int ty) {
		GroundType g = MAP.get(tx, ty);
		int m = mapMoistureCurrent.get(tx+ty*SETT.TWIDTH);
		boolean set = false;
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			int x = tx + DIR.ORTHO.get(di).x();
			int y = ty + DIR.ORTHO.get(di).y();
			if (joins(g, m, x, y)) {
				set = true;
				break;
			}
		}
		edge.set(tx+ty*SETT.TWIDTH, set);
	}
	
	private boolean joins(GroundType g, int m, int x, int y) {
		if (SETT.IN_BOUNDS(x, y)) {
			int i2 = MAP.get(x, y).index;
			
			if (g.index < i2)
				return true;
			return g.index == i2 && m < mapMoistureCurrent.get(x+y*SETT.TWIDTH);
		}
		return false;
	}
	
	private void update(int tx, int ty) {
		setEdge(tx, ty);
		SETT.TILE_MAP().miniCUpdate(tx, ty);
	}

	void render(Renderer r, RenderIterator it) {
		int tile = it.tile();
		int ran = it.ran();
		int x = it.x();
		int y = it.y();
		
		GroundType g = MAP.get(tile);
		int m = mapMoistureCurrent.get(tile);
		g.tmps[m].bind();
		g.sheet.render(r, ran&(GroundTypes.VARS-1), x, y);
		if (edge.get(it.tile())) {

			for (int di = 0; di < 4; di++) {
				DIR d = DIR.ORTHO.get(di);
				
				
				int dx = it.tx() + d.x();
				int dy = it.ty() + d.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					GroundType g2 = MAP.get(dx, dy);
					int m2 = mapMoistureCurrent.get(dx+dy*SETT.TWIDTH);
					if (g.index < g2.index || (g.index == g2.index && m < m2)) {
						g2.tmps[m2].bind();
						ran = ran >> 4;
						
						DIR d2 = d.next(2);
						int t = it.tile()+d2.x()+d2.y()*SETT.TWIDTH;
						if (SETT.IN_BOUNDS(it.tx(), it.ty(), d2) && g2.index == mapTypes.get(t) && m2 == mapMoistureCurrent.get(t)) {
							types.c_masks.renderTextured(g2.sheet.getTexture(ran&(GroundTypes.VARS-1)), 8*d.orthoID()+(ran&7), x, y);
						}else {
							types.s_masks.renderTextured(g2.sheet.getTexture(ran&(GroundTypes.VARS-1)), 8*d.orthoID()+(ran&7), x, y);
						}

					}
				}
			}
			
			
		}
		
		//getter.get(tile).render(r, data[tile]&0x0F, ran, x, y);
		COLOR.unbind();
		
	}
	
	public void render(Renderer r, float ds, ShadowBatch s, RenderData data) {
		RenderIterator i = data.onScreenTiles();

		while (i.has()) {
			render(r, i);
			minerals.render(r, i.tile(), i.ran(), i.x(), i.y());
			i.next();
		}
		
	}

	public void renderMinerals(Renderer r, int tile, int ran, int x, int y) {
		minerals.render(r, tile, ran, x, y);
	}
	
	public TextureCoords getTexture(int tile, int ran) {
		return MAP.get(tile).sheet.getTexture(ran&15);
	}
	
	public static CharSequence ¤¤soilType = "Soil Type";
	public static CharSequence ¤¤moisture = "Moisture";
	public static CharSequence ¤¤moistureB = "Moisture (base)";
	public static CharSequence ¤¤moistureT = "Moisture (target)";
	
	static {
		D.ts(Ground.class);
	}
	
	public void hover(GUI_BOX box, int tx, int ty) {
		GBox b = (GBox) box;
		b.textLL(Str.TMP.clear().add(¤¤soilType).add(':'));
		b.textL(b.text().add(MAP.get(tx, ty).name));
		b.NL();
		b.text(b.text().add(MAP.get(tx, ty).desc));
		b.NL(6);
		b.textLL(¤¤moisture);
		b.tab(6);
		b.add(GFORMAT.perc(b.text(), MOISTURE_CURRENT.get(tx, ty)));
		b.NL();
		b.textLL(¤¤moistureB);
		b.tab(6);
		b.add(GFORMAT.perc(b.text(), MOISTURE_BASE.get(tx, ty)));
		b.NL();
		b.textLL(¤¤moistureT);
		b.tab(6);
		b.add(GFORMAT.perc(b.text(), MOISTURE_TOT.get(tx, ty)));
		b.sep();
		
		if (minerals.getter.is(tx, ty)) {
			b.add(MINERALS().getter.get(tx, ty).resource.icon());
			b.textLL(MINERALS().getter.get(tx, ty).name);
//			b.tab(6);
//			b.add(GFORMAT.perc(b.text(), MINERALS().amountPlayer.get(tx,ty)));
			b.sep();
		}
		
	}
	
	public final SMinimapGetter minimap = new SMinimapGetter() {
		
		@Override
		public COLOR miniColorPimped(ColorImp origional, int x, int y, boolean northern, boolean southern) {
			if (minerals.getter.is(x, y)) {
				return minerals.miniC(origional, MAP.get(x, y).miniC, x, y);
			}
			
			for (DIR d : DIR.ALL) {
				if (TERRAIN().WATER.is.is(x+d.x(), y+d.y()) || TERRAIN().MOUNTAIN.is(x+d.x(), y+d.y())) {
					origional.shadeSelf(0.75);
					return origional;
				}
			}
			if (northern || southern)
				origional.shadeSelf(0.9);
			return origional;
		}
		
		@Override
		public COLOR miniC(int x, int y) {
			return MAP.get(x, y).miniC;
		}
	};
	
}
