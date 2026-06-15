This mod is a collection of smaller mods made over time to provide more information to make decisions.

* Creating UI tabs uses a modification to IManager, here are all changes for that file:
* All 3 ui.goods.X java files are created from scratch and wouldn't have any conflict with other mods, other than needing the IManager modifications

view.ui.manage.IManager.java


	1) Import the tabs code
	import view.ui.goods.UIMaintenance;
	import view.ui.goods.UIRecipes;
	import view.ui.goods.UIValues;
	
	2) The tabs being added
	private final UIRecipes recipes;
	private final UIValues values;
	private final UIMaintenance all_maintenance;

	3) The tabs being added
	recipes = new UIRecipes();
	all.add(recipes);

	values = new UIValues();
	all.add(values);

	all_maintenance = new UIMaintenance();
	all.add(all_maintenance);
	
	4) The icons being added to the main UI screen:
	bAdd(s, i++, recipes, UI.icons().s.storage, null);
	bAdd(s, i++, values, UI.icons().s.storage, null);
	bAdd(s, i++, all_maintenance, UI.icons().s.storage, null);
 
	$TODO$ Change icons from "storage" to something more useful
	
	
* The "Recipes" tab that helps determine if an industry is currently profitable to buy inputs (if any) and sell outputs to your current trade partners
* This is to be used to determine if you should go into an industry, but better information is available if you're already in the industry.

view.ui.goods.UIRecipes.java

	1) Cycle through industries to find all recipes 
	ArrayListGrower<Industry> all = new ArrayListGrower<>();
		for (RoomBlueprint h : ROOMS().all()) {
		if (h instanceof INDUSTRY_HASER) {
			INDUSTRY_HASER ii = (INDUSTRY_HASER) h;
			for (Industry ins : ii.industries()) {
				all.add(ins);
			}
		}
	}
	
	2) Add the rows generated in RRow to "rows"
	
	ArrayListGrower<RRow> rows = new ArrayListGrower<>();
	for (Industry ind : all) {
		if (ind.outs().isEmpty())
			continue;
		RRow row = new RRow(ind);
		rows.add(row);
	}

	3) Call the rows into the UI, including the top title. This is scrollable if it is very long. The height-15 gives the title 15 space.
	
	GScrollRows scrollRows = new GScrollRows(rows, HEIGHT-15);
	section.addDown(0, new GText(UI.FONT().H2, "Profit per employee with no labor bonuses, buying inputs and selling outputs"));
	section.addDown(0, scrollRows.view());
	
	4) Collect all inputs to purchase and all outputs to sell
	
	// The `private class RRow extends GuiSection` makes variables for the various prices, but some of these were later dropped because of information overload. 
	double goods_sell = 0;
	double goods_buy = 0;
	for (Industry.IndustryResource oo : ind.outs()) {
		goods_sell += oo.rate * FACTIONS.player().trade.pricesSell.get(oo.resource);
	}
	for (Industry.IndustryResource oo : ind.ins()) {
		goods_buy += oo.rate * FACTIONS.player().trade.pricesBuy.get(oo.resource);
	}
	
	5) Display them for a given industry's recipe
	
	body().setWidth(WIDTH).setHeight(1);
	add(GFORMAT.f(new GText(UI.FONT().S, 7), goods_sell - goods_buy), incTab(2), MARGIN);
	add(ind.blue.icon, incTab(1), 0);


	for (Industry.IndustryResource oo : ind.outs()) {
		add(oo.resource.icon(), incTab(1), 0);
		add(GFORMAT.f(new GText(UI.FONT().S, 0), oo.rate).adjustWidth(), incTab(1.5), MARGIN);
		add(GFORMAT.text(new GText(UI.FONT().S, 0), "@").adjustWidth(), incTab(0.5), MARGIN);
		add(GFORMAT.i(new GText(UI.FONT().S, 0), FACTIONS.player().trade.pricesSell.get(oo.resource)).adjustWidth(), incTab(1), MARGIN);
	}

	for (Industry.IndustryResource oo : ind.ins()) {
		add(oo.resource.icon(), incTab(1), 0);
		add(GFORMAT.f(new GText(UI.FONT().S, 0), -oo.rate).adjustWidth(), incTab(1.5), MARGIN);
		add(GFORMAT.text(new GText(UI.FONT().S, 0), "@").adjustWidth(), incTab(0.5), MARGIN);
		add(GFORMAT.i(new GText(UI.FONT().S, 0), FACTIONS.player().trade.pricesBuy.get(oo.resource)).adjustWidth(), incTab(1), MARGIN);
	}
 
	$TODO$   Can add  Boostable t = ind.bonus(); to make a multiplier of the labor speed, but since this is to inform someone to *join* an industry it isn't as important
	
* The "Values" UI panel uses the same code as "Recipes" but instead of using trade prices, it uses "Average Value" the 'middle' of buy or sell prices, averaged from the world. 

view.ui.goods.UIValues.java	

	1) Display all recipes, input prices and output prices 
	
	double goods_sell = 0;
	double goods_buy = 0;
	for (Industry.IndustryResource oo : ind.outs()) {
		goods_sell += oo.rate * FACTIONS.PRICE().get(oo.resource);
	}
	for (Industry.IndustryResource oo : ind.ins()) {
		goods_buy += oo.rate * FACTIONS.PRICE().get(oo.resource);
	}
	
* The "Maintenance" UI panel uses the import price and average value prices to display the cost of maintenance, one resource at a time and the total

view.ui.goods.UIMaintenance.java	

	1) Display the rows using the list of resources
	
	ArrayListGrower<EmiRow> rows = new ArrayListGrower<>();
	double import_costs = 0;
	double value_costs = 0;
	// Sum up the total first
	for (RESOURCE res : RESOURCES.ALL()) {
			import_costs += SETT.MAINTENANCE().estimateGlobal(res) * FACTIONS.PRICE().get(res);
			value_costs += SETT.MAINTENANCE().estimateGlobal(res) * FACTIONS.player().trade.pricesBuy.get(res);
	}
	
	
* Knowledge tree information added 

ui.tech.Node.java

	1) Add the tech's cost analysis (# of lab employees for a tech)
 		void knowledge_costs() function
 	2) Add the tech's benefit analysis (# of workers benefit from the tech)
  		void knowledge_benefits() function
  	3) Change the Knowlede UI to show the Cost/Benefit analysis upon hovering over it
		private COLOR col(*) function
		
* Knowledge tree information added to the top panel

game.faction.player.PTech.java

	1) Add some of the information generated in Node's cost analysis to the Knowedge total hover GUI
		
		
