package view.ui.goods;

import game.faction.FACTIONS;
import init.sprite.UI.UI;

import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.main.RoomBlueprint;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.ArrayListGrower;
import util.dic.ExtraInfoDic;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.ui.manage.IFullView;
import static settlement.main.SETT.ROOMS;
/////////////////////////////////////////////#!# This is a unique file that doesn't overwrite any of Jake's files.
/////#!# Displays all recipes and uses the Buy and Sell Price

public final class UIRecipes extends IFullView {

    // private final static CharSequence ¤¤Name = "Recipes";
    private final static CharSequence ¤¤Name = ExtraInfoDic.recipes;

    public UIRecipes() {
        super(¤¤Name, UI.icons().l.coin);
    }

    @Override
    public void init() {
        section.clear();
        section.body().moveY1(IFullView.TOP_HEIGHT);
        section.body().moveX1(16);

        section.body().setWidth(WIDTH).setHeight(1);

        // Go through all rooms to find the ones that have an "industry." Either using goods or producing goods = "INDUSTRY_HASER"
        // Then go through each industry to collect each recipe.
        ArrayListGrower<Industry> all = new ArrayListGrower<>();
        for (RoomBlueprint h : ROOMS().all()) {
            if (h instanceof INDUSTRY_HASER) {
                INDUSTRY_HASER ii = (INDUSTRY_HASER) h;
                for (Industry ins : ii.industries()) {
                    all.add(ins);
                }
            }
        }
        // Add the rows generated in RRow to "rows"
        ArrayListGrower<RRow> rows = new ArrayListGrower<>();
        for (Industry ind : all) {
            if (ind.outs().isEmpty())
                continue;
            RRow row = new RRow(ind);
            rows.add(row);
        }

        // Call the rows into the UI, including the top title. This is scrollable if it is very long. The height-15 gives the title 15 space.
        GScrollRows scrollRows = new GScrollRows(rows, HEIGHT-15);
        // section.addDown(0, new GText(UI.FONT().H2, "Profit per employee with no labor bonuses, buying inputs and selling outputs"));
        section.addDown(0, new GText(UI.FONT().H2, ExtraInfoDic.titleRecipes));
        section.addDown(0, scrollRows.view());
    }


    // The `private class RRow extends GuiSection` makes variables for the various prices, but some of these were later dropped because of information overload.
    private static class RRow extends GuiSection {
        private final int MARGIN = 4;
            private double tab;

        // Collect all inputs to purchase and all outputs to sell
        RRow(Industry i) {
            super();
            double goods_sell = 0;
            double goods_buy = 0;

            for (IndustryResource oo : i.outs()) {
                goods_sell += oo.rate * FACTIONS.player().trade.pricesSell.get(oo.resource);
            }

            for (IndustryResource oo : i.ins()) {
                goods_buy += oo.rate * FACTIONS.player().trade.pricesBuy.get(oo.resource);
            }
            // Display them for a given industry's recipe
            body().setWidth(WIDTH).setHeight(1);
            add(GFORMAT.f(new GText(UI.FONT().S, 7), goods_sell - goods_buy), incTab(2), MARGIN);
            add(i.blue.icon, incTab(1), 0);

            for (IndustryResource oo : i.outs()) {
                add(oo.resource.icon(), incTab(1), 0);
                add(GFORMAT.f(new GText(UI.FONT().S, 0), oo.rate).adjustWidth(), incTab(1.5), MARGIN);
                add(GFORMAT.text(new GText(UI.FONT().S, 0), "@").adjustWidth(), incTab(0.5), MARGIN);
                add(GFORMAT.i(new GText(UI.FONT().S, 0), FACTIONS.player().trade.pricesSell.get(oo.resource)).adjustWidth(), incTab(1), MARGIN);
            }

            for (IndustryResource oo : i.ins()) {
                add(oo.resource.icon(), incTab(1), 0);
                add(GFORMAT.f(new GText(UI.FONT().S, 0), -oo.rate).adjustWidth(), incTab(1.5), MARGIN);
                add(GFORMAT.text(new GText(UI.FONT().S, 0), "@").adjustWidth(), incTab(0.5), MARGIN);
                add(GFORMAT.i(new GText(UI.FONT().S, 0), FACTIONS.player().trade.pricesBuy.get(oo.resource)).adjustWidth(), incTab(1), MARGIN);
            }
        }

        private int incTab(double n) {
            double t = tab;
            tab += n;
            return (int) (t * MARGIN * 10);
        }

    }
}
