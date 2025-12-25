package view.ui.goods;

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
/////#!# Displays all the PROD.consumers

public final class UIExpenses extends BalanceView {
    private static CharSequence ¤¤Name = ExtraInfoDic.expenses;
    public UIExpenses() {
            super(¤¤Name, UI.c_icons().l.minus);
    }

    @Override
    public void init() {
        isNegative = true;
        super.init();
        ArrayListGrower<GuiSection> rows = new ArrayListGrower<>();
        rows.add(new BalanceRowHeader(WIDTH, this, "Consumer","Import"));

        List<Map.Entry<CharSequence, ArrayListGrower<RoomProduction.Source>>> dataSorted = getData((res -> SETT.ROOMS().PROD.consumers(res)));
        super.sortData((dataSorted));
        super.renderTable(dataSorted, rows);
    }
}
