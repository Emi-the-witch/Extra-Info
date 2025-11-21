package init.tech;

import game.faction.FACTIONS;
import game.faction.player.PTech;
import game.time.TIME;
import init.resources.RESOURCE;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.industry.module.ROOM_PRODUCER_INSTANCE;
import settlement.room.infra.admin.AdminData;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEmploymentIns;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.room.main.employment.RoomEquip;
import static settlement.main.SETT.*;

/////////////////////////////////////////////#!# This is a unique file that doesn't overwrite any of Jake's files.
/////#!# This calculates the costs of gaining tech currencies
public class Knowledge_Costs {

        private static int index = -1;
        // for each tech currency
        public static double [] know_tot    ;// total value the player has
        public static double [] know_emp    ;// employment in buildings that give this currency
        public static double [] know_worker ;// tech value per worker
        public static double [] cost_total  ;// Total costs per worker
        public static double [] cost_inputs ;// input costs
        public static double [] cost_maint  ;// maintenance costs
        public static double [] cost_tools  ;// tools costs

        private static void setup() {

                index = FACTIONS.player().tech().currs().size();
                know_tot    = new double[index];  	// total value the player has
                know_emp    = new double[index]; 	// employment in buildings that give this currency
                know_worker = new double[index]; 	// tech value per worker
                cost_total  = new double[index]; 	// Total costs per worker
                cost_inputs = new double[index];        // input costs
                cost_maint  = new double[index];        // maintenance costs
                cost_tools  = new double[index];        // tools costs
        }
        public static void costs()
        {
                setup(); // Set up the variables for the correct lengths
                ////////////////////////////////////////////////////////////////////////////////////////
                // For each tech currency, determine the worker and material cost value

                index = 0;
                for ( PTech.TechCurr tech_value : FACTIONS.player().tech().currs() ){ // For each type of tech
                        know_tot[index] = 0;  // Check the knowledge total directly -- but gives ALL sources like titles :C

                        // Check the employment by checking every building for a room instance that has that boostable
                        for (int y = 0; y < THEIGHT; y++) {
                                for (int x = 0; x < TWIDTH; x++) {
                                        if (ROOMS().map.get(x,y) == null){ continue;}           // if tile on map is not a room
                                        Room room = ROOMS().map.get(x, y);                      // Room variable

                                        // CHECK ROOM FOR GENERATING THE TECH CURRENCY
                                        if (!(room.blueprint() instanceof AdminData.ROOM_ADMIN_HOLDER)){ continue;}                 // if not an admin room instance
                                        AdminData.ROOM_ADMIN_HOLDER admin_room = (AdminData.ROOM_ADMIN_HOLDER) room.blueprint();    // Admin room variable
                                        if(tech_value.cu.bo !=  admin_room.admin().target){continue;}                   // If it doesn't match up with *THIS* currency

                                        // EMPLOYMENT
                                        if (!(room instanceof RoomInstance)){ continue;}        // if not a room instance with employees, skip
                                        RoomInstance r = (RoomInstance) room;              // Room instance variable
                                        if (r.employees() == null) { continue; }
                                        know_emp[index]  += (double) r.employees().employed() / r.area(); //  employment divided by the area of the room
                                        know_tot[index]   = admin_room.admin().value()   ;  // Will have issues if a mod has multiple buildings for the same tech currency.
//                                        know_tot[index]  += admin_room.admin().value()  / r.area() ; // It takes all of this industry not just the instance.
                                        // INPUT COSTS
                                        if (room instanceof ROOM_PRODUCER_INSTANCE){
                                                ROOM_PRODUCER_INSTANCE s = ((ROOM_PRODUCER_INSTANCE) room);
                                                double total = 0;

                                                for (int ri = 0; ri < s.industry().ins().size(); ri++) {
                                                        IndustryResource i = s.industry().ins().get(ri);
                                                        double n = i.dayPrev.get(s);
                                                        //double n = i.history().get(1); //laboratories didn't use dayPrev??
                                                        double sellFor = FACTIONS.player().trade.pricesBuy.get(i.resource);
                                                        total -= n * sellFor;
                                                }
                                                cost_inputs[index] += total / r.area();
                                        }

                                        // TOOLS
                                        RoomEmploymentIns e = r.employees();
                                        RoomEmploymentSimple ee = r.blueprint().employment();
                                        for (RoomEquip w : ee.tools()) {
                                                double n = w.degradePerDay * e.tools(w);
                                                double sellFor = FACTIONS.player().trade.pricesBuy.get(w.resource);
                                                cost_tools[index] -= n * sellFor / r.area();
                                        }

                                        // MAINTENANCE
                                        ROOM_DEGRADER deg = r.degrader(r.mX(), r.mY());
                                        double iso = r.isolation(r.mX(), r.mY());
                                        double boost = MAINTENANCE().speed();

                                        if (deg == null) { continue; }
                                        for (int i = 0; i < deg.resSize(); i++) {
                                                if (deg.resAmount(i) <= 0)
                                                        continue;
                                                RESOURCE res = deg.res(i);

                                                double n = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, deg.resAmount(i)) * TIME.years().bitConversion(TIME.days()) / 16.0;
                                                double sellFor = FACTIONS.player().trade.pricesBuy.get(res);
                                                cost_maint[index] -= n * sellFor / r.area();
                                        }
                                }
                                if (know_emp[index] != 0) { know_worker[index] = know_tot[index] / know_emp[index] ; }
                                if (know_emp[index] != 0) { cost_total[index] = ( cost_inputs[index] + cost_maint[index] + cost_tools[index] ) / know_emp[index] ; }
                        }
                        index += 1;
                }


        }
}
