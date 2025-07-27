package view.ui.goods;

import init.paths.PATHS;
import init.sprite.UI.UI;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.gui.misc.*;
import view.ui.manage.IFullView;
import java.util.*;
import java.util.Map.Entry;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

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


        // Display top line messages
        section.addDown(0, new GText(UI.FONT().H2, "Saves settings automatically, but mods only load values when you load the save game!"));

        GText tableHeader = new GText(UI.FONT().S, "                                                                              ");
        section.addDown(10, tableHeader);

        // Display each Property File entry value
        for (Entry entry:PropFile.entrySet()) {


            GText keytext = new GText(UI.FONT().M, (String) entry.getKey());
            section.addDown(10, keytext);


            StringInputSprite t = new StringInputSprite(24, UI.FONT().M) {

//                @Override
//                public Str text() {
//                    return new Str(24).add((String) entry.getValue());
//                }

                @Override
                protected void change() {
                    String newValue = this.text().toString();
                    PropFile.setProperty((String) entry.getKey(), newValue);
                    try{PropFile.store(new FileWriter(String.valueOf(PropPath)), "Extra Info Changed stuff most recently.");}catch(Exception f){return;}
                    return;
                }

            };
            t.set(  (CharSequence)  entry.getValue());
            GInput in = new GInput(t);
            section.addDown(10, in);
        }
    }
}