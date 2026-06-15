package view.ui.goods.balance;

import init.sprite.UI.UI;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GText;
import util.info.GFORMAT;

public class BalanceRowExpandable extends GuiSection {
    // These MUST match your ProductionRow slots exactly
    private static final int SLOT_TOGGLE = 0;
    private static final int SLOT_LABEL = 80;
    private static final int SLOT_EXPORT = 550;
    private static final int SLOT_VALUE = 800;

    public BalanceRowExpandable(CharSequence category, double totalTradeValue, double totalValue, boolean isExpanded, int width, Runnable toggleAction) {
        body().setWidth(width).setHeight(24);

        String buttonLabel = (isExpanded ? "[-] " : "[+] ");

        util.gui.misc.GButt.ButtPanel toggleBtn = new util.gui.misc.GButt.ButtPanel(buttonLabel) {
            @Override
            public boolean click() {
                toggleAction.run();
                return true;
            }
        };
        toggleBtn.setDim(48, 40);

        add(toggleBtn, SLOT_TOGGLE, 0);

        GText label = new GText(UI.FONT().S, category);
        add(label.adjustWidth(), SLOT_LABEL, 12);

        GText expT = new GText(UI.FONT().S, 10);
        GFORMAT.iIncr(expT, (long) totalTradeValue);
        add(expT.adjustWidth(), SLOT_EXPORT, 12);

        GText valT = new GText(UI.FONT().S, 10);
        GFORMAT.iIncr(valT, (long) totalValue);
        add(valT.adjustWidth(), SLOT_VALUE, 12);
    }
}