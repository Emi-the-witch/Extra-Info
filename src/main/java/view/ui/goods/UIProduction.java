package view.ui.goods;

import game.faction.FACTIONS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.industry.module.RoomProduction;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.ArrayListGrower;
import util.dic.ExtraInfoDic;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.ui.manage.IFullView;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
/////////////////////////////////////////////#!# This is a unique file that doesn't overwrite any of Jake's files.
/////#!# Displays all the PROD.producers and is relied upon by UITreasury for the consumers and producers values

public final class UIProduction extends IFullView {

        static double total_export = 0;
        static double total_value = 0;
        // private static CharSequence ¤¤Name = "Production";
        private static CharSequence ¤¤Name = ExtraInfoDic.production;
        public UIProduction() {
                super(¤¤Name, UI.c_icons().l.plus);
        }


        @Override
        public void init() {
                section.clear();
                section.body().moveY1(IFullView.TOP_HEIGHT);
                section.body().moveX1(16);
                section.body().setWidth(WIDTH).setHeight(1);


                // Display top line messages
                // section.addDown(0, new GText(UI.FONT().H2, "Producers"));
                section.addDown(0, new GText(UI.FONT().H2, ExtraInfoDic.producers));
                ArrayListGrower<RegRow> rows = new ArrayListGrower<>();
                GText tableHeader = new GText(UI.FONT().S, "                                                                              ");
                section.addDown(10, tableHeader);

                // Order by source
                HashMap<CharSequence, ArrayListGrower<RoomProduction.Source>> data = new HashMap<>();
                for (RESOURCE res : RESOURCES.ALL()) {
                        for (RoomProduction.Source ii : SETT.ROOMS().PROD.producers(res)) {
                                if (!data.containsKey(ii.name())) {
                                        data.put(ii.name(), new ArrayListGrower<>());
                                }
                                data.get(ii.name()).add(ii);
                        }
                }
                // Display by source
                for (Map.Entry<CharSequence, ArrayListGrower<RoomProduction.Source>> item :       data.entrySet()) {
                        CharSequence category = item.getKey();

                        //Check if any value >0
                        boolean check = false;
                        for (RoomProduction.Source ii : item.getValue()) {
                                if (ii.am() != 0) {
                                        check = true;
                                }
                        }
                        if (!check){continue;}
                        // Reset the totals for the next category
                        total_export = 0;
                        total_value = 0;

                        // Display rows of resources and amounts
                        rows.add(new AddRow(null, tableHeader.width(), (String) category )); // Source title line
                        rows.add(new AddRow(null, tableHeader.width(), "columns" )); // column description line
                        for (RoomProduction.Source ii : item.getValue()) {
                                if (ii.am() != 0) {
                                        rows.add(new AddRow(ii, tableHeader.width(), ""));
                                }
                        }
                        rows.add(new AddRow(null, tableHeader.width(), "total" )); // total line
                        rows.add(new AddRow(null, tableHeader.width(), "space" )); // space line(s)
                        rows.add(new AddRow(null, tableHeader.width(), "space" )); // space line(s)
                }


                // Display the rows!
                GScrollRows scrollRows = new GScrollRows(rows, HEIGHT-20);
                section.addDown(5, scrollRows.view());
        }

        private class AddRow extends RegRow {
                // Create the row using the resource:
                AddRow(RoomProduction.Source ii, int width, String spec) {

                        if ( ii != null ) {
                                body().setWidth(width).setHeight(1);

                                // Display resource.icon()
                                add(GFORMAT.f(new GText(UI.FONT().S, 0), ii.am()).adjustWidth(), incTab(2), MARGIN);

                                // Amount of resource used:
                                add(ii.res.icon(), incTab(3), 0);

                                // Export values for that resource:
                                add(GFORMAT.i(new GText(UI.FONT().S, 0), (long) (ii.am() * FACTIONS.player().trade.pricesSell.get(ii.res))).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.denari).adjustWidth(), incTab(4), MARGIN);

                                // Value of those resources:
                                add(GFORMAT.i(new GText(UI.FONT().S, 0), (long) (ii.am() * FACTIONS.PRICE().get(ii.res))).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.denari).adjustWidth(), incTab(4), MARGIN);

                                total_export += (ii.am() * FACTIONS.player().trade.pricesSell.get(ii.res));
                                total_value += (ii.am() * FACTIONS.PRICE().get(ii.res));
                        }else if ( ii == null &&
                                !Objects.equals(spec, "columns") &&
                                !Objects.equals(spec, "total") &&
                                !Objects.equals(spec, "space") ){ // Then display  what "spec" is
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), spec ).adjustWidth(), incTab(4), MARGIN);

                        }else if ( ii == null && Objects.equals(spec, "columns")){ // New Columnn titles
                                // add(GFORMAT.text(new GText(UI.FONT().S, 0), "Resource per day         Value if exported per day   Average value per day").adjustWidth(), incTab(4), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.titleProduction).adjustWidth(), incTab(4), MARGIN);

                        }else if( ii == null && Objects.equals(spec, "total")){ // Total values
                                // add(GFORMAT.text(new GText(UI.FONT().S, 0), "Total Values:").adjustWidth(), incTab(5), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.totalValues).adjustWidth(), incTab(5), MARGIN);
                                add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) +total_export).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.denari).adjustWidth(), incTab(4), MARGIN);
                                add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) +total_value).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.denari).adjustWidth(), incTab(4), MARGIN);

                        }else if( ii == null && Objects.equals(spec, "space")){ // blank line!
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), " ").adjustWidth(), incTab(5), MARGIN);
                        }
                }
        }

        private abstract class RegRow extends GuiSection {
                protected static final int MARGIN = 4;
                private double tab;

                protected int incTab(double n) {
                        double t = tab;
                        tab += n;
                        return (int) (t * MARGIN * 10);
                }
        }
        public static double production() {
                double tot = 0;
                for (RESOURCE res : RESOURCES.ALL()) {
                        for (RoomProduction.Source rr : SETT.ROOMS().PROD.producers(res)) {
                                if (rr.am() == 0) {continue;}
                                tot += rr.am() * FACTIONS.PRICE().get(res) ;
                        }
                }
                return tot;
        }
        public static double consumption() {
                double tot = 0;
                for (RESOURCE res : RESOURCES.ALL()) {
                        for (RoomProduction.Source rr : SETT.ROOMS().PROD.consumers(res)) {
                                if (rr.am() == 0) {continue;}
                                tot -= rr.am() * FACTIONS.PRICE().get(res) ;
                        }
                }
                return tot;
        }

        public static double net() {
                double tot = 0;
                for (RESOURCE res : RESOURCES.ALL()) {
                        double subtot = 0; // number of resources
                        for (RoomProduction.Source rr : SETT.ROOMS().PROD.producers(res)) {
                                if (rr.am() == 0) {continue;}
                                subtot += rr.am() ;
                        }
                        for (RoomProduction.Source rr : SETT.ROOMS().PROD.consumers(res)) {
                                if (rr.am() == 0) {continue;}
                                subtot -= rr.am() ;
                        }
                        // use sell price if net positive, buy price if net negative.
                        if (subtot>0){tot+=subtot * FACTIONS.player().trade.pricesSell.get(res); }
                        if (subtot<0){tot+=subtot * FACTIONS.player().trade.pricesBuy.get(res); }

                }
                return tot;
        }
}
