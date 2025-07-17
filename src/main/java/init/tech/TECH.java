
package init.tech;

import java.io.IOException;

import game.boosting.BoostSpecs;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.values.GVALUES;
import game.values.Lockable;
import game.values.Lockers;
import init.sprite.UI.UI;
import init.tech.TechCurrency.TechCurrencies;
import settlement.stats.Induvidual;
import snake2d.LOG;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O;
import util.dic.Dic;
import util.info.INFO;
import view.ui.tech.Node_Extra;
import world.map.regions.Region;

		public final class TECH implements INDEXED {
			////////////////////////////////////////////////////////////////#!# The only added lines!!!
			//#!# This adds the cost benefit analysis and tech tree display to each tech individually
			public CostBenefit Tech_CostBenefit = new CostBenefit(this);
			public Node_Extra Extra = new Node_Extra();
			////////////////////////////////////////////////////////////////#!#
			public final COLOR color;
			private final int index;
			public final int levelMax;
			public final LIST<TechCost> costs;
			public final int costTotal;
			public final double levelCostInc;
//	public final double levelCostMulInc;


			private LIST<init.tech.TECH.TechRequirement> needs;
			private LIST<init.tech.TECH.TechRequirement> needsPruned;
			private final INFO info;

			public final BoostSpecs boosters;
			public final Lockable<Faction> plockable;
			public final Lockers lockers;

			private SPRITE icon = null;

			public final String key;
			public final TechTree tree;
			public final double AIAmount;
			Json requires;

			TECH(TechCurrency.TechCurrencies cc, String key, LISTE<init.tech.TECH> all, Json data, Json text, TechTree tree, int xx, int yy) throws IOException {
				this.key = key;
				this.tree = tree;
				if (text != null)
					info = new INFO(text);
				else
					info = new INFO(tree.name + " " + (xx+1) + ":" + (yy+1), Dic.empty);
				index = all.add(this);
				levelMax = data.i("LEVEL_MAX", 1, 10000, 1);
				costs = cc.read(data);
				int a = 0;
				for (TechCost c : costs)
					a += c.amount;
				costTotal = a;
				levelCostInc = data.dTry("LEVEL_COST_INC", 0, 100000, 0);
				if (data.has("LEVEL_COST_INC_MUL"))
					LOG.ln(key);
				AIAmount = data.dTry("AI_AMOUNT", 0, 1, 1.0);
				plockable = GVALUES.FACTION.LOCK.push();
				plockable.push(data);
				lockers = new Lockers(Dic.¤¤TechnologyShort + ": " + info.name, UI.icons().s.vial);

				if (data.has("COLOR"))
					color = new ColorImp(data);
				else
					color = tree.color;

				lockers.add(GVALUES.FACTION, data, new DOUBLE_O<Faction>() {

					@Override
					public double getD(Faction t) {
						if (t == FACTIONS.player()) {
							if (FACTIONS.player().tech.isPenaltyLocked(init.tech.TECH.this))
								return 0;
							return FACTIONS.player().tech.level(init.tech.TECH.this) > 0 ? 1 : 0;
						}
						return 1;
					}

				});

				lockers.add(GVALUES.INDU, data, new DOUBLE_O<Induvidual>() {

					@Override
					public double getD(Induvidual t) {
						if (t.faction() == FACTIONS.player()) {
							if (FACTIONS.player().tech.isPenaltyLocked(init.tech.TECH.this))
								return 0;
							return FACTIONS.player().tech.level(init.tech.TECH.this) > 0 ? 1 : 0;
						}
						return 1;
					}

				});

				lockers.add(GVALUES.REGION, data, new DOUBLE_O<Region>() {

					@Override
					public double getD(Region t) {
						if (t.faction() == FACTIONS.player()) {
							if (FACTIONS.player().tech.isPenaltyLocked(init.tech.TECH.this))
								return 0;
							return FACTIONS.player().tech.level(init.tech.TECH.this) > 0 ? 1 : 0;
						}
						return 1;
					}

				});

				boosters = new BoostSpecs(info.name, UI.icons().s.vial, false);
				boosters.read(data, null);

				if (data.has("ICON"))
					icon = UI.icons().get(data).huge;

				requires = data;

				data.has("REQUIRES_TECH_LEVEL");

				data.checkUnused();

			}

			@Override
			public int index() {
				return index;
			}

			public LIST<init.tech.TECH.TechRequirement> requires(){
				return needs;
			}

			public LIST<init.tech.TECH.TechRequirement> requiresNodes(){
				return needsPruned;
			}

			void set(LIST<init.tech.TECH.TechRequirement> needs) {
				this.needs = needs;
			}

			void prune(LIST<init.tech.TECH.TechRequirement> needs) {
				this.needsPruned = needs;
			}

			public boolean requires(init.tech.TECH other, int level) {
				if (other == this)
					return false;
				for (int i = 0; i < needs.size(); i++) {
					init.tech.TECH t = needs.get(i).tech;
					if (t == other || t.requires(other, needs.get(i).level))
						if (needs.get(i).level > level)
							return true;
				}
				return false;
			}

			public SPRITE icon() {
				if (icon == null) {
					icon = TechIcon.icon(this);
				}
				return icon;
			}

			public static final class TechRequirement {

				public final init.tech.TECH tech;
				public final int level;

				TechRequirement(init.tech.TECH t, int l) {
					this.tech = t;
					this.level = l;
				}

				@Override
				public boolean equals(Object obj) {
					if (obj instanceof init.tech.TECH.TechRequirement) {
						init.tech.TECH.TechRequirement q = (init.tech.TECH.TechRequirement) obj;
						return q.level == level && q.tech == tech;
					}
					return false;
				}

			}

			public CharSequence name() {
				return info.name;
			}

			public CharSequence desc() {
				return info.desc;
			}

		}
