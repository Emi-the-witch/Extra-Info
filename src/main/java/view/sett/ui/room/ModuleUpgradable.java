package view.sett.ui.room;

import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.time.TIME;
import init.resources.RESOURCE;
import init.settings.S;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import util.text.D;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.service.module.ROOM_SERVICER;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.text.Dic;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GGrid;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModuleUpgradable implements ModuleMaker {

	private final CharSequence ¤¤UPGRADE_ALL = "¤Upgrade all";
	private final CharSequence ¤¤UPGRADE_ALL_DESC = "¤Upgrade all rooms once. Upgrading a room significantly increases performance. It costs a lot of resources initially that will be deducted from warehouses, and also in maintenance over time.";
	private final CharSequence ¤¤UPGRADABLE = "¤Upgradable:";
	private final CharSequence ¤¤UPGRADE = "¤Upgrade";
	private final CharSequence ¤¤UPGRADE_MAX_REACHED = "¤Maximally Upgraded.";

	private final CharSequence ¤¤DOWNGRADED = "¤Downgrade this room. All resources will be lost.";

	ModuleUpgradable(Init init){
		D.t(this);
	}


	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		if (p instanceof RoomBlueprintIns<?>) {
			RoomBlueprintIns<?> pi = (RoomBlueprintIns<?>) p;
			if (pi.upgrades().max() > 0) {
				l.add(new I(pi));
			}
		}

	}

	private class I extends UIRoomModule {

		private final RoomBlueprintIns<?> blueprint;

		I(RoomBlueprintIns<?> blue){
			this.blueprint = blue;
		}

		@Override
		public void appendManageScr(GGrid grid, GGrid text, GuiSection sExta) {



		}

		@Override
		public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
					       LISTE<UIRoomBulkApplier> appliers) {

			appliers.add(new UIRoomBulkApplier(new Str(¤¤UPGRADE_ALL)) {

				private final int[] res = new int[16];

				@Override
				protected void apply(RoomInstance ii) {

					if (ii.upgrade() >= blueprint.upgrades().max() || !ii.blueprintI().upgrades().requires(ii.upgrade()+1).passes(FACTIONS.player()))
						return;
					boolean okk = true;
					for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
						int am = ii.resAmount(ri, ii.upgrade()+1) - ii.resAmount(ri, ii.upgrade());
						okk &= SETT.ROOMS().STOCKPILE.tally().amountReservable.get(blueprint.constructor().resource(ri)) >= am;

					}
					if (okk) {
						for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
							int am = ii.resAmount(ri, ii.upgrade()+1) - ii.resAmount(ri, ii.upgrade());
							blueprint.constructor().resource(ri).remove(am, RTYPE.CONSTRUCTION);
						}
						ii.upgradeSet(ii.upgrade()+1);
					}
				}

				@Override
				protected void hover(GBox b) {
					b.title(¤¤UPGRADE_ALL);
					b.text(¤¤UPGRADE_ALL_DESC);
					b.NL(8);

					Arrays.fill(res, 0);

					int ok = 0;
					for (int i = 0; i < blueprint.instancesSize(); i++) {
						RoomInstance ii = blueprint.getInstance(i);

						if (ii.upgrade() < blueprint.upgrades().max() && ii.blueprintI().upgrades().requires(ii.upgrade()+1).passes(FACTIONS.player())) {
							boolean okk = true;
							for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
								int am = ii.resAmount(ri, ii.upgrade()+1) - ii.resAmount(ri, ii.upgrade());
								if (SETT.ROOMS().STOCKPILE.tally().amountReservable.get(blueprint.constructor().resource(ri)) >= res[ri] + am) {

								}else {
									okk = false;
								}

							}
							if (okk) {
								ok++;
								for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
									int am = ii.resAmount(ri, ii.upgrade()+1) - ii.resAmount(ri, ii.upgrade());
									res[ri] += am;
								}
							}
						}

					}

					for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
						if (res[ri] > 0) {
							b.add(blueprint.constructor().resource(ri).icon());
							b.add(GFORMAT.iIncr(b.text(), -res[ri]));
							b.NL();
						}
					}

					b.NL(8);

					b.textL(¤¤UPGRADABLE);
					b.add(GFORMAT.iofkInv(b.text(), ok, blueprint.instancesSize()));


				}

			});

		}

		@Override
		public void appendButt(GuiSection s, GETTER<RoomInstance> get) {

			if (blueprint.upgrades().max() > 0) {
				SPRITE sp = new SPRITE.Imp(Icon.S+Icon.S, Icon.S) {

					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						RoomInstance ins = get.get();
						int up = ins.upgrade();
						ColorImp.TMP.interpolate(GCOLOR.T().H2, GCOLOR.T().H1, (double)up/blueprint.upgrades().max());
						ColorImp.TMP.bind();
						for (int i = 0; i <= up; i++) {
							int x = i / 2;
							int y = i % 2;
							SPRITES.icons().s.plus.render(r, X1+x*Icon.S/2, Y1+y*Icon.S/2);
						}
						COLOR.unbind();
					}
				};
				s.addRightC(16, sp);

			}


		}

		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			box.NL(4);
			box.add(UI.icons().s.arrowUp);
			box.textL(Dic.¤¤Upgrade);
			box.tab(6);
			RoomBlueprintImp b = (RoomBlueprintImp) room.blueprint();
			box.add(GFORMAT.iofk(box.text(), room.upgrade(rx, ry), b.upgrades().max()));
			box.NL();
		}

		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {

			GButt bb = new GButt.ButtPanel(UI.icons().m.arrow_down) {

				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.text(¤¤DOWNGRADED);
				}

				@Override
				protected void clickA() {
					downGrade(g(get));
				}

				@Override
				protected void renAction() {
					activeSet(g(get).upgrade() > 0);
				}

			};

			GButt.BSection b = new GButt.BSection() {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					text.title(¤¤UPGRADE);

					RoomInstance i = get.get();
					if (i.upgrade() == blueprint.upgrades().max()) {
						text.text(¤¤UPGRADE_MAX_REACHED);
						return;
					}

					if (blueprint.upgrades().desc(g(get).upgrade()+1) != null) {
						b.text(blueprint.upgrades().desc(g(get).upgrade()+1));
					}else if (i instanceof ROOM_SERVICER) {
						b.textLL(Dic.¤¤Quality);
						double d = 1.0/(i.blueprintI().upgrades().max()+1.0);
						b.add(GFORMAT.percInc(b.text(), d));
					}else {
						b.textLL(Dic.¤¤Boosts);
						b.add(GFORMAT.f0(b.text(), blueprint.upgrades().boost(g(get).upgrade()+1)-blueprint.upgrades().boost(g(get).upgrade())));
					}

					b.NL(8);

					g(get).blueprintI().upgrades().requires(g(get).upgrade()+1).hover(text, FACTIONS.player());

					b.NL(8);

					for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
						int am =  g(get).resAmount(ri, g(get).upgrade()+1)-g(get).resAmount(ri, g(get).upgrade());
						if (am > 0) {
							b.add(blueprint.constructor().resource(ri).icon());
							GText t = b.text();
							GFORMAT.iIncr(t, -am);
							if (am > SETT.ROOMS().STOCKPILE.tally().amountReservable.get(blueprint.constructor().resource(ri)))
								t.errorify();
							else
								t.normalify();
							b.add(t);
							b.NL();
						}
					}

					b.NL();
					b.textLL("Daily maintenance cost estimate:");
					b.NL(8);

					RoomInstance ins = get.get();
					ROOM_DEGRADER deg = get.get().degrader(get.get().mX(), get.get().mY());
					double iso = ins.isolation(get.get().mX(), get.get().mY());
					double boost = SETT.MAINTENANCE().speed();

					double maint_cost = 0;
					for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
						int am =  g(get).resAmount(ri, g(get).upgrade()+1)-g(get).resAmount(ri, g(get).upgrade());
						if (am <= 0) {continue;}
						RESOURCE res = deg.res(ri);
						b.add(res.icon());
						b.add(GFORMAT.f0(b.text(), -ROOM_DEGRADER.rateResource(boost, deg.base(), iso, am)* TIME.years().bitConversion(TIME.days())/16));
						b.textL(res.name);
						b.tab(6);
						b.add(GFORMAT.f0(b.text(), -ROOM_DEGRADER.rateResource(boost, deg.base(), iso, am)* TIME.years().bitConversion(TIME.days())*FACTIONS.player().trade.pricesBuy.get(res.tr())/16));
						maint_cost += (-ROOM_DEGRADER.rateResource(boost, deg.base(), iso, am)*TIME.years().bitConversion(TIME.days())*FACTIONS.player().trade.pricesBuy.get(res.tr())/16);
						b.textLL(" $");
						b.NL();
					}

					b.sep();
					b.textLL("Total Denari Cost");
					b.tab(6);
					b.add(GFORMAT.f0(b.text(), maint_cost));
					b.textLL(" $");

				}


				@Override
				protected void clickA() {
					if (g(get).upgrade() >= blueprint.upgrades().max()) {
						return;
					}
					for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
						int am =  g(get).resAmount(ri, g(get).upgrade()+1)-g(get).resAmount(ri, g(get).upgrade());
						if (am > 0) {
							blueprint.constructor().resource(ri).remove(am, RTYPE.CONSTRUCTION);
						}

					}
					g(get).upgradeSet(g(get).upgrade()+1);
					super.clickA();
				}

				@Override
				protected void renAction() {
					activeSet(true);

					if (g(get).upgrade() >= blueprint.upgrades().max()) {
						activeSet(false);
						return;
					}
					if (S.get().developer)
						return;
					else if (!g(get).blueprintI().upgrades().requires(g(get).upgrade()+1).passes(FACTIONS.player()))
						activeSet(false);


					for (int ri = 0; ri < blueprint.constructor().resources(); ri++) {
						int am =  g(get).resAmount(ri, g(get).upgrade()+1)-g(get).resAmount(ri, g(get).upgrade());
						if (am > SETT.ROOMS().STOCKPILE.tally().amountReservable.get(blueprint.constructor().resource(ri))) {
							activeSet(false);
							break;
						}
					}
				}
			};
			b.body().setHeight(bb.body.height());
			b.body().incrW(8);
			b.addCentredY(new GText(UI.FONT().S, ¤¤UPGRADE).lablifySub(), 8);
			b.addRightC(10, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.iofk(text, g(get).upgrade(), blueprint.upgrades().max());
					text.lablify();
				}
			});

			b.body().incrW(64);

			GuiSection s = new GuiSection();
			s.add(b);


			s.addRight(0, bb);

			section.addRelBody(8, DIR.S, s);



		}

		private RoomInstance g(GETTER<RoomInstance> g) {
			return (RoomInstance) g.get();
		}



	}

	private void downGrade(RoomInstance ins) {
		if (ins.upgrade() <= 0)
			return;
		ins.upgradeSet(ins.upgrade()-1);

		for (COORDINATE c : ins.body()) {
			if (ins.is(c) && SETT.MAINTENANCE().isser.is(c)) {
				RESOURCE res = SETT.MAINTENANCE().resource.get(c);
				if (res != null && !hasRes(ins, res)) {
					SETT.MAINTENANCE().maintain(c.x(), c.y());
				}
			}
		}
	}


	private boolean hasRes(RoomInstance ins, RESOURCE res) {
		for (int ri = 0; ri < ins.blueprintI().constructor().resources(); ri++) {
			if (ins.blueprintI().constructor().resource(ri) == res) {
				return ins.blueprintI().constructor().resourceHas(ri, ins.upgrade());
			}
		}
		return false;
	}


}
