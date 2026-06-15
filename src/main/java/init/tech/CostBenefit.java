package init.tech;

import game.boosting.*;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.type.HCLASS_RACE;
import init.value.Lock;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.ROOM_PRODUCER_INSTANCE;
import settlement.room.industry.module.RoomProduction;
import settlement.room.main.*;
import settlement.room.main.employment.RoomEmploymentIns;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.room.main.employment.RoomEquip;
import settlement.stats.STATS;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import view.keyboard.KEYS;

import java.util.Objects;
import static game.time.TIME.playedGame;
import static init.tech.Knowledge_Costs.cost_inputs;
import static init.tech.Knowledge_Costs.know_worker;
import static settlement.main.SETT.*;


/////////////////////////////////////////////#!# This is a unique file that doesn't overwrite any of Jake's files.
///#!#! This calculates the benefits of each tech's bonuses
public class CostBenefit {

        public double CUR_TIME = -1;
        // Old Variables for reference
        public static double cost_tot = 0; 	// total costs (tools + maint atm) per person


        public boolean contains_upgrade = false;
        public double benefit_maint_upgrade = 0;
        public double benefit_maint_before = 0 ;
        public static double benefit_maint = 0;	// Maintenance per worker for the benefitting industries
        public static double benefit_tools = 0; // tool cost per person for benefited industry buildings
        public double benefit_tot = 0; 	// total benefits cost (tools + maint atm) per person

        public double costs;      // Overall cost in workers
        public double benefits;   // Overall benefit in workers


        // Constructor
        public TECH tech;
        public CostBenefit(TECH t) {
                tech = t;
        }

        // Create the Green-Yellow-Red color for nodes based on the cost/benefit
        public COLOR col (boolean hovered, TECH tech){
                boolean maxed = FACTIONS.player().tech.level(tech) == tech.levelMax;
                if (maxed){ return new ColorImp(10, 120, 120); }
                benefits = tech.Tech_CostBenefit.benefits;
                costs = tech.Extra.worker_cost;
                double shade_val = hovered ? 1 : .6;
                // If no knowledge workers or no calculated benefits, default to white
                if (costs <= 0 || benefits <= 0) {
                        return new ColorImp(127, 127, 127).shade(shade_val);
                }
                // High relative costs are red, high relative benefits are green
                // benefit 200 cost 100 ->   0 127   0
                // benefit 100 cost 100 -> 127 127   0
                // benefit  50 cost 100 -> 127   0   0
                if (benefits > costs) {
                        // benefit = cost => 127 red. benefit >= 2 * cost
                        double red = 255 - (benefits / costs * 127);
                        // Clamp red from 0 to 127
                        red = Math.min(Math.max(red, 0), 127);
                        return new ColorImp((int) red, 127, 0).shade(shade_val);
                }
                if (costs > benefits) {
                        // cost = benefit => 127 green. cost <= 2 * benefit => 0 green
                        double green = 255 - (costs / benefits * 127);
                        // Clamp green from 0 to 127
                        green = Math.min(Math.max(green, 0), 127);
                        return new ColorImp(127, (int) green, 0).shade(shade_val);
                }
                return new ColorImp(127, 127, 127).shade(shade_val);
        }

        public void update(TECH tech){
                // Only run this if you haven't lately.
                if (CUR_TIME == playedGame() && !( KEYS.MAIN().MOD.isPressed() )){return;}
                CUR_TIME = playedGame();

                // Reset variables before rerunning the analysis:
                benefits = 0;
                benefit_maint = 0;
                benefit_tools = 0;
                costs = 0;
                cost_tot = 0;

                // Run the various functions for cost benefit analysis
                Knowledge_Costs.costs();  // Determining cost of tech
                booster_benefits(tech);   // Production bonus tech
                unlock_benefits(tech);    // Upgrade unlock tech
                booster_tools(tech);      // Tool unlock tech

                // Calculate the total costs
                for (TechCost c : tech.costs) {
                        costs   += c.amount / know_worker[c.cu.index];
                        cost_tot += cost_inputs[c.cu.index];
                }

        }


        void booster_benefits(TECH tech)
        {
                // Adds up the boost benefits for every boost in the tech, by industry, by room, per person.
                benefits = 0; // reset benefit upon new tech
                benefit_maint = 0;
                benefit_tools = 0;

                double benefit_maint_total = 0;
                double benefit_emp_total = 0;

                for (BoostSpec b : tech.boosters.all()) { // For all boosts of this tech,

                        for (RoomBlueprint h : ROOMS().all()) { // For each type of room blueprint    Note: We need SETT.ROOMS() to find the RoomInstance bonuses
                                if (  !(h instanceof RoomBlueprintIns)  ) { continue; } // Industries only

                                for (RoomInstance r : ((RoomBlueprintIns<?>) h).all()) { // For each workshop in the industry

                                        if (r.employees() == null || !(r instanceof ROOM_PRODUCER_INSTANCE)) { continue; } // That has employees and produces goods

                                        Industry ind = ((ROOM_PRODUCER_INSTANCE) r).industry();  // Industry of the workshop
                                        Boostable bonus = ind.bonus(); // The boosts of the industry

                                        if (bonus == null || b.boostable.key() != bonus.key()) { continue; } // Boost exists in industry and matches the tech

                                        // BOOSTS BENEFITS
                                        double add = 0;
                                        int tot = 0;

                                        // Testing Jake's method instead (boostValue function added)
//                                        for (Lock<?> l : tech.lockers.all()) {
//                                                if (upgradeBoost.upgradeBoost.containsKey(l.lockable.key)) {
//                                                        NodeBoosts.upEntry am = upgradeBoost.upgradeBoost.get(l.lockable.key);
//                                                        tot += boostValue(am.blue, am.bo, am.value, false);
//                                                }
//                                        }
                                        //


                                        for (Humanoid person : r.employees().employees()) { // for each person working
                                                tot++; //adding up the number of employees
                                                if (STATS.WORK().EMPLOYED.get(person) == r) { //IDK if this is needed, copied from hoverBoosts function
                                                        for (Booster s : bonus.all()) { // look at all boosts an industry has
                                                                if (!s.isMul) { // add up the non-multiplier bonuses
                                                                        add += s.get(person.indu());
                                                                }
                                                        }
                                                }
                                        }
                                        add /= tot; // add is the total additive bonuses.
                                        if(  tot * b.booster.to() / (1+add) > 0  ){
                                                benefits += tot * b.booster.to() / (1+add); //Add the technology's benefit of each workshop
                                        }

                                        // TOOLS COSTS
                                        RoomEmploymentSimple ee = r.blueprint().employment();
                                        RoomEmploymentIns e = r.employees();

                                        for (RoomEquip w : ee.tools()) {
                                                double n = w.degradePerDay * e.tools(w);
                                                double sellFor = FACTIONS.player().trade.pricesBuy.get(w.resource.tr());
                                                benefit_tools -= n * sellFor;
                                        }
                                        benefit_emp_total += r.employees().employed(); // Employee count used for tools and maintenance


                                        // MAINTENANCE COSTS
                                        ROOM_DEGRADER deg = r.degrader(r.mX(), r.mY());
                                        double iso = r.isolation(r.mX(), r.mY());
                                        double boost = MAINTENANCE().speed();

                                        double total_room_maintenance_import = 0;
                                        if (deg == null){ continue; }
                                        for (int i = 0; i < deg.resSize(); i++) {
                                                if (r.resAmount(i,1+r.upgrade()) <= 0)
                                                        continue;
                                                RESOURCE res = deg.res(i);

                                                double n = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, r.resAmount(i,r.upgrade()))* TIME.years().bitConversion(TIME.days()) / 16.0;
                                                double sellFor = FACTIONS.player().trade.pricesBuy.get(res.tr());
                                                total_room_maintenance_import -= n * sellFor;


                                        }
                                        benefit_maint_total += total_room_maintenance_import; // Add for each room


                                }
                        }
                }
                if (benefit_emp_total >0) { benefit_tools = benefit_tools / benefit_emp_total; }
                if (benefit_emp_total >0) { benefit_maint = benefit_maint_total / benefit_emp_total; }
                benefit_tot = benefit_tools + benefit_maint;

        }
        void unlock_benefits(TECH tech)
        { // UNLOCKING UPGRADES

                // Adds up the boost benefits for every boost in the tech, by industry, by room, per person.
                double benefit_maint_total = 0;
                double benefit_maint_upgrade_total = 0;
                double benefit_emp_total = 0;

                for (Lock<?> ll : tech.lockers.all()){ // For all unlockables

                        for (RoomBlueprint h : ROOMS().all()) { // For each type of room blueprint    Note: We need SETT.ROOMS() to find the RoomInstance bonuses
                                if (  !(h instanceof RoomBlueprintIns)  ) { continue; } // Industries only

                                for (RoomInstance r : ((RoomBlueprintIns<?>) h).all()) { // For each workshop in the industry
                                        if (r.employees() == null || !(r instanceof ROOM_PRODUCER_INSTANCE)) { continue; } // That has employees and produces goods

                                        RoomBlueprintImp b = (RoomBlueprintImp) r.blueprint();  // Get blueprint to find unlocks
                                        try {b.upgrades().requires(r.upgrade(r.mX(), r.mY())+1).all().isEmpty();}catch(Exception e){continue;} //Skip this room if there are no upgrades
                                        Industry ind = ((ROOM_PRODUCER_INSTANCE) r).industry();  // Industry of the workshop
                                        Boostable bonus = ind.bonus(); // The boosts of the industry


                                        if (bonus == null) { continue; } // Boost exists in industry and matches the tech



                                        boolean contains = false;
                                        // MAINTENANCE COSTS
                                        for (Lock<Faction> s : b.upgrades().requires(r.upgrade(r.mX(), r.mY())+1).all()) { //loop through all buildings
                                                if (s.unlocker.name == ll.unlocker.name) { // If the tech matches the building
                                                        contains_upgrade = true; // Allow it to be shown later
                                                        contains = true;
                                                        benefit_emp_total += r.employees().employed(); // Employee count used for tools and maintenance
                                                        if (r.employees().employed()==0){continue;} // Skip if no employees
                                                        int up = 1; // +1 upgrade level
                                                        if ( r.upgrade() == r.blueprintI().upgrades().max() ){up=0;} // Unless it's already at the max

                                                        ROOM_DEGRADER deg = r.degrader(r.mX(), r.mY()); // rateResource requirement
                                                        double iso = r.isolation(r.mX(), r.mY()); 	// rateResource requirement
                                                        double boost = MAINTENANCE().speed();   	// rateResource requirement

                                                        double total_room_maintenance_import = 0;		// reset 'current building' stats
                                                        double total_room_maintenance_import_upgraded = 0;	// reset 'current building' stats
                                                        if (deg == null){ continue; }    			// If no maintenance, skip
                                                        for (int i = 0; i < deg.resSize(); i++) {		// For each resource
                                                                if (r.resAmount(i,up+r.upgrade()) <= 0){continue;}	// skip if resource use is 0 (at upgraded maintenance level)

                                                                // Maintenance amount per day for current upgrade level
                                                                double n = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, r.resAmount(i,r.upgrade()))* TIME.years().bitConversion(TIME.days()) / 16.0;
                                                                // Maintenance amount per day for upgraded level (if not maxed)
                                                                double m = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, r.resAmount(i,up+r.upgrade()))* TIME.years().bitConversion(TIME.days()) / 16.0;
                                                                double sellFor = FACTIONS.player().trade.pricesBuy.get(deg.res(i).tr()); 	// get the import cost of the resource
                                                                total_room_maintenance_import -= n * sellFor;				// multiply cost * amount of resource (not upgraded)
                                                                total_room_maintenance_import_upgraded -= m * sellFor;			// multiply cost * amount of resource (upgraded)

                                                        }
                                                        benefit_maint_total += total_room_maintenance_import; 				// Add cost for each room
                                                        benefit_maint_upgrade_total += total_room_maintenance_import_upgraded; 		// Add cost for each room
                                                }
                                        }
                                        if (contains) { // If this room had an unlock
                                                // UPGRADE BOOSTS BENEFITS
                                                double add = 0;
                                                int tot = 0;
                                                double upgrade = 0;

                                                for (Humanoid person : r.employees().employees()) { // for each person working
                                                        tot++; //adding up the number of employees
                                                        if (STATS.WORK().EMPLOYED.get(person) == r) { //IDK if this is needed, copied from hoverBoosts function
                                                                for (Booster s : bonus.all()) { // look at all boosts an industry has
                                                                        if (!s.isMul) {
                                                                                add += s.get(person.indu());
                                                                        } // add up the non-multiplier bonuses
                                                                        if (Objects.equals(s.info.name, "Upgrade")) {
                                                                                upgrade = b.upgrades().boost(r.upgrade() + 1);
                                                                        } // assign upgrade the booster amount
                                                                }
                                                        }
                                                }
                                                add /= tot; // add is the total additive bonuses.
                                                if ((tot * upgrade / (1 + add)) > 0) {
                                                        benefits += tot * upgrade / (1 + add); //Add the technology's benefit of each workshop
                                                }
                                        }
                                }
                        }
                }
                if (benefit_emp_total >0) { benefit_maint_before = benefit_maint_total / benefit_emp_total; }
                if (benefit_emp_total >0) { benefit_maint_upgrade = benefit_maint_upgrade_total / benefit_emp_total; }
        }
        void booster_tools(TECH tech)
        {

                // Adds up the boost benefits for every boost in the tech, by industry, by room, per person.
                double benefit_emp_total = 0;
                double extra_tools = 0;
                for (BoostSpec b : tech.boosters.all()) { // For all boosts of this tech,

                        for (RoomBlueprint h : ROOMS().all()) { // For each type of room blueprint    Note: We need SETT.ROOMS() to find the RoomInstance bonuses
                                if (  !(h instanceof RoomBlueprintIns)  ) { continue; } // Industries only

                                for (RoomInstance r : ((RoomBlueprintIns<?>) h).all()) { // For each workshop in the industry

                                        if (r.employees() == null || !(r instanceof ROOM_PRODUCER_INSTANCE)) { continue; } // That has employees and produces goods

                                        Industry ind = ((ROOM_PRODUCER_INSTANCE) r).industry();  // Industry of the workshop
                                        Boostable bonus = ind.bonus(); // The boosts of the industry

                                        if (!(Objects.equals(b.boostable.cat.name, "Equipment"))){ continue; }
                                        if (bonus == null || !Objects.equals((b.boostable.key).substring(17), h.key())) { continue; } // Boost exists in industry and matches the tech


                                        // BOOSTS BENEFITS
                                        double add = 0;
                                        int tot = 0;
                                        double tool_additive = 0;
                                        for (Humanoid person : r.employees().employees()) { // for each person working
                                                tot++; //adding up the number of employees
                                                if (STATS.WORK().EMPLOYED.get(person) == r) { //IDK if this is needed, copied from hoverBoosts function
                                                        for (Booster s : bonus.all()) { // look at all boosts an industry has
                                                                if (!s.isMul) { // add up the non-multiplier bonuses
                                                                        if (Objects.equals(s.info.name,"Tools")){ tool_additive = s.to(); }
                                                                        add += s.get(person.indu());
                                                                }
                                                        }
                                                }
                                        }
                                        add /= tot; // add is the total additive bonuses.
                                        if(  tot * b.booster.to() / (1+add) > 0  ) {
                                                for (RoomEquip w : r.blueprint().employment().tools()) {
                                                        // b.booster.to() = number of tools gained from the boost
                                                        // w.target(r.blueprintI().employment()).max() = number of tools max
                                                        // tool_additive =  benefit of tools, guessing 1 to 2, but pulled from the boost check above
                                                        benefits += tot * (tool_additive * b.booster.to() / w.target(r.blueprintI().employment()).max()) / (1 + add);//Add the technology's benefit of each workshop
                                                }
                                        }

                                        // TOOLS COSTS (before)
                                        RoomEmploymentSimple ee = r.blueprint().employment();
                                        RoomEmploymentIns e = r.employees();

                                        for (RoomEquip w : ee.tools()) {
                                                double n = w.degradePerDay * e.tools(w);
                                                double sellFor = FACTIONS.player().trade.pricesBuy.get(w.resource.tr());
                                                benefit_tools -= n * sellFor;

                                        }
                                        benefit_emp_total += r.employees().employed(); // Employee count used for tools and maintenance

                                }
                        }
                }
                if (benefit_emp_total >0) { benefit_tools += extra_tools / benefit_emp_total; }

        }
        public static double resource_use(String Source) {
                double tot = 0;
                for (RESOURCE res : RESOURCES.ALL()) {
                        for (RoomProduction.Source rr : SETT.ROOMS().PROD.consumers(res)) {
                                if (Objects.equals(rr.name(), Source)){
                                        if (rr.am() == 0) {continue;}
                                        // import price
                                        // FACTIONS.player().trade.pricesBuy.get(res)
                                        // resource "value" (average value to factions across the world)
                                        // FACTIONS.PRICE().get(res)
                                        tot -= rr.am() * FACTIONS.PRICE().get(res.tr()) ;
                                }
                        }
                }
                return tot;
        }



        public static double next_tech_benefit(BoostSpec bb){
                double cur = 0;
                for ( Boostable A : BOOSTABLES.CIVICS().all() ) {
                        if (Objects.equals(bb.boostable.key(), A.key)) {
                                cur = A.get(HCLASS_RACE.clP(null, null));
                        }
                }
                double v = bb.booster.to(); 	// benefit per level of tech
                double w = cur + v;		// current benefit from all sources +1 tech level
                v = cur;			// current benefit from all sources

                return 100 * (1 - v/w);
        }
        private double boostValue(RoomBlueprintImp r, Boostable bo, double increase, boolean isMul) {





                double employees = r.employment().employed();
                double current = bo.get(HCLASS_RACE.clP());
                double next = current;
                if (isMul) {
                        next = BUtil.value(bo.all(), HCLASS_RACE.clP(), bo.baseValue, increase, bo.minValue);
                }else {
                        next = BUtil.value(bo.all(), HCLASS_RACE.clP(), bo.baseValue + increase, 1, bo.minValue);
                }

                double res = employees*(next-current)/current;


                return res;


        }

}
