package view.ui.goods;

import game.faction.FACTIONS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.ArrayListGrower;
import util.dic.ExtraInfoDic;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.ui.manage.IFullView;
import static java.lang.Math.round;
import static settlement.stats.colls.StatsEnv.sum_emp;
import static settlement.stats.colls.StatsEnv.sum_res;

import settlement.stats.colls.StatsPopulation;

import java.text.DecimalFormat;

/////////////////////////////////////////////#!# This is a unique file that doesn't overwrite any of Jake's files.
/////#!# Helps display the # of employees who


public final class UILogistics extends IFullView {

        // private static CharSequence ¤¤Name = "Industries";
        private static CharSequence ¤¤Name = ExtraInfoDic.logistics;
        public UILogistics() {
                super(¤¤Name, UI.icons().l.coin);
        }


        @Override
        public void init() {
                section.clear();
                section.body().moveY1(IFullView.TOP_HEIGHT);
                section.body().moveX1(16);
                section.body().setWidth(WIDTH).setHeight(1);

                // Display top line messages
//                 section.addDown(0, new GText(UI.FONT().H2, "Testing Logistics"));
                section.addDown(0, new GText(UI.FONT().H2, ExtraInfoDic.logistics));





                // Display rows of resources and amounts
                section.addDown(0, new GText(UI.FONT().M, "This asks every person in the capital what they're hauling 16 times per day to make these estimates."));
                ArrayListGrower<RegRow> rows = new ArrayListGrower<>();
                GText tableHeader = new GText(UI.FONT().S, "");
                section.addDown(10, tableHeader);
                int position = 0 ;
                rows.add(new AddRow_table1(null, position, tableHeader.width())); // Source title line
                for (RESOURCE r: RESOURCES.ALL()) {
                                rows.add(new AddRow_table1(r, position, tableHeader.width()));
                                if (position % 4 == 0){rows.add(new AddRow_table1(r, -1, tableHeader.width()));}
                                position++;
                }
                GScrollRows scrollRows = new GScrollRows(rows, (int) round(HEIGHT * .42));
                section.addDown(5, scrollRows.view());



                // Display rows of resources and amounts
//                section.addDown(0, new GText(UI.FONT().M, "Average amount hauled per person"));
                ArrayListGrower<RegRow> rows2 = new ArrayListGrower<>();
                GText tableHeader2 = new GText(UI.FONT().S, "");
                section.addDown(10, tableHeader2);
                position = 0 ;
                rows2.add(new AddRow_table2(null, position, tableHeader2.width() )); // Source title line
//                rows2.add(new AddRow_table2(null, 0 )); // Source title line
                for (RESOURCE r: RESOURCES.ALL()) {
                        rows2.add(new AddRow_table2(r, position, tableHeader2.width()));
                        if (position % 4 == 0){rows2.add(new AddRow_table2(r, -1, tableHeader.width()));}
                        position++;
                }
                GScrollRows scrollRows2 = new GScrollRows(rows2, (int) round(HEIGHT * .42));
                section.addDown(5, scrollRows2.view());



        }

        private static class AddRow_table1 extends RegRow {
                // Create the row using the resource:
                AddRow_table1(RESOURCE ii, int pos, int width) {
                        // First row
                        if (ii==null){
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "Resources").adjustWidth(), incTab(4), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "Percent of active haulers moving the resource").adjustWidth(), incTab(10), MARGIN);
                                return;
                        }
                        if (pos == 0){// This is the start of the second table
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "").adjustWidth(), incTab(10), MARGIN);
                                return;
                        }
                        if (pos == -1){// help with spacing
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), " ").adjustWidth(), incTab(10), MARGIN);
                                return;
                        }
                        if (pos < sum_res.length) { // This is the first table, going from 0-pos
                                // Show each interval value for each resource
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ii.name).adjustWidth(), incTab(4), MARGIN);
                                for (int i = 0; i < sum_res[pos].length; i++) {
                                        if (sum_res[pos][i] != 0) {
                                                /// Rounded value string
                                                DecimalFormat df = new DecimalFormat("#.#");
                                                String output = df.format( 100*sum_res[pos][i]/sum_d1(sum_res, i));

                                                add(GFORMAT.text(new GText(UI.FONT().S, 0), output).adjustWidth(), incTab(2), MARGIN);
                                        } else if(sum_d1(sum_res, i)==0) {
                                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "").adjustWidth(), incTab(2), MARGIN);
                                        } else{
                                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ".").adjustWidth(), incTab(2), MARGIN);
                                        }
                                }
                        }
                }
        }        private static class AddRow_table2 extends RegRow {
                // Create the row using the resource:
                AddRow_table2(RESOURCE ii, int pos, int width) {
                        // First row
                        if (ii==null){
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "Resources").adjustWidth(), incTab(4), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "Average amount hauled at once").adjustWidth(), incTab(10), MARGIN);
                                return;
                        }
                        if (pos == 0){// This is the start of the second table
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "").adjustWidth(), incTab(10), MARGIN);
                                return;
                        }
                        if (pos == -1){// help with spacing
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), " ").adjustWidth(), incTab(10), MARGIN);
                                return;
                        }
                        if (pos < sum_res.length) { // This is the first table, going from 0-pos
                                // Show each interval value for each resource
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ii.name).adjustWidth(), incTab(4), MARGIN);
                                for (int i = 0; i < sum_res[pos].length; i++) {
                                        if (sum_res[pos][i] != 0) {
                                                /// Rounded value string
                                                DecimalFormat df = new DecimalFormat("#.#");
                                                String output = df.format((double) sum_res[pos][i]/sum_emp[pos][i]);

                                                add(GFORMAT.text(new GText(UI.FONT().S, 0), output).adjustWidth(), incTab(2), MARGIN);
                                        } else if(sum_d1(sum_res, i)==0) {
                                                add(GFORMAT.text(new GText(UI.FONT().S, 0), "").adjustWidth(), incTab(2), MARGIN);
                                        } else{
                                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ".").adjustWidth(), incTab(2), MARGIN);
                                        }
                                }
                        }
                }
        }
        /// Margin notes for Table 1 and 2:
        /// The (1st) is the "Resources" which should align with the resource names (4th)
        /// (2nd) and (3rd) are the end of their uses, so it doesn't matter
        /// (5th) and (6th) should be the same and are the distance between (up to) ~3-digit numbers, unless there are 1000+ items carried at once of the same type.
        private abstract static class RegRow extends GuiSection {
                protected static final int MARGIN = 4;
                private double tab;

                protected int incTab(double n) {
                        double t = tab;
                        tab += n;
                        return (int) (t * MARGIN * 10);
                }
        }
        public static int sum_d2(int[][] numbers, int position){
                int sum = 0;
                for (int i = 0; i < numbers[position].length; i++) {
                        sum += numbers[position][i];
                }
                return sum;
        }
        public static int sum_d1(int[][] numbers, int position){
                int sum = 0;
                for (int i = 0; i < numbers.length; i++) {
                        sum += numbers[i][position];
                }
                return sum;
        }

}
