package init.sprite.UI;

import java.io.IOException;

import init.INIT;
import init.INIT.InitResource;
import util.colors.GCOLOR;

public class UI extends InitResource{
	///////////////////////////////////
	//#!# Add icons from CustomIcons
	///////////////////////////////////
	private static UIDecor decor;
	private static UIPanels panels;
	private static UIFonts fonts;
	private static Icons icons;
	private static UIImageMaker image;
	private static CustomIcons c_icons;///#!# Add custom icons

	public UI(INIT init) throws IOException{
		super(init);
		GCOLOR.read();
		fonts = new UIFonts();
		panels = new UIPanels();
		decor = new UIDecor();
		icons = new Icons();
		image = new UIImageMaker();
		c_icons = new CustomIcons();
	}

	public static UIFonts FONT() {
		return fonts;
	}

	public static UIPanels PANEL() {
		return panels;
	}

	public static UIDecor decor() {
		return decor;
	}

	public static Icons icons() {
		return icons;
	}

	public static UIImageMaker image() {
		return image;
	}
	public static CustomIcons c_icons() {///#!# Add custom icons
		return c_icons;
	}
}
