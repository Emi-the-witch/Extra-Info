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
import view.ui.goods.tableRow.BalanceRow;
import view.ui.goods.tableRow.BalanceRowHeader;
import view.ui.goods.tableRow.BalanceRowExpandable;
import view.ui.manage.IFullView;
import java.util.HashMap;
import java.util.Map;

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

        private final java.util.Set<CharSequence> expandedCategories = new java.util.HashSet<>();
        private int sortColumn = 0; // 0=Name, 1=Amount, 2=Export, 3=Value
        private boolean sortAscending = true;


        @Override
        public void init() {
                section.clear();
                section.body().moveY1(IFullView.TOP_HEIGHT);
                section.body().moveX1(16);
                section.body().setWidth(WIDTH).setHeight(1);

                section.addDown(0, new GText(UI.FONT().H2, ExtraInfoDic.producers));
                ArrayListGrower<GuiSection> rows = new ArrayListGrower<>();
                GText tableHeader = new GText(UI.FONT().S, "                                                                              ");
                rows.add(new BalanceRowHeader(WIDTH, this));

                // Order by source
                HashMap<CharSequence, ArrayListGrower<RoomProduction.Source>> data = new HashMap<>();
                for (RESOURCE res : RESOURCES.ALL()) {
                        for (RoomProduction.Source ii : SETT.ROOMS().PROD.producers(res)) {
                                if (ii.am() > 0) { // Only capture active producers
                                        data.computeIfAbsent(ii.name(), k -> new ArrayListGrower<>()).add(ii);
                                }
                        }
                }

                java.util.List<Map.Entry<CharSequence, ArrayListGrower<RoomProduction.Source>>> dataSorted =
                        new java.util.ArrayList<>(data.entrySet());

                dataSorted.sort((a, b) -> {
                        double sumA = 0;
                        double sumB = 0;
                        int res;
                        if (sortColumn == 0) {
                                res = a.getKey().toString().compareTo(b.getKey().toString());
                        } else {

                                for (RoomProduction.Source s : a.getValue()) {
                                        sumA += getSortValue(s, sortColumn);
                                }
                                for (RoomProduction.Source s : b.getValue()) {
                                        sumB += getSortValue(s, sortColumn);
                                }

                                res = Double.compare(sumA, sumB);
                        }
                        return sortAscending ? res : -res;
                });

                for (Map.Entry<CharSequence, ArrayListGrower<RoomProduction.Source>> item : dataSorted) {
                        CharSequence category = item.getKey();
                        boolean isExpanded = expandedCategories.contains(category);

                        total_export = 0;
                        total_value = 0;

                        for (RoomProduction.Source ii : item.getValue()) {
                                if (ii.am() != 0) {
                                        long resExport = (long) (ii.am() * FACTIONS.player().trade.pricesSell.get(ii.res));
                                        long resValue = (long) (ii.am() * FACTIONS.PRICE().get(ii.res));
                                        total_export += resExport;
                                        total_value += resValue;
                                }
                        }

                        rows.add(new BalanceRowExpandable(category, total_export, total_value, isExpanded, tableHeader.width(), () -> {
                                if (expandedCategories.contains(category)) expandedCategories.remove(category);
                                else expandedCategories.add(category);
                                init();
                        }));

                        if (isExpanded) {
                                for (RoomProduction.Source ii : item.getValue()) {
                                        long resExport = (long) (ii.am() * FACTIONS.player().trade.pricesSell.get(ii.res));
                                        long resValue = (long) (ii.am() * FACTIONS.PRICE().get(ii.res));
                                        rows.add(new BalanceRow(ii.res, ii.am(), resExport, resValue, tableHeader.width()));
                                }
                        }
                }

                GScrollRows scrollRows = new GScrollRows(rows, HEIGHT-20);
                section.addDown(5, scrollRows.view());
        }

        private double getSortValue(RoomProduction.Source s, int column) {
                switch (column) {
                        case 1: return s.am();
                        case 2: return s.am() * FACTIONS.player().trade.pricesSell.get(s.res);
                        case 3: return s.am() * FACTIONS.PRICE().get(s.res);
                        default: return 0;
                }
        }

        public void handleSort(int column) {
                if (this.sortColumn == column) {
                        this.sortAscending = !this.sortAscending;
                } else {
                        this.sortColumn = column;
                        this.sortAscending = true;
                }
                init();
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
