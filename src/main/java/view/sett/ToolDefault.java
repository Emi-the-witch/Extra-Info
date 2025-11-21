package view.sett;

import game.faction.FACTIONS;
import init.constant.C;
import init.settings.S;
import init.sprite.SPRITES;
import util.text.D;
import settlement.entity.ENTITY;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.misc.util.RESOURCE_TILE;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import settlement.thing.THINGS.Thing;
import settlement.tilemap.GuiTerrainHoverInfo;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.*;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.Tool;
import view.tool.ToolManager;

import static settlement.main.SETT.*;

final class ToolDefault extends Tool{

	private boolean dragging = false;
	private final Coo dragCoo = new Coo();
	private static CharSequence ¤¤clickJob = "¤Hold '{0}' and click to place more of job: ";
	private static CharSequence ¤¤clickRoom = "¤Hold '{0}' and click to build another: ";
	private static CharSequence ¤¤clickRoom2 = "¤Hold '{0}' and click to copy this: ";
	private static CharSequence ¤¤reconstruct = "¤click to reconstruct rooms";
	
	static {
		D.ts(ToolDefault.class);
	}
	
	private final LIST<SettDebugClick> debugs;
	
	ToolDefault(ToolManager m) {
		super(m);
		debugs = new ArrayList<>(SettDebugClick.all);
	}

	@Override
	protected void updateHovered(float ds, GameWindow window) {
		update(ds, window);
	}
	
	@Override
	protected void update(float ds, GameWindow window) {
		dragging &= MButt.RIGHT.isDown();
	}
	
	private final BODY_HOLDERE body = new BODY_HOLDERE() {
		
		private final Rec body = new Rec(C.TILE_SIZE);
		
		@Override
		public RECTANGLEE body() {
			return body;
		}
	};

	@Override
	protected void renderHovered(SPRITE_RENDERER r, float ds, GameWindow window, GBox box) {
		if (!SETT.IN_BOUNDS(window.tile()))
			return;
		
		if (MButt.RIGHT.isDown()) {
			if (!dragging) {
				dragging = true;
				dragCoo.set(window.tile());
			}
			
			box.add(SPRITES.icons().s.crossheir);
			box.add(box.text().add(VIEW.s().getWindow().tile().x()).add(',').add(VIEW.s().getWindow().tile().y()));
			box.tab(4);
			
			if (dragging) {
				
				int dx = dragCoo.x()-window.tile().x();
				int dy = dragCoo.y()-window.tile().y();
				int w = Math.abs(dx);
				int h = Math.abs(dy);
				if (w + h > 0) {
					dx = CLAMP.i(dx, -1, 1); 
					dy = CLAMP.i(dy, -1, 1); 
					box.add(box.text().add(w+1).add('x').add(h+1));
					
					for (int d = 1; d <= w; d++) {
						int x = window.tile().rel().x() + (d*dx)*C.TILE_SIZE;
						int y = window.tile().rel().y();
						SPRITES.cons().BIG.dashed_hollow.render(r, 0, x, y);
					}
					
					for (int d = 1; d <= h; d++) {
						int x = window.tile().rel().x() + (dx*w)*C.TILE_SIZE;
						int y = window.tile().rel().y() + (d*dy)*C.TILE_SIZE;
						SPRITES.cons().BIG.dashed_hollow.render(r, 0, x, y);
					}
				}
				// #!# If dragging, run check
				String output = MaintHover.drag(dragCoo.x(), dragCoo.y(), window.tile().x(), window.tile().y());
				box.add(box.text().add(output));

			}else{
				// #!# If not dragging maint calc reset
				MaintHover.not_drag();
			}


			box.NL(4);
			
			SPRITES.cons().BIG.dashed.render(r, 0, window.tile().rel().x(), window.tile().rel().y());
			VIEW.mouse().setReplacement(SPRITES.icons().m.questionmark);


			int tx = VIEW.s().getWindow().tile().x();
			int ty = VIEW.s().getWindow().tile().y();
			GuiTerrainHoverInfo.add(box, tx, ty);
			if (S.get().developer) {
				box.text(PATH().availability.get(VIEW.s().getWindow().tile()).name());
			}
			
			for (SettEnv t : SETT.ENV().map.all()){
				box.NL();
				box.textL(t.info.name);
				box.tab(5);
				box.add(GFORMAT.perc(box.text(), t.get(window.tile())));
			}
			
			SETT.OVERLAY().RESOURCES.add();
		}else {
			
			COORDINATE coo = window.pixel();
			if (!PIXEL_BOUNDS.holdsPoint(coo))
				return;
			ENTITY e = ENTITIES().getArroundPoint(coo.x(), coo.y());
			SETT_HOVERABLE t = THINGS().getArroundCoo(coo.x(), coo.y());
			
			if (isEntity(coo, e, t)) {
				if (e.canBeClicked())
					VIEW.mouse().setReplacement(SPRITES.icons().m.questionmark);
				SETT.OVERLAY().add(e);
				e.hover(box);
			}else if(isThing(coo, e, t)) {
				if (t.canBeClicked())
					VIEW.mouse().setReplacement(SPRITES.icons().m.questionmark);
				SETT.OVERLAY().add((Thing)t);
				t.hover(box);
			}
			else if(ROOMS().map.is(window.tile())) {
				Room room = ROOMS().map.get(window.tile());
				RESOURCE_TILE res = room.resourceTile(window.tile().x(), window.tile().y());
				if (res != null && res.resource() != null) {
					body.body().moveX1Y1(window.tile().x()*C.TILE_SIZE, window.tile().y()*C.TILE_SIZE);
					SETT.OVERLAY().add(body, COLOR.WHITE65);
					box.title(res.resource().name);
					box.add(GFORMAT.i(box.text(), res.amount()));
					if (S.get().developer) {
						box.NL();
						box.add(box.text().add('a').add(res.reservable()));
					}
				}else {

					VIEW.s().ui.rooms.hover(box, room, window.tile().x(), window.tile().y());
					if (room.blueprint() == SETT.ROOMS().THRONE) {
						box.title(ROOMS().THRONE.info.name);
						box.text(ROOMS().THRONE.info.desc);
					}
					if (room instanceof RoomInstance)
						VIEW.mouse().setReplacement(SPRITES.icons().m.questionmark);
					SETT.OVERLAY().add(window.tile().x(), window.tile().y());
				}
				
				if (room.constructor() != null && room.blueprint() != SETT.ROOMS().THRONE) {
					box.NL(8);
					GText te = box.text();
					te.lablifySub();
					te.add(¤¤clickRoom).insert(0, KEYS.MAIN().MOD.repr());
					box.add(te);
					box.NL().text(room.constructor().blue().info.name);
					box.NL();
					
					if (room.constructor().usesArea() && room.constructor().canBeCopied()) {
						te = box.text();
						te.lablifySub();
						te.add(¤¤clickRoom2).insert(0, KEYS.MAIN().UNDO.repr());
						box.add(te);
						box.NL().text(room.constructor().blue().info.name);
						if (SETT.ROOMS().construction.isser.is(window.tile().x(), window.tile().y())) {
							box.NL(8);
							box.textL(¤¤reconstruct);
						}
					}
					
					
				}
				
			}else {
				JOBS().hover(window.tile().x(), window.tile().y(), box);
				
				Job j = SETT.JOBS().jobGetter.get(window.tile());
				
				if (j != null && j.placer() != null) {
					box.NL(8);
					GText te = box.text();
					te.lablifySub();
					te.add(¤¤clickJob).insert(0, KEYS.MAIN().MOD.repr());
					box.add(te);
					box.NL().text(j.placer().name());
				}
			}	
			
			
			
			
			
			
		}
	}
	
	private boolean isEntity(COORDINATE coo, ENTITY e, SETT_HOVERABLE t) {
		if (e == null)
			return false;
		if (t == null)
			return true;
		double edist = COORDINATE.properDistance(coo.x(), coo.y(), e.body().cX(), e.body().cY());
		double tdist = COORDINATE.properDistance(coo.x(), coo.y(), ((Thing) t).body().cX(), ((Thing) t).body().cY());
		return edist <= tdist;
	}
	
	private boolean isThing(COORDINATE coo, ENTITY e, SETT_HOVERABLE t) {
		if (t == null)
			return false;
		if (e == null)
			return true;
		double edist = COORDINATE.properDistance(coo.x(), coo.y(), e.body().cX(), e.body().cY());
		double tdist = COORDINATE.properDistance(coo.x(), coo.y(), ((Thing) t).body().cX(), ((Thing) t).body().cY());
		return edist > tdist;
	}

	
	@Override
	protected boolean rightClick() {
		return false;
	}

	@Override
	protected void click(GameWindow window) {
		if (MButt.RIGHT.isDown()) {
			
			return;
		}else {
			{
				
				int px = VIEW.s().getWindow().pixel().x();
				int py = VIEW.s().getWindow().pixel().y();
				int tx = VIEW.s().getWindow().tile().x();
				int ty = VIEW.s().getWindow().tile().y();
				for (SettDebugClick c : debugs)
					if (c.debug(px, py, tx, ty))
						return;
			}
			
			if (KEYS.MAIN().MOD.isPressed()) {
				Room room = ROOMS().map.get(window.tile());
				if (room != null && room.constructor() != null && room.blueprint() != SETT.ROOMS().THRONE && room.constructor().blue().reqs.passes(FACTIONS.player())) {
					
					if (room.constructor().blue().cat == SETT.ROOMS().CATS.DECOR) {
						VIEW.s().misc.placer.init(room.constructor().blue(), room.constructor().blue().cat);
					}else {
						SETT.ROOMS().placement.placer.structure.set(window.tile().x(), window.tile().y());
						VIEW.s().misc.placer.init(room.constructor().blue(), VIEW.s().getWindow().tile().x(), VIEW.s().getWindow().tile().y());
					}
					
					return;
				}
				
				Job j = SETT.JOBS().jobGetter.get(window.tile());
				if (j != null && j.placer() != null) {
					VIEW.s().tools.place(j.placer(), j.config());
					return;
				}
			}
			if (KEYS.MAIN().UNDO.isPressed()) {
				Room room = ROOMS().map.get(VIEW.s().getWindow().tile());
				if (room != null && room.constructor() != null && room.blueprint() != SETT.ROOMS().THRONE) {
					SETT.ROOMS().copy.copy(VIEW.s().getWindow().tile().x(), VIEW.s().getWindow().tile().y());
					return;
				}
			}
			
			COORDINATE coo = window.pixel();
			if (!PIXEL_BOUNDS.holdsPoint(coo))
				return;
			ENTITY e = ENTITIES().getArroundPoint(coo.x(), coo.y());
			SETT_HOVERABLE t = THINGS().getArroundCoo(coo.x(), coo.y());
			
			if (isEntity(coo, e, t) && e.canBeClicked()) {
				e.click();
			}else if(isThing(coo, e, t) && t.canBeClicked()) {
					t.click();
			}else if(ROOMS().map.is(window.tile())) {
				Room room = ROOMS().map.get(window.tile());
				VIEW.s().ui.rooms.click(room, window.tile().x(), window.tile().y());
			}			
		}
		
	}


}
