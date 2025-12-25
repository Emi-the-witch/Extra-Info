package view.ui.goods.balance;

import init.resources.RESOURCE;
import init.sprite.UI.UI;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GText;
import util.info.GFORMAT;


public class BalanceRow extends GuiSection {
    private static final int SLOT_NAME = 150;
    private static final int SLOT_AMOUNT = 300;
    private static final int SLOT_EXPORT = 550;
    private static final int SLOT_VALUE = 800;

    public BalanceRow(RESOURCE res, double amount, long expVal, long mktVal, int width) {
        body().setWidth(width).setHeight(24);

        add(res.icon(), SLOT_NAME, 0);
        add(new GText(UI.FONT().S, res.name).adjustWidth(), SLOT_NAME + 24, 4);

        GText amTxt = new GText(UI.FONT().S, 10);
        GFORMAT.f(amTxt, amount);
        add(amTxt.adjustWidth(), SLOT_AMOUNT, 4);

        GText expTxt = new GText(UI.FONT().S, 10);
        GFORMAT.iIncr(expTxt, expVal);
        add(expTxt.adjustWidth(), SLOT_EXPORT, 4);

        GText valTxt = new GText(UI.FONT().S, 10);
        GFORMAT.iIncr(valTxt, mktVal);
        add(valTxt.adjustWidth(), SLOT_VALUE, 4);
    }
}
