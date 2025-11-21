package view.sett.ui.room;

import game.faction.FACTIONS;
import game.time.TIME;
import init.resources.RESOURCE;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.industry.module.ROOM_PRODUCER_INSTANCE;
import settlement.room.food.farm.FarmInstance;
import settlement.room.industry.module.RoomProduction;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEmploymentIns;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.room.main.employment.RoomEquip;
import settlement.room.food.farm.Util;
import util.data.GETTER;
import settlement.room.food.orchard.Instance;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static view.sett.ui.room.ModuleIndustry.g;

public class ProfitCalc {

        /////////////////////////////////////////////////////////////////////////////////////////////////
        /// #!# This adds the profit on a building, specifying each cost and revenue and calculating profit per person.
        /// #!# This needs serious work to separate from Jake's code and update to look nicer
        /// #!# Make 3 values: For Self, and For Sale - and the weighted average of the two
        /// For Self: use the value as if you would have bought it instead
        /// For Sale: use the sale price
        /// #!# Have a hover over UI for each to describe the costs and revenues.
        ////////////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#

        public static double revenue;  // price if you sell it
        public static double saved;    // price if you were to buy it
        public static double weighted_average;
        public static double percent_for_self;
        public static double consumed;
        public static double produced;
        public static double inputs;
        public static double tools;
        public static double maintenance;
        public static double profit1; // Profit per room
        public static double profit2;
        public static double profit3; // weighted average version
        public static double ppp1; // Profit per person for sale
        public static double ppp2; // Profit per person for self
        public static double ppp3; // Profit per person for weighted average

        public static void refresh(GETTER<RoomInstance> get){
                ROOM_PRODUCER_INSTANCE p = ((ROOM_PRODUCER_INSTANCE) g(get));
                RoomInstance ins = get.get();
                //////////////////////////////////////////////////////////////////////
                // "Revenue" or "Money saved" / value added
                revenue =0;  // Revenue "for trade"
                saved =0;    // Revenue "for self"
                weighted_average= 0; // Revenue depending on actual consumption

                for(int ri = 0; ri<p.industry().outs().size();ri++){
                        IndustryResource i = p.industry().outs().get(ri);
                        double n = i.dayPrev.get(p);

                        double sellFor = FACTIONS.player().trade.pricesSell.get(i.resource);
                        revenue += n * sellFor;

                        double sellFor2 = FACTIONS.player().trade.pricesBuy.get(i.resource);
                        saved += n * sellFor2;

                        consumed = 0;
                        produced = 0;
                        // consumed / produced = quantities of the resource for *this* resource
                        // tot_consumed / tot_produced = total of resource quantities and average value of the resource
                        for (RoomProduction.Source rr : SETT.ROOMS().PROD.consumers(i.resource)) {
                                consumed += rr.am();
                        }
                        for (RoomProduction.Source rr : SETT.ROOMS().PROD.producers(i.resource)) {
                                produced += rr.am();
                        }

                        // Price of goods based on amount for self or sale
                        weighted_average +=
                                // Percent for self
                                (  min((consumed/produced),1)) * n * sellFor2 +
                                        // Percent for sale
                                        (1 - min((consumed/produced),1)) * n * sellFor;

                }
                //////////////////////////////////////////////////////////////////////
                // Input materials
                inputs =0;  // Input materials costs
                if (p.industry().ins() != null) {
                        for (int ri = 0; ri < p.industry().ins().size(); ri++) {
                                IndustryResource i = p.industry().ins().get(ri);
                                double n = i.dayPrev.get(p);
                                double sellFor = FACTIONS.player().trade.pricesBuy.get(i.resource);
                                inputs -= n * sellFor;
                        }
                }
                //////////////////////////////////////////////////////////////////////
                // Tools cost!
                tools =0; // Tools costs
                if(ins.blueprint().employment() !=null){

                        RoomEmploymentSimple ee = ins.blueprint().employment();
                        RoomEmploymentIns e = ins.employees();

                        for (RoomEquip w : ee.tools()) {
                                double n = w.degradePerDay * e.tools(w);
                                double sellFor = FACTIONS.player().trade.pricesBuy.get(w.resource);
                                tools -= n * sellFor;
                        }
                }
                //////////////////////////////////////////////////////////////////////
                // Maintenance calculation!
                ROOM_DEGRADER deg = get.get().degrader(get.get().mX(), get.get().mY());
                double iso = ins.isolation(get.get().mX(), get.get().mY());
                double boost = SETT.MAINTENANCE().speed();

                maintenance =0;
                if (ins.blueprintI().degrades()) {
                        for (int i = 0; i < deg.resSize(); i++) {
                                if (deg.resAmount(i) <= 0)
                                        continue;
                                RESOURCE res = deg.res(i);

                                double n = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, deg.resAmount(i)) * TIME.years().bitConversion(TIME.days()) / 16.0;
                                double sellFor = FACTIONS.player().trade.pricesBuy.get(res);
                                maintenance -= n * sellFor;
                        }
                }
                //////////////////////////////////////////////////////////////////////
                // Total Profit
                profit1 = revenue          + inputs + tools + maintenance;
                profit2 = saved            + inputs + tools + maintenance;
                profit3 = weighted_average + inputs + tools + maintenance;
                // weighted average revenue = A * saved + (1-A) * revenue
                // Where A is the percent used for self and 1-A is percent used for sale
                //		AVG = A * saved + (1-A) * revenue
                //		AVG = A * saved + revenue - A * revenue
                //		AVG - revenue = A * saved - A * revenue
                //		(AVG - revenue) = A * ( saved - revenue)
                //		(AVG - revenue) /  ( saved - revenue) = A

                percent_for_self =  (weighted_average - revenue) / (saved - revenue);
                RoomEmploymentIns e = ins.employees();
                double employedBLD = max(1, e.employed());
                ppp1 = profit1 / employedBLD;
                ppp2 = profit2 / employedBLD;
                ppp3 = profit3 / employedBLD;
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
        public static void refresh2(GETTER<FarmInstance> getter, IndustryResource i){
                //ROOM_PRODUCER_INSTANCE p = ((ROOM_PRODUCER_INSTANCE) g((RoomInstance)getter));
                FarmInstance ins = getter.get();
                double amountProduced = Util.prevHarvest(ins);
                //prevHarvest is last year's harvest
                //prospect is the estimate.
                //////////////////////////////////////////////////////////////////////
                // "Revenue" or "Money saved" / value added
                revenue =0;  // Revenue "for trade"
                saved =0;    // Revenue "for self"
                weighted_average= 0; // Revenue depending on actual consumption
                inputs=0;
                maintenance=0;
//                for(int ri = 0; ri<p.industry().outs().size();ri++){
//                        IndustryResource i = p.industry().outs().get(ri);
//                        double n = i.dayPrev.get(p);
                double n = amountProduced / 16;

                double sellFor = FACTIONS.player().trade.pricesSell.get(i.resource);
                revenue += n * sellFor;

                double sellFor2 = FACTIONS.player().trade.pricesBuy.get(i.resource);
                saved += n * sellFor2;

                consumed = 0;
                produced = 0;
                // consumed / produced = quantities of the resource for *this* resource
                // tot_consumed / tot_produced = total of resource quantities and average value of the resource
                for (RoomProduction.Source rr : SETT.ROOMS().PROD.consumers(i.resource)) {
                        consumed += rr.am();
                }
                for (RoomProduction.Source rr : SETT.ROOMS().PROD.producers(i.resource)) {
                        produced += rr.am();
                }

                // Price of goods based on amount for self or sale
                weighted_average +=
                        // Percent for self
                        (  min((consumed/produced),1)) * n * sellFor2 +
                                // Percent for sale
                                (1 - min((consumed/produced),1)) * n * sellFor;

//                }

                //////////////////////////////////////////////////////////////////////
                // Tools cost!
                tools =0; // Tools costs
                if(ins.blueprint().employment() !=null){

                        RoomEmploymentSimple ee = ins.blueprint().employment();
                        RoomEmploymentIns e = ins.employees();

                        for (RoomEquip w : ee.tools()) {
                                double m = w.degradePerDay * e.tools(w);
                                double toolSellFor = FACTIONS.player().trade.pricesBuy.get(w.resource);
                                tools -= m * toolSellFor;
                        }
                }

                //////////////////////////////////////////////////////////////////////
                // Total Profit
                profit1 = revenue          + inputs + tools + maintenance;
                profit2 = saved            + inputs + tools + maintenance;
                profit3 = weighted_average + inputs + tools + maintenance;
                // weighted average revenue = A * saved + (1-A) * revenue
                // Where A is the percent used for self and 1-A is percent used for sale
                //		AVG = A * saved + (1-A) * revenue
                //		AVG = A * saved + revenue - A * revenue
                //		AVG - revenue = A * saved - A * revenue
                //		(AVG - revenue) = A * ( saved - revenue)
                //		(AVG - revenue) /  ( saved - revenue) = A

                percent_for_self =  (weighted_average - revenue) / (saved - revenue);
                RoomEmploymentIns e = ins.employees();
                double employedBLD = max(1, e.employed());
                ppp1 = profit1 / employedBLD;
                ppp2 = profit2 / employedBLD;
                ppp3 = profit3 / employedBLD;
        }
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
        public static void refresh3(GETTER<Instance> getter, IndustryResource i){
                //ROOM_PRODUCER_INSTANCE p = ((ROOM_PRODUCER_INSTANCE) g((RoomInstance)getter));
                //
                Instance ins = getter.get();
                double amountProduced = (int)ins.blueprintI().industries().get(0).outs().get(0).yearPrev.get(ins);
                //prevHarvest is last year's harvest
                //prospect is the estimate.
                //////////////////////////////////////////////////////////////////////
                // "Revenue" or "Money saved" / value added
                revenue =0;  // Revenue "for trade"
                saved =0;    // Revenue "for self"
                weighted_average= 0; // Revenue depending on actual consumption
                inputs=0;
                maintenance=0;
//                for(int ri = 0; ri<p.industry().outs().size();ri++){
//                        IndustryResource i = p.industry().outs().get(ri);
//                        double n = i.dayPrev.get(p);
                double n = amountProduced / 16;

                double sellFor = FACTIONS.player().trade.pricesSell.get(i.resource);
                revenue += n * sellFor;

                double sellFor2 = FACTIONS.player().trade.pricesBuy.get(i.resource);
                saved += n * sellFor2;

                consumed = 0;
                produced = 0;
                // consumed / produced = quantities of the resource for *this* resource
                // tot_consumed / tot_produced = total of resource quantities and average value of the resource
                for (RoomProduction.Source rr : SETT.ROOMS().PROD.consumers(i.resource)) {
                        consumed += rr.am();
                }
                for (RoomProduction.Source rr : SETT.ROOMS().PROD.producers(i.resource)) {
                        produced += rr.am();
                }

                // Price of goods based on amount for self or sale
                weighted_average +=
                        // Percent for self
                        (  min((consumed/produced),1)) * n * sellFor2 +
                                // Percent for sale
                                (1 - min((consumed/produced),1)) * n * sellFor;

//                }

                //////////////////////////////////////////////////////////////////////
                // Tools cost!
                tools =0; // Tools costs
                if(ins.blueprint().employment() !=null){

                        RoomEmploymentSimple ee = ins.blueprint().employment();
                        RoomEmploymentIns e = ins.employees();

                        for (RoomEquip w : ee.tools()) {
                                double m = w.degradePerDay * e.tools(w);
                                double toolSellFor = FACTIONS.player().trade.pricesBuy.get(w.resource);
                                tools -= m * toolSellFor;
                        }
                }

                //////////////////////////////////////////////////////////////////////
                // Total Profit
                profit1 = revenue          + inputs + tools + maintenance;
                profit2 = saved            + inputs + tools + maintenance;
                profit3 = weighted_average + inputs + tools + maintenance;
                // weighted average revenue = A * saved + (1-A) * revenue
                // Where A is the percent used for self and 1-A is percent used for sale
                //		AVG = A * saved + (1-A) * revenue
                //		AVG = A * saved + revenue - A * revenue
                //		AVG - revenue = A * saved - A * revenue
                //		(AVG - revenue) = A * ( saved - revenue)
                //		(AVG - revenue) /  ( saved - revenue) = A

                percent_for_self =  (weighted_average - revenue) / (saved - revenue);
                RoomEmploymentIns e = ins.employees();
                double employedBLD = max(1, e.employed());
                ppp1 = profit1 / employedBLD;
                ppp2 = profit2 / employedBLD;
                ppp3 = profit3 / employedBLD;
        }
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////#!#
}
