package init.sprite.UI;

import util.colors.GCOLOR;

import java.io.IOException;

public class UI {
	///////////////////////////////////
	//#!# Add icons from CustomIcons
	///////////////////////////////////
	private static UIDecor decor;
	private static UIPanels panels;
	private static UIFonts fonts;
	private static Icons icons;
	private static UIImageMaker image;
	private static CustomIcons c_icons;//#!# Add custom icons

	public static void init() throws IOException {
		GCOLOR.read();
		fonts = new UIFonts();
		panels = new UIPanels();
		decor = new UIDecor();
		icons = new Icons();
		image = new UIImageMaker();
		c_icons = new CustomIcons();  //#!# Add custom icons
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

	public static CustomIcons c_icons() {//#!# Add custom icons
		return c_icons;
	}

}
