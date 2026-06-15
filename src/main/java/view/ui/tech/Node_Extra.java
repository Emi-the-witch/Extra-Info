package view.ui.tech;

import game.boosting.BoostSpec;
import game.faction.FACTIONS;
import game.faction.player.PTech;
import init.sprite.UI.UI;
import init.tech.TECH;
import init.tech.TechCost;
import util.colors.GCOLOR;
import util.dic.ExtraInfoDic;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.keyboard.KEYS;
import java.util.Objects;
import static init.tech.CostBenefit.*;
import static init.tech.Knowledge_Costs.*;
import static view.ui.goods.UIMaintenance.sum_d;

public class Node_Extra{
        public double worker_cost ;
        public void  output(TECH tech, GBox b){
                output0(tech, b); // All techs, cost in workers, extra: breakdown of costs
                output1(tech, b); // Production Bonus Tech text
                output2(tech, b); // Maint/Spoil/Furniture use reduction tech

        }

        public void output0(TECH tech, GBox b)
        {
                //////////////////////////////////////////////////////////////////////////////////////
                /////////      For All Techs           ///////////////////////////////////////////////
                /////////      Always displayed        ///////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////
                // Calculate the total costs and benefits for simplification
                int j = 0;
                worker_cost   = 0;
                double material_cost = 0; // non-worker cost, total
                double production_maint_cost = 0;
                double production_tools_cost = 0;
                if (tech.costs == null){return;}
                if (FACTIONS.player() == null){return;}
                if (FACTIONS.player().tech == null){return;}

                boolean error = false;
                for (TechCost c : tech.costs) {
                        PTech t = FACTIONS.player().tech();
                        int cost = t.costLevelNext(c.amount, tech);

                        // No workers / knowledge per workers for the tech *and* cost >0, this can't be calculated
                        if (know_worker[c.cu.index] == 0){
                                error = true;
                                continue;
                        }

                        // Otherwise, Tech cost / knowledge per worker = number of workers needed
                        worker_cost += cost / know_worker[c.cu.index];

                        // Total costs per worker for each currency type
                        material_cost += cost_total[c.cu.index];

                        // Cost per worker of the production facility
                        production_maint_cost = benefit_maint;
                        production_tools_cost = benefit_tools;

                }





                ////////////// Calculate cost-benefit analysis #
                if (tech.Tech_CostBenefit.benefits != 0 && !error) {
                        b.sep(); // move here because no line before
                        b.NL();
                        b.add(GFORMAT.f(b.text(), tech.Tech_CostBenefit.benefits / worker_cost , 3));
                        b.tab(2);
                        // b.add(GFORMAT.text(b.text(), "Below '1' means you are spending more labor in tech buildings than you'd gain from having the tech."));
                        b.add(GFORMAT.text(b.text(), ExtraInfoDic.techBelow1));
                }




                if (!KEYS.MAIN().UNDO.isPressed()) {
                        b.sep(); // add a line

                        //////////// Costs display
                        b.NL();
                        if (!error) {
                                // ExtraInfoDic.techCost == {0} Cost in workers
                                GText tmp = GFORMAT.text(b.text(), ExtraInfoDic.techCost);
                                tmp.insert(0, String.format("%-13.1f", worker_cost)); // 2 tab ~= 13 space (don't know why)
                                b.add(tmp);
                        }else{
                                // b.add(GFORMAT.text(b.text(), "Unable to calculate the cost in workers"));
                                b.add(GFORMAT.text(b.text(), ExtraInfoDic.techCostFail));
                        }


                        /////////// Benefits display
                        b.NL();
                        if (tech.Tech_CostBenefit.benefits != 0) {
                                // ExtraInfoDic.techBenefit == {0} Benefit in workers
                                GText tmp = GFORMAT.text(b.text(), ExtraInfoDic.techBenefit);
                                tmp.insert(0, String.format("%-13.1f", tech.Tech_CostBenefit.benefits)); // 13 space
                                b.add(tmp);
                        }else{
                                // b.add(GFORMAT.text(b.text(), "Unable to calculate the benefits in workers"));
                                b.add(GFORMAT.text(b.text(), ExtraInfoDic.techBenefitFail));
                        }


                        b.sep();
                        // b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Press Undo button (shift) for more info or to refresh the calculations"));
                        GText tmp = GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.techTip);
                        tmp.insert(0, KEYS.MAIN().UNDO.repr());
                        b.add(tmp);
                        b.sep(); // todo seems like no need of a line here
                }
                //////////////////////////////////////////////////////////////////////////////////////
                /////////      If pressing shift       ///////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////

                if (KEYS.MAIN().UNDO.isPressed()) {
                        /////////
                        b.sep(); // add a line
                        b.NL();
                        b.add(GFORMAT.text(b.text(), " "));
                        b.NL();
                        b.add(GFORMAT.text(b.text(), "Summary of benefits of this technology:"));

                        b.NL();   // Production worker - Tech worker
                        b.add(GFORMAT.f(b.text(), (tech.Tech_CostBenefit.benefits - worker_cost), 1));
                        b.tab(2);
                        b.add(GFORMAT.text(b.text(), "Labor benefit"));

                        b.NL();  // Production workers * production costs - tech workers * tech costs
                        // costs are negative already so we do the inverse.
                        b.add(GFORMAT.f(b.text(), -( tech.Tech_CostBenefit.benefits * (production_maint_cost+production_tools_cost) - worker_cost * material_cost), 1));
                        b.tab(2);
                        b.add(GFORMAT.text(b.text(), "Running costs benefit"));




                        b.NL();
                        b.add(GFORMAT.text(b.text(), " "));
                        b.NL();
                        b.add(GFORMAT.text(b.text(), "Detailed Cost Benefit:"));

                        b.NL();
                        b.add(GFORMAT.f(b.text(), (worker_cost), 1));
                        b.tab(2);
                        b.add(GFORMAT.text(b.text(), "Tech workers cost"));

                        b.NL();
                        b.add(GFORMAT.f(b.text(), (worker_cost * material_cost), 1));
                        b.tab(2);
                        b.add(GFORMAT.text(b.text(), "Tech worker running cost"));

                        b.NL();
                        b.add(GFORMAT.f(b.text(), (tech.Tech_CostBenefit.benefits), 1));
                        b.tab(2);
                        b.add(GFORMAT.text(b.text(), "Production worker benefit"));

                        b.NL();
                        b.add(GFORMAT.f(b.text(), (tech.Tech_CostBenefit.benefits * (production_maint_cost + production_tools_cost)), 1));
                        b.tab(2);
                        b.add(GFORMAT.text(b.text(), "Production maint+tool costs"));




                        b.NL();
                        b.add(GFORMAT.text(b.text(), " "));
                        b.NL();
                        b.add(GFORMAT.text(b.text(), "Per worker costs for this technology"));

                        b.NL();
                        b.add(GFORMAT.f(b.text(), material_cost, 1));
                        b.tab(2);
                        b.add(GFORMAT.text(b.text(), "Tech worker"));


                        // sum production workers divided by the sum of workplaces maintenance + tools
                        b.NL();
                        b.add(GFORMAT.f(b.text(), production_maint_cost+production_tools_cost, 1));
                        b.tab(2);
                        b.add(GFORMAT.text(b.text(), "Production worker"));
                }
        }
        public void output1(TECH tech, GBox b)
        {
                //////////////////////////////////////////////////////////////////////////////////////
                /////////      Production Bonus Tech     /////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////
                /////////      Always displayed        ///////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////
                tech.Tech_CostBenefit.update(tech);


                // lol nothing

                //////////////////////////////////////////////////////////////////////////////////////
                /////////      If pressing shift       ///////////////////////////////////////////////
                //////////////////////////////////////////////////////////////////////////////////////
                if (KEYS.MAIN().UNDO.isPressed()) {

                        for (PTech.TechCurr tech_value : FACTIONS.player().tech().currs()) {
                                int i = tech_value.cu.index;

                                b.NL();
                                b.add(GFORMAT.text(b.text(), " "));
                                b.NL();

                                b.add(GFORMAT.text(b.text(), tech_value.cu.bo.name));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), know_tot[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Tech currency total"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), know_emp[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Employment"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), know_worker[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Tech per worker"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), cost_total[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Total Costs per worker"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), cost_inputs[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Total input Costs"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), cost_maint[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Total Maintenance costs"));

                                b.NL();
                                b.add(GFORMAT.f(b.text(), cost_tools[i], 1));
                                b.tab(2);
                                b.add(GFORMAT.text(b.text(), "Total Tools costs"));

                                b.NL();
                                b.NL();

                        }

                }

        }



        // Maintenance, Spoilage, Furniture cost reduction boosts
        public void output2(TECH tech, GBox b) {
                double maint_output=0;
                double spoil_output=0;
                double furniture_output=0;

                for (BoostSpec bb : tech.boosters.all()) {

                        if (Objects.equals(bb.boostable.key(), "CIVIC_MAINTENANCE")) {
                                double costs = resource_use("Maintenance");
                                maint_output = tech_divisor_presentation(tech, "maintenance", costs, bb, b);

                        }
                        if (Objects.equals(bb.boostable.key(), "CIVIC_SPOILAGE")) {
                                double costs = resource_use("Spoilage");
                                spoil_output = tech_divisor_presentation(tech, "spoilage", costs, bb, b);
                                b.sep();
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Note: Spoilage estimate excludes floor spoilage which gets no benefit from spoilage tech"));
                                b.NL();
                        }
                        if (Objects.equals(bb.boostable.key(), "CIVIC_FURNITURE")) {
                                double costs = resource_use("Furniture");
                                furniture_output = tech_divisor_presentation(tech, "furniture", costs, bb, b);
                        }

//                        if (KEYS.MAIN().UNDO.isPressed()) {
//                                if (!(tech.Tech_CostBenefit.benefit_maint == 0 && tech.Tech_CostBenefit.benefit_tools == 0 && tech.Tech_CostBenefit.benefit_tot == 0)) {
//                                        b.sep();
//                                }
//
//                                if (tech.Tech_CostBenefit.benefit_maint != 0) {
//                                        b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech.Tech_CostBenefit.benefit_maint * 10) / 10, 1).color(GCOLOR.T().IBAD));
//                                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Maintenance costs per production worker"));
//                                        b.NL();
//                                }
//                                if (tech.Tech_CostBenefit.benefit_tools != 0) {
//                                        b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech.Tech_CostBenefit.benefit_tools * 10) / 10, 1).color(GCOLOR.T().IBAD));
//                                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Tool costs per production worker"));
//                                        b.NL();
//                                }
//                                if (tech.Tech_CostBenefit.benefit_tot != 0) {
//                                        b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech.Tech_CostBenefit.benefit_tot * 10) / 10, 1).color(GCOLOR.T().IBAD));
//                                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Total upkeep costs per production worker"));
//                                        b.NL();
//                                }
//                                if (!(tech.Tech_CostBenefit.benefit_maint == 0 && tech.Tech_CostBenefit.benefit_tools == 0 && tech.Tech_CostBenefit.benefit_tot == 0)) {
//                                        b.NL();
//                                }
//                        }
                }
                if (KEYS.MAIN().UNDO.isPressed()) {



//                        if (!(sum_d(cost_maint) == 0 && sum_d(cost_tools) == 0 && sum_d(cost_inputs) == 0 && cost_tot == 0)) {
//                                b.sep();
//                        }
//                        if (sum_d(cost_maint) != 0) {// If sum of employment array isn't 0, maint/emp sums to 1 decimal or display 0
//                                b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) (sum_d(know_emp)!=0 ? Math.ceil(sum_d(cost_maint)/sum_d(know_emp) * 10)  / 10 : 0), 1).color(GCOLOR.T().IBAD));
//                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Maintenance costs per knowledge worker"));
//                                b.NL();
//                        }
//                        if (sum_d(cost_tools) != 0) {
//                                b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(sum_d(cost_tools) * 10) / 10, 1).color(GCOLOR.T().IBAD));
//                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Tool costs per knowledge worker"));
//                                b.NL();
//                        }
//                        if (sum_d(cost_inputs) != 0) {
//                                b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(sum_d(cost_inputs) * 10) / 10, 1).color(GCOLOR.T().IBAD));
//                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Input costs per knowledge worker (paper)"));
//                                b.NL();
//                        }
//                        if (cost_tot != 0) {
//                                b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(cost_tot * 10) / 10, 1).color(GCOLOR.T().IBAD));
//                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Average knowledge worker costs"));
//                                b.NL();
//                        }
                        if (!(sum_d(cost_maint) == 0 && sum_d(cost_tools) == 0 && sum_d(cost_inputs) == 0 && cost_tot == 0)) {
                                b.NL();
                        }
                }
                if (!(maint_output == 0 && spoil_output == 0 && furniture_output == 0)) {
                        if (!(KEYS.MAIN().UNDO.isPressed())) {
                                b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) ((maint_output + spoil_output + furniture_output + cost_tot * tech.Tech_CostBenefit.costs) / tech.Tech_CostBenefit.costs)));
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Profit per knowledge worker"));
                                b.NL();
                                b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) ((maint_output + spoil_output + furniture_output + cost_tot * tech.Tech_CostBenefit.costs) / tech.Tech_CostBenefit.costs)));
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));
                                b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Profit per knowledge worker"));
                                b.NL();
                        }
                }

        }
        public double tech_divisor_presentation(TECH tech, String what, double denari_costs, BoostSpec bb, GBox b) {
                double tech_benefit = next_tech_benefit(bb); // % of maintenance you'll still have, e.g. 20% reduction from current tech level

                if (KEYS.MAIN().UNDO.isPressed()) {
                        b.sep();
                        b.add(GFORMAT.f(new GText(UI.FONT().S, 0), 1/( 1+ FACTIONS.player().tech.level(tech) * bb.booster.to() ) ) );
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "%"));   b.tab(2);
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Current percent from this tech"));
                        b.NL();

                        b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) Math.ceil(denari_costs)));
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Cost of all ".concat(what)));
                        b.NL();
                        b.add(GFORMAT.f(new GText(UI.FONT().S, 0), (double) Math.ceil(tech_benefit * 100) / 100, 2).color(GCOLOR.T().IGOOD));
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "%"));   b.tab(2);
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Percent reduction from this tech"));
                        b.NL();

                        b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) Math.ceil(-denari_costs * tech_benefit/ tech.Tech_CostBenefit.costs/100)));
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Estimated ".concat(what).concat(" benefits per worker")));
                        b.NL();

                        b.sep();
                        b.add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long)  ( ( -denari_costs * tech_benefit/100 + cost_tot * tech.Tech_CostBenefit.costs) / tech.Tech_CostBenefit.costs ) ) );
                        //  ( (-Amount of money saved)  + (costs per worker * # of tech workers) )/ # of tech workers
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "$"));   b.tab(2);
                        b.add(GFORMAT.text(new GText(UI.FONT().S, 0), "Cost-Benefit per knowledge worker from this tech"));
                        b.NL();
                }
                return -denari_costs * tech_benefit/100;
        }
}
