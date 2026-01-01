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
import view.ui.goods.balance.BalanceRowHeader;
import view.ui.goods.balance.BalanceView;
import java.util.List;
import java.util.Map;
/////////////////////////////////////////////#!# This is a unique file that doesn't overwrite any of Jake's files.
/////#!# Displays all the PROD.producers

public final class UIProduction extends BalanceView {
        private static CharSequence ¤¤Name = ExtraInfoDic.production;
        public UIProduction() {
                super(¤¤Name, UI.c_icons().l.plus);
        }

        @Override
        public void init() {
                super.init();
                ArrayListGrower<GuiSection> rows = new ArrayListGrower<>();
                rows.add(new BalanceRowHeader(WIDTH, this, "Producer", "Import"));

                List<Map.Entry<CharSequence, ArrayListGrower<RoomProduction.Source>>> dataSorted = getData((res -> SETT.ROOMS().PROD.producers(res)));
                super.sortData((dataSorted));
                super.renderTable(dataSorted, rows);
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
