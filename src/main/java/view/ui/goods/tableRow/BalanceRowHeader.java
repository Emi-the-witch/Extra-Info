package view.ui.goods.tableRow;

import snake2d.util.gui.GuiSection;
import view.ui.goods.UIProduction;

public class BalanceRowHeader extends GuiSection {
    // These MUST match your ProductionRow slots exactly

    private static final int SLOT_NAME = 0;
    private static final int SLOT_EXPORT = 550;
    private static final int SLOT_VALUE = 800;

    public BalanceRowHeader(int width, UIProduction parent) {
        body().setWidth(width).setHeight(40);

        // Column 0: Resource Name
        addHeader(this, "Producer", 0, SLOT_NAME, 150, parent);

        // Column 2: Export Value
        addHeader(this, "Export Value", 2, SLOT_EXPORT, 200, parent);

        // Column 3: Market Value
        addHeader(this, "Market Value", 3, SLOT_VALUE, 200, parent);
    }

    private void addHeader(GuiSection container, String title, int id, int x, int w, UIProduction parent) {
        util.gui.misc.GButt.ButtPanel button = new util.gui.misc.GButt.ButtPanel(title);

        button.setDim(w, 40);
        button.clickActionSet(() -> {
            parent.handleSort(id); // Pass the integer ID here
        });

        container.add(button, x, 0);
    }
}