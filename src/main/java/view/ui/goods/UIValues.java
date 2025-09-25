package view.ui.goods;

import game.faction.FACTIONS;
import init.sprite.UI.UI;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.Industry;
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
/////#!# Displays all recipes and uses the Faction average price

public final class UIValues extends IFullView {

    // private static final CharSequence ¤¤Name = "Values";
    private static final CharSequence ¤¤Name = ExtraInfoDic.values;

    public UIValues() {
        super(¤¤Name, UI.icons().l.coin);
    }

    @Override
    public void init() {
        section.clear();
        section.body().moveY1(IFullView.TOP_HEIGHT);
        section.body().moveX1(16);

        section.body().setWidth(WIDTH).setHeight(1);

        ArrayListGrower<Industry> all = new ArrayListGrower<>();

        for (RoomBlueprint h : ROOMS().all()) {
            if (h instanceof INDUSTRY_HASER) {
                INDUSTRY_HASER ii = (INDUSTRY_HASER) h;
                for (Industry ins : ii.industries()) {
                    all.add(ins);
                }
            }
        }

        ArrayListGrower<RRow> rows = new ArrayListGrower<>();
        for (Industry ind : all) {
            if (ind.outs().isEmpty())
                continue;
            RRow row = new RRow(ind);
            rows.add(row);
        }

        GScrollRows scrollRows = new GScrollRows(rows, HEIGHT-15);
        // section.addDown(0, new GText(UI.FONT().H2, "Profit per employee with no labor bonuses, using world average value for resources"));
        section.addDown(0, new GText(UI.FONT().H2, ExtraInfoDic.titleValues));
        section.addDown(0, scrollRows.view());
    }



    private static class RRow extends GuiSection {
        private final int MARGIN = 4;
            private double tab;

        RRow(Industry i) {
            super();
            double goods_sell = 0;
            double goods_buy = 0;

            for (Industry.IndustryResource oo : i.outs()) {
                goods_sell += oo.rate * FACTIONS.PRICE().get(oo.resource);
            }

            for (Industry.IndustryResource oo : i.ins()) {
                goods_buy += oo.rate * FACTIONS.PRICE().get(oo.resource);
            }

            body().setWidth(WIDTH).setHeight(1);
            add(GFORMAT.f(new GText(UI.FONT().S, 7), goods_sell - goods_buy), incTab(2), MARGIN);
            add(i.blue.icon, incTab(1), 0);

            for (Industry.IndustryResource oo : i.outs()) {
                add(oo.resource.icon(), incTab(1), 0);
                add(GFORMAT.f(new GText(UI.FONT().S, 0), oo.rate).adjustWidth(), incTab(1.5), MARGIN);
                add(GFORMAT.text(new GText(UI.FONT().S, 0), "@").adjustWidth(), incTab(0.5), MARGIN);
                add(GFORMAT.i(new GText(UI.FONT().S, 0), FACTIONS.PRICE().get(oo.resource)).adjustWidth(), incTab(1), MARGIN);
            }

            for (Industry.IndustryResource oo : i.ins()) {
                add(oo.resource.icon(), incTab(1), 0);
                add(GFORMAT.f(new GText(UI.FONT().S, 0), -oo.rate).adjustWidth(), incTab(1.5), MARGIN);
                add(GFORMAT.text(new GText(UI.FONT().S, 0), "@").adjustWidth(), incTab(0.5), MARGIN);
                add(GFORMAT.i(new GText(UI.FONT().S, 0), FACTIONS.PRICE().get(oo.resource)).adjustWidth(), incTab(1), MARGIN);
            }
        }

        private int incTab(double n) {
            double t = tab;
            tab += n;
            return (int) (t * MARGIN * 10);
        }

    }
}
