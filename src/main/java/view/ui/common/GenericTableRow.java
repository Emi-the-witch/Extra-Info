//package view.ui.common;
//
//import snake2d.util.gui.GuiSection;
//import snake2d.util.gui.renderable.RENDEROBJ;
//import util.gui.misc.GText;
//
//public class GenericTableRow extends GuiSection {
//    protected static final int[] COLS = {250, 120, 150, 150};
//    protected static final int ROW_H = 24;
//
//    public GenericTableRow() {
//        this.body().setHeight(ROW_H);
//    }
//
//    /**
//     * Helper to wrap a GText into a centered GuiSection cell.
//     */
//    protected GuiSection makeTextCell(GText txt, int colIndex) {
//        GuiSection cell = new GuiSection();
//        cell.body().setWidth(COLS[colIndex]).setHeight(ROW_H);
//
//        // Use txt.w() instead of txt.body().width()
//        int x = (colIndex == 0) ? 5 : (int)((COLS[colIndex] - txt.w()) / 2);
//
//        // We add the text to the cell section
//        cell.add(txt, x, 0);
//        return cell;
//    }
//
//    protected void renderCell(int col, GuiSection cell) {
//        cell.body().setWidth(COLS[col]).setHeight(ROW_H);
//        this.addRight(0, cell);
//    }
//}
