package view.ui.goods;

import game.faction.FACTIONS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.tilemap.floor.Floors;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import util.dic.ExtraInfoDic;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.ui.manage.IFullView;

import java.util.Objects;

import static game.time.TIME.playedGame;
import static java.lang.Math.round;
import static settlement.main.SETT.*;

public final class      UIMaintenance extends IFullView {

        static double CUR_TIME = 0;
        static double CUR_TIME2 = 0;
        // private static CharSequence ¤¤Name = "Maintenance";
        private static CharSequence ¤¤Name = ExtraInfoDic.maintenance;
        public static double import_costs = 0;
        public double value_costs = 0;
        ResData total = new ResData();
        static double[] sort_totals = new double[255];// hopefully less than 255 building types!
        static boolean sortAscending = true;
        static int sortColumn = 0;

        private static final int NAME_COLUMN_WIDTH = 180;
        private static final int SUM_COLUMN_WIDTH = 120;
        private static final int RESOURCE_COLUMN_WIDTH = 52;
        private static final int RESOURCE_INDEX_OFFSET = 100;

        @Override
        public void init() {

                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // Prep work
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                ResData temp = update();
                if (temp != null ){total = temp;}
                section.clear();
                section.body().moveY1(IFullView.TOP_HEIGHT);
                section.body().moveX1(16);
                section.body().setWidth(WIDTH).setHeight(1);

                // Display the rows using the list of resources
                ArrayListGrower<MaintRow> rows = new ArrayListGrower<>();

                import_costs = 0;
                value_costs = 0;
                 // Sum up the total resource use and update building_totals values
                for (RESOURCE res : RESOURCES.ALL()) {
                        if (SETT.MAINTENANCE().estimateGlobal(res) != 0) {
                                import_costs += SETT.MAINTENANCE().estimateGlobal(res) * FACTIONS.player().trade.pricesBuy.get(res);
                                value_costs += SETT.MAINTENANCE().estimateGlobal(res) * FACTIONS.PRICE().get(res);
                        }
                        continue;
                }

                // Adding info to building_totals additional variables

                for (String key : building_totals.keys()) { //For each key and resource, update the import/value price per building:
                        update2(key);

                }


                // Key of building, import price, value price, #1 resource , #2 resource, #3 resource, #4 resource [Image and # ]
                //add(GFORMAT.text(new GText(UI.FONT().S, 0), keyName).adjustWidth(), incTab(0), MARGIN);



                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // Table 1
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                // Display top line messages
                // section.addDown(0, new GText(UI.FONT().H2, "Overall Maintenance costs"));
                section.addDown(0, new GText(UI.FONT().H2, ExtraInfoDic.overallMaintenance));
                // GText tableHeader = new GText(UI.FONT().S, "Resource per day         Costs if imported per day   Average value per day");
                GText tableHeader = new GText(UI.FONT().S, ExtraInfoDic.titleMaintenance);
                section.addDown(10, tableHeader);

                // Create each row

                for (RESOURCE res : RESOURCES.ALL()) {
                        if (SETT.MAINTENANCE().estimateGlobal(res) != 0) {
                                rows.add(new ResourceRow(res, tableHeader.width(), 0, 0));
                        }

                }
                rows.add(new ResourceRow(null, tableHeader.width() , import_costs, value_costs));


                // Display the rows!
                GScrollRows scrollRows = new GScrollRows(rows, (int) round(HEIGHT * .33));
                section.addDown(5, scrollRows.view());
                
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // Table 2
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                generateMaintenanceTableHeader();
                generateMaintenanceTableContent();
        }

        private void generateMaintenanceTableHeader() {
                section.addDown(0, new GText(UI.FONT().H2, ExtraInfoDic.overallBuildingMaintenance));

                GuiSection headerRow = new GuiSection();

                addHeaderButton(headerRow, ExtraInfoDic.buildingsTitle, 0, NAME_COLUMN_WIDTH);
                addHeaderButton(headerRow, ExtraInfoDic.importTitle, 1, SUM_COLUMN_WIDTH);
                addHeaderButton(headerRow, ExtraInfoDic.valueTitle, 2, SUM_COLUMN_WIDTH);

                for (RESOURCE res : RESOURCES.ALL()) {
                        if (SETT.MAINTENANCE().estimateGlobal(res) == 0) continue;
                        addHeaderButton(headerRow, res, RESOURCE_INDEX_OFFSET + res.index(), RESOURCE_COLUMN_WIDTH);
                }
                section.addDown(24, headerRow);
                section.addDown(8, new GuiSection());
        }

        private void generateMaintenanceTableContent() {
                ArrayListGrower<MaintRow> BLDGrows = new ArrayListGrower<>();
                for (String key : getSortedKeys()) {
                        BLDGrows.add(new BuildingMaint(key));
                }
                BLDGrows.add(new BuildingMaint(null));
                BLDGrows.add(new BuildingMaint(null));
                GScrollRows scrollRows = new GScrollRows(BLDGrows, (int) round(HEIGHT * .50) );
                section.addDown(5, scrollRows.view());
        }

        private void addHeaderButton(GuiSection container, Object content, int sortMode, int width) {
                // Determine if we are passing an Icon or Text
                util.gui.misc.GButt.ButtPanel button;
                if (content instanceof RESOURCE) {
                        button = new util.gui.misc.GButt.ButtPanel(((RESOURCE)content).icon());
                } else {
                        button = new util.gui.misc.GButt.ButtPanel((CharSequence)content);
                }

                container.addRight(0, button.setDim(width, 40).clickActionSet(() -> {
                        if (sortColumn == sortMode) sortAscending = !sortAscending;
                        else {
                                sortColumn = sortMode;
                                sortAscending = (sortMode < RESOURCE_INDEX_OFFSET);
                        }
                        init();
                }));
        }

        private java.util.List<String> getSortedKeys() {
                java.util.List<String> keys = new java.util.ArrayList<>();
                for (String k : building_totals.keys()) {
                        if (!building_totals.get(k).empty) keys.add(k);
                }

                keys.sort((k1, k2) -> {
                        ResData d1 = building_totals.get(k1);
                        ResData d2 = building_totals.get(k2);
                        int res = 0;
                        switch (sortColumn) {
                                case 0:
                                        String n1 = d1.keyName != null ? d1.keyName : k1;
                                        String n2 = d2.keyName != null ? d2.keyName : k2;
                                        res = n1.compareToIgnoreCase(n2);
                                        break;
                                case 1:
                                        res = Double.compare(d1.import_price, d2.import_price);
                                        break;
                                case 2:
                                        res = Double.compare(d1.value_price, d2.value_price);
                                        break;
                                default:
                                        if (sortColumn >= RESOURCE_INDEX_OFFSET) {
                                                int resIndex = sortColumn - RESOURCE_INDEX_OFFSET;
                                                res = Double.compare(d1.amounts[resIndex], d2.amounts[resIndex]);
                                        }
                                        break;
                        }
                        return sortAscending ? res : -res;
                });
                return keys;
        }

        private static class ResourceRow extends MaintRow {
                // Create the row using the resource:
                ResourceRow(RESOURCE res, int width, double import_costs, double value_costs) {
                        //////////////////////////////////////////////////////////////////////
                        // Table 1 Data
                        //////////////////////////////////////////////////////////////////////
                         if (res != null){
                                double amount_of_res = SETT.MAINTENANCE().estimateGlobal(res);
//                                body().setWidth(width).setHeight(1);
                                // Display resource.icon()
                                add(GFORMAT.f(new GText(UI.FONT().S, 0), amount_of_res).adjustWidth(), incTab(2), MARGIN);
                                // Amount of resource used:
                                add(res.icon(), incTab(3), 0);
                                // Import costs for that resource:
                                add(GFORMAT.i(new GText(UI.FONT().S, 0), (long) (amount_of_res * FACTIONS.player().trade.pricesBuy.get(res))).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.denari).adjustWidth(), incTab(4), MARGIN);
                                // Value of those resources:
                                add(GFORMAT.i(new GText(UI.FONT().S, 0), (long) (amount_of_res * FACTIONS.PRICE().get(res))).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.denari).adjustWidth(), incTab(4), MARGIN);
                        }
                        //////////////////////////////////////////////////////////////////////
                        // Table 1 Total
                        //////////////////////////////////////////////////////////////////////
                        else{
                                // add(GFORMAT.text(new GText(UI.FONT().S, 0), "Total Costs:").adjustWidth(), incTab(5), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.totalCosts).adjustWidth(), incTab(5), MARGIN);
                                add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) -import_costs).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.denari).adjustWidth(), incTab(4), MARGIN);
                                add(GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) -value_costs).adjustWidth(), incTab(2), MARGIN);
                                add(GFORMAT.text(new GText(UI.FONT().S, 0), ExtraInfoDic.denari).adjustWidth(), incTab(4), MARGIN);
                        }
                }
        }
        private static class BuildingMaint extends MaintRow {
                // Tabla 2
                BuildingMaint(String key) {
                        body().setHeight(24);
                        // Render resource row
                        if (key != null) {
                                ResData data = building_totals.get(key);
                                String name = data.keyName != null ? data.keyName : key;
                                renderRow(name, data.import_price, data.value_price, data.amounts);
                        }
                        // Render totals row
                        else {
                                double total_import = 0;
                                double total_value = 0;
                                double[] total_amount = new double[RESOURCES.ALL().size()];

                                for (String k : building_totals.keys()) {
                                        ResData d = building_totals.get(k);
                                        total_import += d.import_price;
                                        total_value += d.value_price;
                                        for (RESOURCE res : RESOURCES.ALL()) {
                                                total_amount[res.index()] += d.amounts[res.index()];
                                        }
                                }

                                renderRow(ExtraInfoDic.total, total_import, total_value, total_amount);
                        }
                }

                private void renderRow(CharSequence name, double importPrice, double valuePrice, double[] resourceAmounts) {
                        renderTableCell(NAME_COLUMN_WIDTH, GFORMAT.text(new GText(UI.FONT().S, 0), name).adjustWidth(), false);
                        renderTableCell(SUM_COLUMN_WIDTH, GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) importPrice).adjustWidth(), true);
                        renderTableCell(SUM_COLUMN_WIDTH, GFORMAT.iIncr(new GText(UI.FONT().S, 0), (long) valuePrice).adjustWidth(), true);
                        for (RESOURCE res : RESOURCES.ALL()) {
                                if (SETT.MAINTENANCE().estimateGlobal(res) == 0) continue;
                                double amount = Math.round(resourceAmounts[res.index()] * 10) / 10.0;
                                renderTableCell(RESOURCE_COLUMN_WIDTH, GFORMAT.f(new GText(UI.FONT().S, 0), amount, 1).adjustWidth(), true);
                        }
                }

                private void renderTableCell(int width, snake2d.util.sprite.SPRITE sprite, boolean alignCenter) {
                        GuiSection cell = new GuiSection();
                        cell.body().setWidth(width).setHeight(24);
                        // If cell content is too wide it will jump in the next line
                        if (sprite instanceof GText && sprite.width() > width) {
                                ((GText) sprite).setMaxWidth(width);
                        }

                        // Center values, presumably do it with numeric values so they are aligned with resource icons
                        if (alignCenter) {
                                int xPos = (int) ((width - sprite.width()) / 2);
                                cell.add(sprite, xPos, 0);
                        } else {
                                cell.add(sprite, 0, 0);
                        }

                        this.addRight(0, cell);
                }
        }
// ROOMS().map.get(85,71).roomI  ==> 155
static KeyMap<ResData> building_totals = new KeyMap<ResData>();
        //Constructor
        public UIMaintenance() {
                super(¤¤Name, UI.c_icons().l.maint);

                for (RoomBlueprint h : ROOMS().all()){ // For each type of room blueprint
                        building_totals.putReplace(h.key(), new ResData());
                }
        }
        //QData without "changed" boolean and not private...
        public class ResData {

                public double[] amounts = new double[RESOURCES.ALL().size()];
                double import_price=0;
                double value_price=0;
                String keyName;
                boolean empty;
        }
        //Update the maintenance values for each blueprint key:
        public ResData update() {
                if (CUR_TIME == playedGame()){return(null);}
                CUR_TIME = playedGame();
                ResData total = new ResData();
                for (RoomBlueprint h : ROOMS().all()){ // For each type of room blueprint
                        building_totals.putReplace(h.key(), new ResData()); //clear it
                }
                building_totals.putReplace("Road", new ResData()); // Add roads to list of keys

                for (int y = 0; y < THEIGHT; y++) {
                        for (int x = 0; x < TWIDTH; x++) {
                                if (ROOMS().map.get(x,y) == null){ // if not a room, it might be a road!
                                        if (FLOOR().getter.is(x, y) && !PATH().solidity.is(x, y)){
                                                Floors.Floor f = FLOOR().getter.get(x, y);
                                                ResData td = building_totals.get("Road");
                                                // resource amount for roads
                                                double am = 0.25*f.resAmount*SETT.MAINTENANCE().resRate*(1.0-f.durability)*SETT.MAINTENANCE().speed();
                                                if (am > 0) { // in f.resource position
                                                        td.amounts[f.resource.index()] += am;
                                                        total.amounts[f.resource.index()] += am;
                                                }

                                        }
                                        continue;
                                }
                                Room room = ROOMS().map.get(x, y);
                                if (!building_totals.containsKey(room.blueprint().key())){continue;}
                                ResData td = building_totals.get(room.blueprint().key());
                                for (int r = 1; r < 5; r++) {
                                        double am = resRate(x, y, r); // Find the quantity of the resource used
                                        if (am > 0) {
                                                td.amounts[res(x, y, r).index()] += am;  // Find the position of the resource's position, add am
                                                total.amounts[res(x, y, r).index()] += am; // After adding to building, add to total.
                                        }
                                }
                        }
                }
                return(total);
        }
        // Update ResData's secondary variables.
        public static void update2(String key){
                building_totals.get(key).import_price = 0;
                building_totals.get(key).value_price = 0;
                for (RESOURCE r : RESOURCES.ALL()){
                        building_totals.get(key).import_price -= building_totals.get(key).amounts[r.index()] * FACTIONS.player().trade.pricesBuy.get(r) ;
                        building_totals.get(key).value_price  -= building_totals.get(key).amounts[r.index()] * FACTIONS.PRICE().get(r);
                }
                // Give the each KeyMap the nicer name of the building, if you can...
                for (RoomBlueprint element :  SETT.ROOMS().all() ){
                        if (element instanceof RoomBlueprintImp) {
                                RoomBlueprintImp room = (RoomBlueprintImp) element;
                                if (Objects.equals(key, room.key)){
                                        building_totals.get(key).keyName = (String) room.info.name;
                                }
                        }
                }
                building_totals.get(key).empty = sum_d(building_totals.get(key).amounts)==0;
                // sort_totals[index] = building_totals.get(key).import_price;
        }
        //From MRoom but that's private
        public double resRate(int tx, int ty, int ri) {
                if (ri == 0 )
                        return 0;
                ri--;

                Room room = ROOMS().map.get(tx, ty);
                if (room != null) {
                        ROOM_DEGRADER deg = room.degrader(tx, ty);
                        if (deg != null) {
                                if (ri >=  deg.resSize())
                                        return 0;
                                return ROOM_DEGRADER.rateResource(SETT.MAINTENANCE().speed(), deg.base(), room.isolation(tx, ty), deg.resAmount(ri))/room.area(tx, ty);
                        }
                }
                return 0;
        }
        //From MRoom but that's private
        public RESOURCE res(int tx, int ty, int ri) {
                if (ri == 0)
                        return null;
                ri-= 1;
                Room room = ROOMS().map.get(tx, ty);
                if (room != null) {
                        if (room.constructor() != null && room.constructor().resources() > 0)
                                return room.constructor().resource(ri%room.constructor().resources());

                }
                return null;
        }
        private abstract static class MaintRow extends GuiSection {
                protected static final int MARGIN = 4;
                private double tab;

                protected int incTab(double n) {
                        double t = tab;
                        tab += n;
                        return (int) (t * MARGIN * 10);
                }
        }
        public static double sum_d(double[] numbers){
                double sum = 0;
                for (double number : numbers){
                        sum += number;
                }
                return sum;
        }
}
