package view.ui.goods;


import init.paths.PATHS;
import init.sprite.UI.UI;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.text.StringInputSprite;
import util.gui.misc.*;
import view.ui.manage.IFullView;
import java.util.*;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import util.gui.table.GScrollRows;


public final class UIModSettings extends IFullView {
    private static CharSequence ¤¤Name = "Mod Settings";

    /// Create the list of property elements in the property file
    // Create PropPath
    Path PropPath = PATHS.local().SETTINGS.get().resolve("EmiTheWitch.txt");
    // String PropPath = rootPath + "EmiTheWitch.txt";

    // Create this mod's property variable that holds the whole file
    public Properties PropFile = new Properties();

    // Try to load the file or create it if it doesn't exist
    {
        // Try to load
        try {
            PropFile.load(new FileInputStream(String.valueOf(PropPath)));
            // Or create if you can't
        } catch (Exception e) {
            // Try to create
            try {
                PropFile.store(new FileWriter(String.valueOf(PropPath)), "store to properties file");
                // Or give up and crash
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public UIModSettings() {
        super(¤¤Name, UI.icons().l.refiner);
    }

    @Override
    public void init() {
        section.clear();
        section.body().moveY1(IFullView.TOP_HEIGHT);
        section.body().moveX1(16);
        section.body().setWidth(WIDTH).setHeight(1);

        ArrayList<String> keys = new ArrayList<String>();
        for (Object key : PropFile.keySet()) {
            keys.add((String) key);
        }
        Collections.sort(keys);

        ArrayListGrower<RENDEROBJ> rows = new ArrayListGrower<>();
        for (String entry:keys) {
            GuiSection row = new GuiSection();




                            GText keytext = new GText(UI.FONT().M, entry);
                            row.addDown(10, keytext);


                            StringInputSprite t = new StringInputSprite(24, UI.FONT().M) {

                                @Override
                                protected void change() {
                                    String newValue = this.text().toString();
                                    PropFile.setProperty(entry, newValue);
                                    try{PropFile.store(new FileWriter(String.valueOf(PropPath)), "Extra Info Changed stuff most recently.");}catch(Exception f){return;}
                                    return;
                                }

                            };
                            t.set(  PropFile.getProperty(entry));
                            GInput in = new GInput(t);
                            row.addDown(10, in);
            rows.add(row);
        }

        GScrollRows scrollRows = new GScrollRows(rows, HEIGHT-40);
        section.addDown(0, new GText(UI.FONT().H2, "Saves instantly. Reload game for mods to update."));
        section.addDown(0, scrollRows.view());





    }
}