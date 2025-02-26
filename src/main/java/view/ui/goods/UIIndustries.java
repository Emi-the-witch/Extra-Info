package view.ui.goods;

import game.faction.FACTIONS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.industry.module.RoomProduction;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.ArrayListGrower;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.ui.manage.IFullView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
/////////////////////////////////////////////#!# This is a unique file that doesn't overwrite any of Jake's files.
/////#!# Displays all the PROD.consumers

public final class UIIndustries extends IFullView {

        private static CharSequence ¤¤Name = "Industries";
        public UIIndustries() {
                super(¤¤Name, UI.icons().l.coin);
        }


        @Override
        public void init() {
                section.clear();
                section.body().moveY1(IFullView.TOP_HEIGHT);
                section.body().moveX1(16);
                section.body().setWidth(WIDTH).setHeight(1);


                // Display top line messages
                section.addDown(0, new GText(UI.FONT().H2, "Industry Profitability"));

                GText tableHeader = new GText(UI.FONT().S, "                                                                              ");
                section.addDown(10, tableHeader);



                //////
                // For each blueprint, get the employment and profitability of each room
                // Get the employee-weighted average of the profit of each industry for each type of profit
                //
                //////
                // Order by source
                HashMap<CharSequence, ArrayListGrower<RoomProduction.Source>> data = new HashMap<>();
                for (RESOURCE res : RESOURCES.ALL()) {
                        for (RoomProduction.Source ii : SETT.ROOMS().PROD.consumers(res)) {
                                if (!data.containsKey(ii.name())) {
                                        data.put(ii.name(), new ArrayListGrower<>());
                                }
                                data.get(ii.name()).add(ii);
                        }
                }
        }

}
