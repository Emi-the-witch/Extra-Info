package view.ui.manage;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.player.PTech.TechCurr;
import init.C;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.type.HTYPES;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.dic.Dic;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.ui.UIView;
import view.ui.goods.*;

import static view.ui.goods.UIMaintenance.sum_d;


////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///#!# Updates the front facing UI to add 5 buttons for Expenses, Production, Recipes, Values, and Maintenance
////////////////////////////////////////////////////////////////////////////////////////////////////////////////


public final class IManager {
	// #!# The tabs being added
	private final UIRecipes recipes;
	private final UIValues values;
	private final UIMaintenance all_maintenance;
	private final UIExpenses all_expenses;
	private final UIProduction all_production;
	private final UIModSettings mod_settings;
	private double[] speed = new double[50];
	////////////////////////////

	public static final int TOP_HEIGHT = 48;
	
	private final GuiSection top = new GuiSection();
	private IFullView current;
	private final Inter inter = new Inter();
	
	public IManager(UIView view) {
		ArrayListGrower<IFullView> all = new ArrayListGrower<>();
		all.add(view.goods);
		all.add(view.economy);
		all.add(view.tourists);
		all.add(view.tech);
		all.add(view.raider);
		all.add(view.level);
		all.add(view.profile);

		// #!# The tabs being added
		recipes = new UIRecipes();
		all.add(recipes);

		values = new UIValues();
		all.add(values);

		all_maintenance = new UIMaintenance();
		all.add(all_maintenance);

		all_expenses = new UIExpenses();
		all.add(all_expenses);

		all_production = new UIProduction();
		all.add(all_production);

		mod_settings = new UIModSettings();
		all.add(mod_settings);
		////////////////////////////
		for (IFullView w : all) {
			
			GButt.ButtPanel b = new GButt.ButtPanel(w.icon) {
				@Override
				protected void clickA() {
					show(w);
				};
				@Override
				protected void renAction() {
					selectedSet(w == current);
				}
			};
			b.hoverInfoSet(w.title);
			b.pad(16, 2);
			top.addRightC(0, b);		
		}

		
		top.body().centerX(C.DIM());
		CLICKABLE exit = new GButt.ButtPanel(SPRITES.icons().m.exit) {
			
			@Override
			protected void clickA() {
				inter.hide();
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(Dic.¤¤Close);
			}
			
		};
		exit = KeyButt.wrap(exit, KEYS.MAIN().SWAP);
		exit.body().moveX2(C.WIDTH()-8);
		exit.body().centerY(top);
		top.add(exit);
		
		top.body().centerY(0, TOP_HEIGHT);
	}
	
	public void show(IFullView view) {
		current = view;
		current.section.body().moveY1(IFullView.TOP_HEIGHT);
		current.section.body().moveX1(16);
		current.init();
		inter.activate();
		
	}
	
	public void show() {
		show(current == null ? VIEW.UI().goods : current);
		
	}
	
	public void close() {
		inter.hide();
	}
	
	public boolean open() {
		return inter.isActivated();
	}
	
	private class Inter extends Interrupter {
		

		
		
		
		public Inter(){
			
			
			
		}
		
		
		
		@Override
		protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
			current.section.hover(mCoo); 
			top.hover(mCoo);
			return true;
		}
	 
		@Override
		protected void mouseClick(MButt button) {
			if (button == MButt.RIGHT) {
				if (!current.back())
					hide();
			}else if(button == MButt.LEFT) {
				current.section.click();
				top.click();
			}
		}
		
		
		
		@Override
		protected void hoverTimer(GBox text) {
			current.section.hoverInfoGet(text);
			top.hoverInfoGet(text);
		}

		@Override
		protected boolean update(float ds) {
			GAME.SPEED.tmpPause();
			return false;
		}
		
		@Override
		protected boolean render(Renderer r, float ds) {
			
			GCOLOR.UI().bg().render(r, C.DIM());
			UI.PANEL().butt.render(r, 0, C.WIDTH(), 0, TOP_HEIGHT, 0, DIR.S.mask());
			current.section.render(r, ds);
			top.render(r, ds);
			return false;
		}
		
		@Override
		public void hide() {
			// TODO Auto-generated method stub
			super.hide();
		}
		
		public void activate() {
			super.show(VIEW.inters().manager);
		}
		
	}
	
	public CLICKABLE butt() {
		
		GuiSection s = new GuiSection();
		int i = 0;
		
		bAdd(s, i++, VIEW.UI().goods, UI.icons().s.storage, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, (SETT.ROOMS().STOCKPILE.tally().amountTotal(null)+1.0)/ (SETT.ROOMS().STOCKPILE.tally().space.total(null)+1.0), 0);
			}
		});
		
		bAdd(s, i++, VIEW.UI().economy, UI.icons().s.money, new GStat() {
			
			int ri = 0;
			int prob = 0;
			int probL = 0;
			
			@Override
			public void update(GText text) {
				if ((GAME.updateI() & 0x11) == 0) {
					if (ri >= RESOURCES.ALL().size()) {
						probL = prob;
						prob = 0;
						ri = 0;
					}else {
						RESOURCE res = RESOURCES.ALL().get(ri);
						
						if (prob < 2) {
							if (SETT.ROOMS().IMPORT.tally.capacity.get(res) > 0 && SETT.ROOMS().IMPORT.tally.problem(res, false) != null)
								prob = 2;
							if (SETT.ROOMS().EXPORT.tally.capacity.get(res) > 0 && SETT.ROOMS().EXPORT.tally.problem(res, false) != null)
								prob = 2;
						}
						
						if (prob < 1 ) {
							if (SETT.ROOMS().IMPORT.tally.capacity.get(res) > 0 && SETT.ROOMS().IMPORT.tally.warning(res, false) != null)
								prob = 1;
							if (SETT.ROOMS().EXPORT.tally.capacity.get(res) > 0 && SETT.ROOMS().EXPORT.tally.warning(res) != null)
								prob = 1;
						}
						ri++;
					}
					
				}
				
				
				GFORMAT.i(text, (int)FACTIONS.player().credits().credits());
				
				if (probL == 0)
					text.normalify();
				else if (probL == 1)
					text.warnify();
				else
					text.errorify();
			}
		});
		
		
		bAdd(s, i++, VIEW.UI().tourists, UI.icons().s.camera, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, STATS.POP().pop(HTYPES.TOURIST()));
			}
		});

		bAdd(s, i++, VIEW.UI().tech, UI.icons().s.vial, new GStat() {

			@Override
			public void update(GText text) {
				int am;
				int index=0;
				for (TechCurr c : GAME.player().tech.currs()) {
					index+=1;
					am = c.available();
					if (index ==1){GFORMAT.i(text, am);}
				}
			}
		});
		bAdd(s, i++, VIEW.UI().tech, UI.icons().s.book, new GStat() {

			@Override
			public void update(GText text) {
				int am;
				int index=0;
				for (TechCurr c : GAME.player().tech.currs()) {
					index+=1;
					am = c.available();
					if (index ==2){GFORMAT.i(text, am);}
				}
			}
		});
		bAdd(s, i++, VIEW.UI().tech, UI.icons().s.clock, new GStat() {

			private int i =0;

			@Override
			public void update(GText text) {

//                int am;
//                int index=0;
//                for (TechCurr c : GAME.player().tech.currs()) {
//                    index+=1;
//                    am = c.available();
//                    if (index ==3){GFORMAT.i(text, am);}
//                }

				i = (i+1) % speed.length;
				speed[i]= game.GameSpeed.actualSpeed;
				GFORMAT.i(text,(long) sum_d(speed) / speed.length  );
			}
		});

		bAdd(s, i++, VIEW.UI().raider, UI.icons().s.death, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, GAME.raiders().active().size());
				text.errorify();
			}
		});

		///////////////////////////////////////////////////
		// #!# The icons being added to the main UI screen:
		bAdd(s, i++, all_maintenance, UI.icons().s.degrade, new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, (long) maint_value());
				text.errorify();
			}
		});
		{
			int k = 0;
			GuiSection ss = new GuiSection();
			bAdd2(ss, k++, recipes, UI.icons().s.money, null);
			bAdd2(ss, k++, values, UI.icons().s.money, null);

			bAdd2(ss, k++, all_expenses, UI.c_icons().s.c_minus, null);
			bAdd2(ss, k++, all_production, UI.c_icons().s.c_plus, null);

			bAdd2(ss, k++, VIEW.UI().level, UI.icons().s.arrowUp, null);
			bAdd2(ss, k++, VIEW.UI().profile, UI.icons().s.menu, null);

			bAdd(s, i++, ss);
		}
		return s;
		

	}
	///////////////////////////////////////////////////////////////////////////////////////////////
	/// #!# Add UI elements and change the side for rendering iconsfrom 74 to 24 for 16x16 pixel icons
	public double maint_value() {
		//double import_costs = 0;
		double value_costs = 0;
		// Sum up the total resource use and update building_totals values
		for (RESOURCE res : RESOURCES.ALL()) {
			if (SETT.MAINTENANCE().estimateGlobal(res) != 0) {
				//      import_costs += SETT.MAINTENANCE().estimateGlobal(res) * FACTIONS.player().trade.pricesBuy.get(res);
				value_costs += SETT.MAINTENANCE().estimateGlobal(res) * FACTIONS.PRICE().get(res);
			}
			continue;
		}
		return value_costs;
	}
	private void bAdd2(GuiSection s, int i, IFullView v, SPRITE icon, SPRITE vv) {
		CLICKABLE p = bb(v, icon, vv);
		bAdd2(s, i, p);

	}
	private void bAdd2(GuiSection s, int i, RENDEROBJ ren) {

		s.add(ren, (i/2)*37, 24*((i%2)));

	}
	////////////////////////////////////////////////////////////////////////////////////
	private void bAdd(GuiSection s, int i, IFullView v, SPRITE icon, SPRITE vv) {
		CLICKABLE p = bb(v, icon, vv);
		bAdd(s, i, p);
		
	}
	
	private CLICKABLE bb(IFullView v, SPRITE icon, SPRITE vv) {
		CLICKABLE p = new CLICKABLE.ClickableAbs(74/(vv == null ? 2 : 1), 24) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
				GButt.ButtPanel.renderFrame(r, isActive, isSelected, isHovered, body);
				
				if (vv == null) {
					icon.renderC(r, body.cX(), body.cY());
				}else {
					icon.renderCY(r, body.x1()+4, body.cY());
					
					vv.renderCY(r, body.x1()+20, body.cY());
				}
				
				
				
			}
			
			@Override
			protected void clickA() {
				show(v);
			};
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				v.hoverInfoGet(text);
			}
			
		};
		p.hoverInfoSet(v.title);
		return p;
		
	}
	
	private void bAdd(GuiSection s, int i, RENDEROBJ ren) {

		s.add(ren, (i/2)*74, 24*((i%2)));
		
	}
	
}