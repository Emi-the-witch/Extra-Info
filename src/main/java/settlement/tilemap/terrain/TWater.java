package settlement.tilemap.terrain;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.TERRAIN;
import static settlement.main.SETT.TWIDTH;

import java.io.IOException;

import game.audio.AUDIO;
import game.audio.SoundRace;
import game.time.TIME;
import init.constant.C;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.settings.S;
import init.sprite.SPRITES;
import init.sprite.UI.Icons.S.IconS;
import init.sprite.game.SheetPair;
import init.sprite.game.SheetType;
import init.type.TERRAIN;
import init.type.TERRAINS;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.path.AVAILABILITY;
import settlement.tilemap.ground.GroundType;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.MATH;
import snake2d.util.bit.Bits;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.color.OpacityImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_DOUBLE;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.Bitsmap2D;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.TileTexture.TileTextureScroller;
import util.GUTIL;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;
import util.text.D;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public class TWater {

	private final Sprites sprites;
	protected static String ¤¤name = "¤Water";
	protected static String ¤¤nameDeep = "¤Deep Water";
	protected static String ¤¤nameBridge = "¤Bridge";

	static {
		D.ts(TWater.class);
	}

	public final TerrainTile DEEP;
	public final TerrainTile SHALLOW;
	public final TerrainTile BRIDGE;
	private final Terrain shared;

	private final static Bits bradius = new Bits(	0b1111_0000_0000_0000);
	private final int reserveBit = 					0b0000_0001_0000_0000;


	private  int iceI = 0;

	public final Bitmap2D groundWater = new Bitmap2D(SETT.TILE_BOUNDS, false);
	public final Bitmap2D groundWaterSalt = new Bitmap2D(SETT.TILE_BOUNDS, false);
	public final Bitmap2D deepSeaFishSpot = new Bitmap2D(SETT.TILE_BOUNDS, false);
	public final Bitsmap2D fishAmount = new Bitsmap2D(0, 4, SETT.TILE_BOUNDS);
	public TWater(Terrain t) throws IOException {
		this.sprites = new Sprites();




		DEEP = new Deep(t);
		SHALLOW = new Shallow(t);
		BRIDGE = new Bridge(t);
		shared = t;

		placer(groundWater, "Water Fresh");
		placer(groundWaterSalt, "Water Salt");
		placer(deepSeaFishSpot, "Water deep sea spot");

/// ///////// #!# Only section changed, adding fish icons to the debug!
		for (int bit = 0; bit < 16; bit ++){
			String name = "Fish " + String.format("%02d", bit);;
			placer(fishAmount, name, bit );
		}
	}

		private void placer(Bitsmap2D map, String name, int bit) {
			PLACABLE undo = new PlacableMulti(name + " undo") {
				@Override
				public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
					map.set(tx, ty, bit);
				}
				@Override
				public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
					return map.is(tx, ty) ? null : E;
				}
			};
			PLACABLE p = new PlacableMulti(name) {
				@Override
				public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
					map.set(tx, ty, bit);

				}

				@Override
				public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {

					return null;
				}
				@Override
				public PLACABLE getUndo() {
					return undo;
				}
			};
			IDebugPanelSett.add(p);
		}
		/////////////////////////////////////////// #!#

	private void placer(Bitmap2D map, String name) {

		PLACABLE undo = new PlacableMulti(name + " undo") {

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				map.set(tx, ty, false);
			}

			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return map.is(tx, ty) ? null : E;
			}
		};

		PLACABLE p = new PlacableMulti(name) {

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				map.set(tx, ty, true);
			}

			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}

			@Override
			public PLACABLE getUndo() {
				return undo;
			}
		};

		IDebugPanelSett.add(p);

	}

	private abstract static class Clear extends TerrainClearing {

		private final SoundRace sound = AUDIO.race("CLEAR_WATER");
		@Override
		public int clearAll(int tx, int ty) {
			return 0;
		}

		@Override
		public SoundRace sound(int tx, int ty) {
			return sound;
		}

		@Override
		public double strength() {
			return 2000*C.TILE_SIZE;
		};

		@Override
		public boolean isEasilyCleared() {
			return false;
		}

	}

	public MAP_DOUBLE radius = new MAP_DOUBLE() {

		private final double ri = 1.0/16;

		@Override
		public double get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH);
			return 0;
		}

		@Override
		public double get(int tile) {
			return (bradius.get(shared.data.get(tile))+1)*ri;
		}
	};

	public final MAP_OBJECT<FINDABLE> service = new MAP_OBJECT<FINDABLE>() {

		private int sx,sy;
		private final FINDABLE service = new FINDABLE() {

			@Override
			public int y() {
				return sy;
			}

			@Override
			public int x() {
				return sx;
			}

			@Override
			public boolean findableReservedIs() {
				return (shared.data.get(sx, sy) & reserveBit) == reserveBit;
			}

			@Override
			public boolean findableReservedCanBe() {
				return (shared.data.get(sx, sy) & reserveBit) != reserveBit;
			}

			@Override
			public void findableReserveCancel() {
				if (findableReservedIs()) {
					PATH().finders.water.report(sx, sy, 1);

				}
				shared.data.set(sx, sy, (shared.data.get(sx, sy) & ~reserveBit));
			}

			@Override
			public void findableReserve() {
				if (findableReservedCanBe()) {
					PATH().finders.water.report(sx, sy, -1);

				}
				shared.data.set(sx, sy, (shared.data.get(sx, sy) | reserveBit));
			}
		};
		@Override
		public FINDABLE get(int tile) {
			return get(tile%SETT.TWIDTH, tile/SETT.THEIGHT);
		}
		@Override
		public FINDABLE get(int tx, int ty) {
			if (SHALLOW.is(tx, ty)) {
				int d = shared.data.get(tx, ty);
				if ((d & 0x0F) == 0x0F) {
					sx = tx;
					sy = ty;
					return service;
				}
			}
			return null;
		}

	};

	private boolean setRadiusAndData(int old, int data, int x, int y) {
		double am = 0;
		int i = 0;
		while (GUTIL.circle().radius(i) < 4) {
			int dx = x+GUTIL.circle().get(i).x();
			int dy = y+GUTIL.circle().get(i).y();
			if (is.is(dx, dy))
				am++;
			i++;
		}
		int ra = (int) (0x0F*CLAMP.d(2.0*am/i, 0, 1));

		int n = bradius.set(data, ra);
		shared.data.set(x, y, n);

		return bradius.get(old) != bradius.get(n);
	}

	public MAP_BOOLEAN open = new MAP_BOOLEAN() {

		@Override
		public boolean is(int tx, int ty) {
			if (SHALLOW.is(tx, ty)) {
				return (shared.data.get(tx, ty) & 0x0F) == 0x0F;
			}
			return DEEP.is(tx, ty);
		}

		@Override
		public boolean is(int tile) {
			if (SHALLOW.is(tile)) {
				return (shared.data.get(tile) & 0x0F) == 0x0F;
			}
			return DEEP.is(tile);
		}
	};


	private int getCode(int x, int y) {
		int m = 0;
		for (DIR d : DIR.ORTHO) {
			if (is.is(x+d.x(), y+d.y()) || !SETT.IN_BOUNDS(x,y,d))
				m |= d.mask();
		}
		return m;
	}

	private int getCodeCorner(int m, int x, int y) {

		int c = 0;
		for (DIR d : DIR.NORTHO) {
			if ((m & d.next(-1).mask()) != 0 && (m & d.next(1).mask()) != 0 && !is.is(x+d.x(), y+d.y())) {
				c |= d.mask();
			}
		}
		return c;
	}

	public MAP_BOOLEAN ice = new MAP_BOOLEAN() {

		@Override
		public boolean is(int tx, int ty) {
			return iceI > (GUTIL.ran2().get(tx+ty*TWIDTH)&0x0FFFF);
		}

		@Override
		public boolean is(int tile) {
			return iceI > (GUTIL.ran2().get(tile)&0x0FFFF);
		}
	};

	public MAP_BOOLEAN deepIs = new MAP_BOOLEAN() {

		@Override
		public boolean is(int tx, int ty) {
			return DEEP.is(tx, ty) || BRIDGE.is(tx, ty);
		}

		@Override
		public boolean is(int tile) {
			return DEEP.is(tile) || BRIDGE.is(tile);
		}
	};

	public void renderIce(RenderIterator i, int mask) {
		sprites.renderIce(mask, 0, i);
	}

	void update(double ds) {
		sprites.update(ds);
		iceI = (int) (SETT.WEATHER().ice.getD()*0x01FFFF)*S.get().downpour.get();
	}

	private final Minimap mini = new Minimap();

	public MAP_BOOLEAN is = new MAP_BOOLEAN() {

		@Override
		public boolean is(int tx, int ty) {
			return SHALLOW.is(tx,ty) || DEEP.is(tx, ty) || BRIDGE.is(tx, ty);
		}

		@Override
		public boolean is(int tile) {
			return SHALLOW.is(tile) || DEEP.is(tile)  || BRIDGE.is(tile);
		}
	};


	public void renderOverlayed(RenderData.RenderIterator i) {
		sprites.renderTexture(i);
	}

	private static class Minimap {


		private final int w = 64;
		private final int h = 32;
		private byte[][] values = new byte[h][w];

		Minimap() {

			for (int i = 0; i < h; i++) {
				drawWave(i, (byte) (24-(i&0b11)*16));
			}
		}

		private void drawWave(int sy, byte v) {

			double period = 5;
			double bend = 3;

			for(int i = 0; i < w; i++) {
				int x = i;
				double d = period*(i/(double)w)*Math.PI;
				int y = sy + (int) (bend*Math.sin(d));
				y &= h-1;
				values[y][x] += v;
			}
		}

		COLOR miniCPimp(ColorImp c, int x, int y, boolean northern, boolean southern) {
			x &= w-1;
			y &= h-1;
			if (values[y][x] != 0) {
				double v = 1.0-(0.5*values[y][x]/128.0);
				c.shadeSelf(v);
			}
			return c;
		}

	}

	private final class Shallow extends TerrainTile {

		private final LIST<SheetPair> ontop;

		private Shallow (Terrain t) throws IOException {
			super("WATER_SHALLOW", t, ¤¤name, sprites.icon, t.colors.minimap.water);
			ontop = SPRITES.GAME().sheets(SheetType.s1x1, new Json(PATHS.CONFIG().get("SETT_MAP_DECORATION")).json("WATER_SWEET_1X1"));
		}

		private TerrainClearing clearing = new Clear() {


			@Override
			public RESOURCE clear1(int tx, int ty) {
				shared.NADA.placeFixed(tx, ty);
				return null;
			}

			@Override
			public boolean can() {
				return true;
			}

		};

		@Override
		void unplace(int tx, int ty) {
			if (service.get(tx, ty) != null && service.get(tx, ty).findableReservedCanBe())
				PATH().finders.water.report(tx, ty, -1);
		}

		@Override
		public TerrainClearing clearing() {
			return clearing;
		}

		@Override
		public COLOR miniColorPimped(ColorImp c, int x, int y, boolean northern, boolean southern) {
			return mini.miniCPimp(c, x, y, northern, southern);
		}

		@Override
		public boolean isPlacable(int tx, int ty) {
			return true;
		}

		@Override
		protected boolean place(int x, int y) {


			if (IN_BOUNDS(x, y)) {
				return setCode(x, y);
			}
			return false;
		}

		private boolean setCode(int x, int y) {


			int old = shared.data.get(x, y);
			super.placeRaw(x, y);
			int c = getCode(x, y);
			c |= getCodeCorner(c, x, y) << 4;
			for (DIR d : DIR.ORTHO) {
				if (DEEP.is(x+d.x(), y+d.y()) || BRIDGE.is(x+d.x(), y+d.y())) {
					break;
				}
			}
			boolean ret = setRadiusAndData(old, c, x, y);
			if (service.get(x, y) != null && service.get(x, y).findableReservedCanBe())
				PATH().finders.water.report(x, y, 1);
			return ret;
		}

		private int getCode(int x, int y) {
			int m = 0;
			for (DIR d : DIR.ORTHO) {
				if (is.is(x+d.x(), y+d.y()) || !SETT.IN_BOUNDS(x,y,d))
					m |= d.mask();
			}
			return m;
		}

		private int getCodeCorner(int m, int x, int y) {

			int c = 0;
			for (DIR d : DIR.NORTHO) {
				if ((m & d.next(-1).mask()) != 0 && (m & d.next(1).mask()) != 0 && !is.is(x+d.x(), y+d.y())) {
					c |= d.mask();
				}
			}
			return c;
		}

		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {

			return false;
		}

		@Override
		protected boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			sprites.render(data&0x0F, (data>>4)&0x0F, i, sprites.shore, sprites.normal);
			sprites.renderTexture(i);
			COLOR.unbind();
			i.countWater();

			return (data&0x0F) == 0x0F;

		}

		@Override
		protected boolean renderMid(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it, int data) {
			sprites.above(data, 0, it);
			if (isIce(it.tile())) {
				int m = 0;
				for (DIR d : DIR.ORTHO) {
					if (isIce(it, d) || !SETT.IN_BOUNDS(it.tx(),it.ty(),d))
						m |= d.mask();
				}
				sprites.renderIce(m, 0, it);
				return m == 0x0F;
			}else if ((data&0x0F) == 0x0F && groundWater.is(it.tile()) && (GUTIL.ran2().get(it.tile())&0x07) == 0) {

				if (ontop.size() == 0)
					return false;

				int ran = it.ran();
				SheetPair sheet = ontop.getC(ran);
				if (sheet == null)
					return false;
				SETT.TERRAIN().colors.tree.get(it.ran()).bind();
				//sheet.d.color(ran).bind();
				ran = ran>>4;

				int frame = sheet.d.frame(it.ran(), 1.0);
				int tile = SheetType.s1x1.tile(sheet.s, sheet.d, 0, frame, ran&0b11);

				int x = it.x();
				int y = it.y();
				{
					ran = GUTIL.ran2().get(it.tile());
					double sp = 10.0/(1+(ran&0b1111));
					ran = ran >> 4;
					int f = (ran & 0b1111) + (int) (sp*TIME.currentSecond());
					ran = ran >> 4;
					int df = MATH.distanceC(8, f, 16);
					x += df;

					sp = 10.0/(1+(ran&0b1111));
					ran = ran >> 4;
					f = (ran & 0b1111) + (int) (sp*TIME.currentSecond());
					ran = ran >> 4;
					df = MATH.distanceC(8, f, 16);
					y += df;

				}

				sheet.s.render(sheet.d, x, y, it, r, tile, ran, 0);
				COLOR.unbind();
				if (s != null)
					sheet.s.renderShadow(sheet.d, x, y, it, s, tile, ran);
				return false;

			}
			return false;
		}

		private boolean isIce(RenderIterator i, DIR d) {
			return is(i.tx(), i.ty(), d) && isIce(i.tx()+d.x(), i.ty()+d.y());
		}

		private boolean isIce(int tx, int ty) {
			return iceI > (GUTIL.ran1().get(tx+ty*TWIDTH)&0x0FFFF) && !SETT.ROOMS().map.is(tx, ty);
		}

		private boolean isIce(int tile) {
			return iceI > (GUTIL.ran1().get(tile)&0x0FFFF) && !SETT.ROOMS().map.is(tile);
		}

//		public void renderIce(RenderIterator i, int mask) {
//			sprites.renderIce(mask, 0, i);
//		}

		@Override
		public AVAILABILITY getAvailability(int x, int y) {
			return AVAILABILITY.PENALTY3;
		}

		@Override
		public void hoverInfo(GBox box, int tx, int ty) {
			super.hoverInfo(box, tx, ty);
			if (S.get().developer)
				box.add(box.text().add(radius.get(tx, ty)));
		}

		@Override
		public TERRAIN terrain(int tx, int ty) {
			if (groundWaterSalt.is(tx, ty))
				return TERRAINS.OCEAN();
			return TERRAINS.WET();
		}
	}

	private abstract class DeepAbs extends TerrainTile{

		DeepAbs(String key, Terrain t, CharSequence name, COLOR mini) {
			super(key, t, name, sprites.icon,mini);

		}

		@Override
		protected boolean place(int x, int y) {
			int old = shared.data.get(x, y);
			super.placeRaw(x, y);
			int m = 0;
			boolean sh = false;
			for (DIR d : DIR.ORTHO) {
				if (joins(x+d.x(), y+d.y()) || !SETT.IN_BOUNDS(x,y,d)) {
					m |= d.mask();
				}else if (SHALLOW.is(x+d.x(), y+d.y())) {
					sh = true;
				}
			}
			int c = 0;
			for (DIR d : DIR.NORTHO) {
				if ((m & d.next(-1).mask()) != 0 && (m & d.next(1).mask()) != 0 && !joins(x+d.x(), y+d.y())) {
					c |= d.mask();
				}
			}
			m |= c << 4;
			if (sh)
				m |= 0b1_0000_0000;
			return setRadiusAndData(old, m, x, y);
		}


		private boolean joins(int tx, int ty) {
			return TERRAIN().get(tx, ty) instanceof DeepAbs;
		}

		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			return false;
		}

		@Override
		protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			int d = 0;
			if ((data & 0b1_0000_0000) != 0) {
				d = TWater.this.getCode(i.tx(), i.ty());
				int c = TWater.this.getCodeCorner(d, i.tx(), i.ty());
				sprites.render(d, c, i, sprites.shore, sprites.normal);
			}

			sprites.render(data&0x0F, (data >> 4)&0x0F, i, sprites.normal, sprites.deep);
			sprites.renderTexture(i);
			COLOR.unbind();
			i.countWater();

			return ((data|d) &0x0F) == 0x0F;
		}

		@Override
		public boolean isPlacable(int tx, int ty) {
			return true;
		}

		@Override
		public int miniDepth() {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public TERRAIN terrain(int tx, int ty) {
			if (groundWaterSalt.is(tx, ty))
				return TERRAINS.OCEAN();
			return TERRAINS.WET();
		}

	}

	private final class Deep extends DeepAbs{

		Deep(Terrain t) {
			super("WATER_DEEP", t, ¤¤nameDeep, t.colors.minimap.water_deep);

		}

		private TerrainClearing clearing = new Clear() {


			@Override
			public RESOURCE clear1(int tx, int ty) {
				BRIDGE.placeFixed(tx, ty);
				return null;
			}

			@Override
			public boolean can() {
				return true;
			}

			@Override
			public boolean canDestroy(int tx, int ty) {
				return true;
			};

			@Override
			public void destroy(int tx, int ty) {
				BRIDGE.placeFixed(tx, ty);
			};

			@Override
			public boolean needs() {
				return true;
			};
		};

		@Override
		public AVAILABILITY getAvailability(int x, int y) {
			return AVAILABILITY.NOT_ACCESSIBLE;
		}

		@Override
		public boolean isPlacable(int tx, int ty) {
			return true;
		}

		@Override
		public TerrainClearing clearing() {
			return clearing;
		}

		@Override
		public int miniDepth() {
			// TODO Auto-generated method stub
			return 1;
		}


		@Override
		public COLOR miniColorPimped(ColorImp c, int x, int y, boolean northern, boolean southern) {
			if ((shared.data.get(x, y) & 0b1_0000_0000) != 0)
				c.interpolate(c, SHALLOW.miniC, 0.5);

			return mini.miniCPimp(c, x, y, northern, southern);
		}
	}

	private final class Bridge extends DeepAbs{

		Bridge(Terrain t) {
			super("WATER_BRIDGE", t, ¤¤nameDeep, null);

		}

		private TerrainClearing clearing = new Clear() {


			@Override
			public RESOURCE clear1(int tx, int ty) {
				DEEP.placeFixed(tx, ty);
				return null;
			}

			@Override
			public boolean can() {
				return false;
			}

			@Override
			public boolean canDestroy(int tx, int ty) {
				return false;
			};

			@Override
			public boolean needs() {
				return true;
			};
		};



		@Override
		protected boolean renderMid(SPRITE_RENDERER r, ShadowBatch s, RenderIterator i, int data) {

			if (SETT.FLOOR().getter.get(i.tile()) == null) {
				GroundType d = SETT.GROUND().MAP.get(i.tile());
				d.col(i.tile()).bind();
				TextureCoords tex = SETT.GROUND().getTexture(i.tile(), i.ran());
				sprites.single_stencil.renderTextured(tex, i.ran()&0x0F, i.x(), i.y());
				sprites.bridgeRaw.render(r, i.ran()&0x0F, i.x(), i.y());
				OPACITY.O99.bind();
				sprites.bridgeRaw.renderTextured(tex, i.ran()&0x0F, i.x(), i.y());
				OPACITY.unbind();
				COLOR.unbind();
			}else {


//				int mask = smask(i);
//				for (DIR d : DIR.ORTHO) {
//					if (is(i.tx(), i.ty(), d) || !SETT.PATH().solidity.is(i.tx(), i.ty(), d)) {
//						mask |= d.mask();
//					}
//				}
//				if (mask != 0x0F) {
//					s.setDistance2Ground(12).setHeight(0);
//					SETT.FLOOR().renderSimple(s, i, SETT.FLOOR().getter.get(i.tile()));
//				}

				SETT.FLOOR().renderSimple(r, i, SETT.FLOOR().getter.get(i.tile()));

//				int smask = smask(i);
//				if (smask != 0x0F) {
//					sprites.bridge.render(r, 16 + smask, i.x(), i.y());
//				}
//
//				int scorner = 0;
//				for (DIR d : DIR.ORTHO) {
//					if (is(i.tx(), i.ty(), d) && is(i.tx(), i.ty(), d.next(2)) && !isFloored(i.tx(), i.ty(), d.next(1)) && !SETT.PATH().solidity.is(i.tx(), i.ty(), d.next(1))) {
//						scorner |= d.mask();
//					}
//				}
//
//				if (scorner != 0) {
//					sprites.bridge.render(r, 48 + scorner, i.x(), i.y());
//				}

				return true;

			}




			return false;
		}

//		private int smask(RenderIterator i) {
//			int smask = 0;
//			for (DIR d : DIR.ORTHO) {
//				if ((is(i.tx(), i.ty(), d) || !SETT.PATH().solidity.is(i.tx(), i.ty(), d)) && !isFloored(i.tx(), i.ty(), d)) {
//					smask |= d.mask();
//				}
//			}
//			smask = ~smask&0x0F;
//			return smask;
//		}


		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator i, int data) {
			if (SETT.FLOOR().getter.get(i.tile()) == null)
				return false;
			int mask = 0;

			for (DIR d : DIR.ORTHO) {
				if (is(i.tx(), i.ty(), d) || !SETT.PATH().solidity.is(i.tx(), i.ty(), d)) {
					mask |= d.mask();
				}
			}



//			int scorner = 0;
//			for (DIR d : DIR.ORTHO) {
//				if (isFloored(i.tx(), i.ty(), d) && isFloored(i.tx(), i.ty(), d.next(2)) && !isFloored(i.tx(), i.ty(), d.next(1))) {
//					scorner |= d.mask();
//				}
//			}
//
//
//			if (scorner != 0) {
//				int off = 32;
//				if (mask == 0x0F)
//					off+=16;
//				sprites.bridge.render(r, off + scorner, i.x(), i.y());
//			}

			sprites.bridge.render(r, mask, i.x(), i.y());



			s.setDistance2Ground(0).setHeight(4);
			sprites.bridge.render(s, mask, i.x(), i.y());

			return false;
		}

//		private boolean isFloored(int tx, int ty, DIR d) {
//			return is(tx, ty, d) && SETT.FLOOR().getter.get(tx, ty, d) != null;
//		}

		@Override
		public AVAILABILITY getAvailability(int x, int y) {
			return AVAILABILITY.NORMAL;
		}

		@Override
		public boolean isPlacable(int tx, int ty) {
			return true;
		}

		@Override
		public TerrainClearing clearing() {
			return clearing;
		}

		@Override
		public int miniDepth() {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public COLOR miniColorPimped(ColorImp c, int x, int y, boolean northern, boolean southern) {
			return null;
		}
	}


	private static class Sprites {

		private final TILE_SHEET stencil;
		private final TILE_SHEET animation;
		private final TILE_SHEET animation_corner;
		private final TILE_SHEET single_stencil;
		private final TILE_SHEET single_animation;
		private final TILE_SHEET ice;
		private final TILE_SHEET ice_fulls;
		private final TILE_SHEET edge;
		private final TILE_SHEET bridgeRaw;
		private final TILE_SHEET bridge;
		public final ColorImp shore = new ColorImp(10, 40, 100);
		public final ColorImp normal = shore.shade(0.5);
		public final ColorImp deep = normal.shade(0.5);

		private short shoreOff = 0;
		private double waterTimer;
		private int shoreDir;

		private final TileTextureScroller dis1 = SPRITES.textures().dis_big.scroller(1.1, -1.1);
		private final TileTextureScroller dis2 = SPRITES.textures().dis_tiny.scroller(-0.8, 0.8);
		private final TileTextureScroller tex1 = SPRITES.textures().bumps.scroller(-1, -1);
		private final TileTextureScroller tex2 = SPRITES.textures().water.scroller(1.5, 1.5);
		private final OpacityImp o2 = new OpacityImp((int) (255*0.25));

		private final int[] offs = new int[16];

		final SPRITE icon;

		Sprites() throws IOException {

			for (int i = 0; i < offs.length; i++) {
				int k = i;
				if (i > 7) {
					k = 7 - (i-7);
					if (k < 0)
						k = 0;
				}
				offs[i] = k;
			}

			stencil = new ITileSheet(PATHS.SPRITE_SETTLEMENT_MAP().get("Water"), 576, 300) {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.init(0, 0, 4, 1, d.s16);
					for (int i = 0; i < 4; i++)
						s.house.setVar(i).paste(true);
					return d.s16.saveGame();
				}

			}.get();

			animation = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.init(0, s.house.body().y2(), 4, 2, d.s16);
					for (int i = 0; i < 8; i++)
						s.house.setVar(i).paste(true);
					return d.s16.saveGame();
				}

			}.get();

			animation_corner = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					for (int i = 0; i < 8; i+=2)
						s.house.setVar(i).pasteEdges(true);

					return d.s16.saveGame();
				}

			}.get();

			single_stencil = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, s.house.body().y2(), 1, 1, 16, 1, d.s16);
					s.full.paste(true);
					return d.s16.saveGame();
				}

			}.get();

			single_animation = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, s.full.body().y2(), 1, 1, 16, 1, d.s16);
					s.full.paste(true);
					return d.s16.saveGame();
				}

			}.get();


			ice = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.init(0, s.full.body().y2(), 4, 1, d.s16);
					for (int i = 0; i < 4; i++)
						s.house.setVar(i).paste(true);
					return d.s16.saveGame();
				}

			}.get();

			ice_fulls = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, s.house.body().y2(), 1, 1, 16, 1, d.s16);
					s.full.paste(true);
					return d.s16.saveGame();
				}

			}.get();

			edge = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.init(0, s.full.body().y2(), 4, 1, d.s16);
					for (int i = 0; i < 4; i++)
						s.house.setVar(i).paste(true);
					return d.s16.saveGame();
				}

			}.get();

			bridgeRaw = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, s.house.body().y2(), 1, 1, 16, 1, d.s16);
					s.full.paste(true);
					return d.s16.saveGame();
				}

			}.get();

			bridge = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.init(0, s.full.body().y2(), 4, 1, d.s16);
					s.house.setVar(0).paste(true);
					s.house.setVar(1).paste(true);
					s.house.setVar(2).pasteEdges (true);
					s.house.setVar(3).pasteEdges(true);

					return d.s16.saveGame();
				}

			}.get();

			icon = new SPRITE.Imp(IconS.L) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {

					int[] dx = new int[] {0, IconS.L/2, 0, IconS.L/2};
					int[] dy = new int[] {0, 0, IconS.L/2, IconS.L/2};
					int[] mm = new int[] {DIR.S.mask() | DIR.E.mask(), DIR.S.mask() | DIR.W.mask(), DIR.N.mask() | DIR.E.mask(), DIR.N.mask() | DIR.W.mask()};

					for (int i = 0; i < 4; i++) {
						int x = X1 + dx[i];
						int y = Y1 + dy[i];
						int x2 = x+IconS.L/2;
						int y2 = y+IconS.L/2;
						int t = mm[i];
						normal.bind();
						stencil.render(r, t, x, x2, y, y2);
						SETT.GROUND().types.NORMAL.miniC.bind();
						edge.render(r, t, x, x2, y, y2);


					}
					COLOR.unbind();


				}
			};
		}

		protected void update(double ds) {
			waterTimer += ds;
			if (waterTimer > 0.2f) {
				waterTimer -= 0.2f;
				shoreOff += shoreDir;
				if (shoreOff >= 7) {
					shoreDir = -1;
				} else if (shoreOff == 0) {
					shoreDir = 1;
				}

			}

			double wx = ds*(1 + SETT.WEATHER().wind.getD()*6)*SETT.WEATHER().wind.dirX();
			double wy = ds*(1 + SETT.WEATHER().wind.getD()*6)*SETT.WEATHER().wind.dirY();
			dis1.update(ds);
			dis2.update(SETT.WEATHER().wind.dirX()*ds*0.8, SETT.WEATHER().wind.dirY()*ds*0.8);
			tex1.update(ds);
			tex2.update(-wx*1.5, -wy*1.2);

			shore.interpolate(SETT.TERRAIN().colors.waternormal, SETT.TERRAIN().colors.waterWinter, 1.0-SETT.WEATHER().growth.getD());
			normal.set(shore).shadeSelf(0.5);
			deep.set(normal).shadeSelf(0.75);

		}

		public void renderIce(int mask, int corner, RenderIterator it) {
			int ran = it.ran();
			int x = it.x();
			int y = it.y();

			if (mask == 0x0F) {
				ice_fulls.render(CORE.renderer(), ran&0x0F, x, y);
			}else {
				ice.render(CORE.renderer(), (ran&0b11)*16 + mask, x, y);
			}
		}

		public void render(int mask, int corner, RenderIterator it, COLOR cForeGround, COLOR bg) {

			int ran = it.ran();
			int x = it.x();
			int y = it.y();

			if (mask == 0) {
				bg.bind();
				int t = ran &0x0F;
				single_stencil.render(CORE.renderer(), t, x, y);
				cForeGround.bind();
				single_stencil.renderTextured(single_animation.getTexture(t), t, x, y);
			}else {
				int stenI = (ran&0b11)*16 + mask;


				ran = ran >> 2;
				int off = (int) ((TIME.currentSecond())*5);
				off += -1 + (ran&3);
				off &= 0x0F;
				off = offs[off];

				int texI = off*16 + mask;

				bg.bind();
				stencil.render(CORE.renderer(), stenI, x, y);

				cForeGround.bind();
				if (mask != 0x0F)
					stencil.renderTextured(animation.getTexture(texI), stenI, x, y);


				if (corner != 0) {
					animation_corner.render(CORE.renderer(), 16*(off/2) + corner, x, y);
				}
			}
		}

		public void above(int mask, int corner, RenderIterator it) {

			int ran = it.ran();
			int x = it.x();
			int y = it.y();

			mask &= 0x0F;

			if (mask == 0x0F)
				return;

			int stenI = (ran&0b11)*16 + mask;



			TextureCoords tex = SETT.GROUND().getTexture(it.tile(), it.ran());
			SETT.GROUND().MAP.get(it.tile()).col(it.tile()).bind();
			edge.renderTextured(tex, stenI, x, y);
			COLOR.unbind();

		}

		void renderTexture(RenderData.RenderIterator i) {

			if (S.get().graphics.get() == 0)
				return;

			normal.bind();
			o2.bind();

			CORE.renderer().renderDisplace(dis1.x1(i.tx()+4), dis1.y1(i.ty()+4), tex1.x1(i.tx()+2), tex1.y1(i.ty()+2),
					C.T_PIXELS, C.T_PIXELS, 16,
					i.x(), i.x()+C.TILE_SIZE, i.y(), i.y()+C.TILE_SIZE);

			CORE.renderer().renderDisplace(dis1.x1(i.tx()+4), dis1.y1(i.ty()+4), tex1.x1(i.tx()+2), tex1.y1(i.ty()+2),
					C.T_PIXELS, C.T_PIXELS, 16,
					i.x(), i.x()+C.TILE_SIZE, i.y(), i.y()+C.TILE_SIZE);
			COLOR.unbind();
			OPACITY.O25.bind();

			CORE.renderer().renderDisplace(dis2.x1(i.tx()), dis2.y1(i.ty()), tex2.x1(i.tx()), tex2.y1(i.ty()),
					C.T_PIXELS, C.T_PIXELS, 8,
					i.x(), i.x()+C.TILE_SIZE, i.y(), i.y()+C.TILE_SIZE);
//			
			OPACITY.unbind();
		}


	}





}
