package view.ui.goods.balance;

import game.faction.FACTIONS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import settlement.room.industry.module.RoomProduction;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import view.ui.manage.IFullView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BalanceView extends IFullView {
    protected boolean isNegative;
    protected static double trade_value = 0;
    protected static double total_value = 0;

    protected final java.util.Set<CharSequence> expandedCategories = new java.util.HashSet<>();
    protected int sortColumn = 0;
    protected boolean sortAscending = true;

    public BalanceView(CharSequence name, SPRITE icon) {
        super(name, icon);
    }

    @Override
    public void init() {
        section.clear();
        section.body().moveY1(IFullView.TOP_HEIGHT);
        section.body().moveX1(16);
        section.body().setWidth(WIDTH).setHeight(1);
    }

    protected void renderTable(java.util.List<Map.Entry<CharSequence, ArrayListGrower<RoomProduction.Source>>> tableData, ArrayListGrower<GuiSection> rows) {
        GText tableHeader = new GText(UI.FONT().S, "                                                                              ");

        for (Map.Entry<CharSequence, ArrayListGrower<RoomProduction.Source>> item : tableData) {
            CharSequence category = item.getKey();
            boolean isExpanded = expandedCategories.contains(category);

            trade_value = 0;
            total_value = 0;

            for (RoomProduction.Source ii : item.getValue()) {
                if (ii.am() != 0) {
                    long resExport = (long) (ii.am() * FACTIONS.player().trade.pricesSell.get(ii.res));
                    long resValue = (long) (ii.am() * FACTIONS.PRICE().get(ii.res));
                    trade_value += resExport;
                    total_value += resValue;
                }
            }

            if (isNegative) {
                trade_value = -trade_value;
                total_value = -total_value;
            }

            rows.add(new BalanceRowExpandable(category, trade_value, total_value, isExpanded, tableHeader.width(), () -> {
                if (expandedCategories.contains(category)) expandedCategories.remove(category);
                else expandedCategories.add(category);
                init();
            }));

            if (isExpanded) {
                for (RoomProduction.Source ii : item.getValue()) {
                    long resTradeValue = (long) (ii.am() * FACTIONS.player().trade.pricesSell.get(ii.res));
                    long resValue = (long) (ii.am() * FACTIONS.PRICE().get(ii.res));
                    if (isNegative) {
                        resTradeValue = -resTradeValue;
                        resValue = -resValue;
                    }
                    rows.add(new BalanceRow(ii.res, ii.am(), resTradeValue, resValue, tableHeader.width()));
                }
            }
        }
        GScrollRows scrollRows = new GScrollRows(rows, HEIGHT - 20);
        section.addDown(5, scrollRows.view());
    }

    protected java.util.List<Map.Entry<CharSequence, ArrayListGrower<RoomProduction.Source>>> getData(Function<RESOURCE, Iterable<RoomProduction.Source>> sourcePicker) {
        HashMap<CharSequence, ArrayListGrower<RoomProduction.Source>> data = new HashMap<>();
        for (RESOURCE res : RESOURCES.ALL()) {
            for (RoomProduction.Source ii : sourcePicker.apply(res)) {
                if (ii.am() > 0) {
                    data.computeIfAbsent(ii.name(), k -> new ArrayListGrower<>()).add(ii);
                }
            }
        }
        return new java.util.ArrayList<>(data.entrySet());
    }

    protected void sortData(List<Map.Entry<CharSequence, ArrayListGrower<RoomProduction.Source>>> array) {
        array.sort((a, b) -> {
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
    }

    protected double getSortValue(RoomProduction.Source s, int column) {
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
}
