package util.dic;

import util.text.Dic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
/////////////////////////////////////////////#!# This is a unique file that doesn't overwrite any of Jake's files.

public class ExtraInfoDic {

    /**
     * replace all 'denari's with built-in localized string
     * if a plural form is required, change here to Dic.¤¤Currs
     */
    public static CharSequence denari = Dic.¤¤Curr;
    /**
     * original: view.ui.goods.UIExpenses, line 130
     * original: view.ui.goods.UIMaintenance, line 136
     */
    public static CharSequence totalCosts;


    /**
     * original: view.ui.economy.RRow, line 207
     */
    public static CharSequence year3;
    /**
     * original: view.ui.economy.RRow, line 212
     */
    public static CharSequence year1;
    /**
     * original: view.ui.economy.RRow, line 217
     */
    public static CharSequence yesterday;


    /**
     * original: view.ui.economy.UITreasury, line 194
     */
    public static CharSequence treasuryTop;
    /**
     * original: view.ui.economy.UITreasury, line 197
     */
    public static CharSequence treasuryMsg1;
    /**
     * original: view.ui.economy.UITreasury, line 198
     */
    public static CharSequence treasuryMsg2;
    /**
     * original: view.ui.economy.UITreasury, line 199
     */
    public static CharSequence treasuryMsg3;
    /**
     * original: view.ui.economy.UITreasury, line 200
     */
    public static CharSequence treasuryMsg4;
    /**
     * original: view.ui.economy.UITreasury, line 216
     */
    public static CharSequence treasuryTip;
    /**
     * original: view.ui.economy.UITreasury, line 221
     */
    public static CharSequence treasuryInfo;


    /**
     * original: view.ui.goods.UIExpenses.¤¤Name
     */
    public static CharSequence expenses;
    /**
     * original: view.ui.goods.UIExpenses, line 41
     */
    public static CharSequence consumers;
    /**
     * original: view.ui.goods.UIExpenses, line 127
     */
    public static CharSequence titleExpenses;


    /**
     * original: view.ui.goods.UIIndustries.¤¤Name
     */
    public static CharSequence industries;
    /**
    /**
    * original: view.ui.goods.UIIndustries.¤¤Name
    */
    public static CharSequence logistics;
    /**
     * original: view.ui.goods.UIIndustries, line 41
     */
    public static CharSequence industryProfitability;


    /**
     * original: view.ui.goods.UIMaintenance.¤¤Name
     */
    public static CharSequence maintenance;
    /**
     * original: view.ui.goods.UIMaintenance, line 85
     */
    public static CharSequence overallMaintenance;
    /**
     * original: view.ui.goods.UIMaintenance, line 86
     */
    public static CharSequence titleMaintenance;
    /**
     * original: view.ui.goods.UIMaintenance, line 109
     */
    public static CharSequence overallBuildingMaintenance;
    /**
     * original: view.ui.goods.UIMaintenance, line 110
     */
    public static CharSequence buildingsTitle;
    /**
     * original: view.ui.goods.UIMaintenance, line 110
     */
    public static CharSequence importTitle;
    /**
     * original: view.ui.goods.UIMaintenance, line 110
     */
    public static CharSequence valueTitle;
    /**
     * original: view.ui.goods.UIMaintenance, line 110
     */
    public static CharSequence titleBuildingMaintenance;
    /**
     * original: view.ui.goods.UIMaintenance, line 200
     */
    public static CharSequence total;


    /**
     * original: view.ui.goods.UIProduction.¤¤Name
     */
    public static CharSequence production;
    /**
     * original: view.ui.goods.UIProduction, line 41
     */
    public static CharSequence producers;
    /**
     * original: view.ui.goods.UIProduction, line 122
     */
    public static CharSequence titleProduction;
    /**
     * original: view.ui.goods.UIProduction, line 125
     */
    public static CharSequence totalValues;


    /**
     * original: view.ui.goods.UIRecipes.¤¤Name
     */
    public static CharSequence recipes;
    /**
     * original: view.ui.goods.UIRecipes, line 58
     */
    public static CharSequence titleRecipes;


    /**
     * original: view.ui.goods.UIValues.¤¤Name
     */
    public static CharSequence values;
    /**
     * original: view.ui.goods.UIValues, line 54
     */
    public static CharSequence titleValues;


    /**
     * original: view.ui.tech.Node_Extra, line 79
     */
    public static CharSequence techBelow1;
    /**
     * original: view.ui.tech.Node_Extra, line 91
     */
    public static CharSequence techCost;
    /**
     * original: view.ui.tech.Node_Extra, line 93
     */
    public static CharSequence techCostFail;
    /**
     * original: view.ui.tech.Node_Extra, line 104
     */
    public static CharSequence techBenefit;
    /**
     * original: view.ui.tech.Node_Extra, line 106
     */
    public static CharSequence techBenefitFail;
    /**
     * original: view.ui.tech.Node_Extra, line 113
     */
    public static CharSequence techTip;


    static {
        // not sure if this runs behind of main(String[] args), but it works (on my machine)
        // if some lang could not use utf-8, edit later
        loadProperties(
                ResourceBundle.getBundle(
                        "extra_info_i18n/extra_info", findLocale(), new EncodedControl(StandardCharsets.UTF_8)
                )
        );
    }

    /**
     * this method is not always powerful. a much more stable way, visit:
     * https://github.com/4rg0n/songs-of-syx-mod-more-options/blob/main/mod-sdk/src/main/java/com/github/argon/sos/mod/sdk/game/api/GameLangApi.java
     */
    private static Locale findLocale() {
        if ("德纳里".contentEquals(Dic.¤¤Curr)) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        return Locale.ROOT; // using Locale.ENGLISH here means extra_info.properties cannot be read (on my machine)

    }

    private static void loadProperties(ResourceBundle bundle) {

        totalCosts = bundle.getString("totalCosts");

        // view.ui.economy.RRow
        year3 = bundle.getString("year3");
        year1 = bundle.getString("year1");
        yesterday = bundle.getString("yesterday");

        // view.ui.economy.UITreasury
        treasuryTop = bundle.getString("treasuryTop");
        treasuryMsg1 = bundle.getString("treasuryMsg1");
        treasuryMsg2 = bundle.getString("treasuryMsg2");
        treasuryMsg3 = bundle.getString("treasuryMsg3");
        treasuryMsg4 = bundle.getString("treasuryMsg4");
        treasuryTip = bundle.getString("treasuryTip");
        treasuryInfo = bundle.getString("treasuryInfo");

        // view.ui.goods.UIExpenses
        expenses = bundle.getString("expenses");
        consumers = bundle.getString("consumers");
        titleExpenses = bundle.getString("titleExpenses");

        // view.ui.goods.UIIndustries
        industries = bundle.getString("industries");
        industryProfitability = bundle.getString("industryProfitability");

        // view.ui.goods.UILogistics
        logistics = bundle.getString("logistics");

        // view.ui.goods.UIMaintenance
        maintenance = bundle.getString("maintenance");
        overallMaintenance = bundle.getString("overallMaintenance");
        titleMaintenance = bundle.getString("titleMaintenance");
        overallBuildingMaintenance = bundle.getString("overallBuildingMaintenance");
        buildingsTitle = bundle.getString("buildings");
        importTitle = bundle.getString("import");
        valueTitle = bundle.getString("value");
        titleBuildingMaintenance = bundle.getString("titleBuildingMaintenance");
        total = bundle.getString("total");

        // view.ui.goods.UIProduction
        production = bundle.getString("production");
        producers = bundle.getString("producers");
        titleProduction = bundle.getString("titleProduction");
        totalValues = bundle.getString("totalValues");

        // view.ui.goods.UIRecipes
        recipes = bundle.getString("recipes");
        titleRecipes = bundle.getString("titleRecipes");

        // view.ui.goods.UIValues
        values = bundle.getString("values");
        titleValues = bundle.getString("titleValues");

        // view.ui.tech.Node_Extra
        techBelow1 = bundle.getString("techBelow1");
        techCost = bundle.getString("techCost");
        techCostFail = bundle.getString("techCostFail");
        techBenefit = bundle.getString("techBenefit");
        techBenefitFail = bundle.getString("techBenefitFail");
        techTip = bundle.getString("techTip");

    }

    private static class EncodedControl extends ResourceBundle.Control {
        private final Charset charset;

        EncodedControl(Charset charset) {
            this.charset = charset;
        }

        @Override
        public ResourceBundle newBundle(
                String baseName, Locale locale, String format, ClassLoader loader, boolean reload
        ) throws IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            try (InputStream stream = loader.getResourceAsStream(resourceName)) {
                if (stream == null) {
                    return null;
                }
                try (Reader reader = new InputStreamReader(stream, charset)) {
                    return new PropertyResourceBundle(reader);
                }
            }
        }


    }


}
