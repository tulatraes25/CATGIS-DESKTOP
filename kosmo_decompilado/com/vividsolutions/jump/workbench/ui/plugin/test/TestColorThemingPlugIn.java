/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.test;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MainMenuNames;
import com.vividsolutions.jump.workbench.ui.plugin.test.RandomTrianglesPlugIn;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStyle;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TestColorThemingPlugIn
extends AbstractPlugIn {
    private List cities = Arrays.asList("Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii");
    private RandomTrianglesPlugIn randomTrianglesPlugIn = new RandomTrianglesPlugIn();

    @Override
    public void initialize(PlugInContext context) throws Exception {
        context.getFeatureInstaller().addMainMenuItem(this, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_OTHERS}, this.getName(), false, null, null);
        this.randomTrianglesPlugIn.setCities(this.cities);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        ArrayList<String> names = new ArrayList<String>();
        names.addAll(ColorScheme.discreteColorSchemeNames());
        names.addAll(ColorScheme.rangeColorSchemeNames());
        Collections.reverse(names);
        for (String colorScheme : names) {
            this.execute(context, colorScheme);
        }
        return true;
    }

    private void execute(PlugInContext context, String colorSchemeName) throws Exception {
        this.randomTrianglesPlugIn.execute(context, 500);
        Layer layer = context.getLayerManager().getLayer(RandomTrianglesPlugIn.NAME);
        ColorScheme colorScheme = ColorScheme.create(colorSchemeName);
        layer.setName("(" + colorScheme.getColors().size() + ") " + colorSchemeName);
        HashMap<Object, BasicStyle> attributeToStyleMap = new HashMap<Object, BasicStyle>();
        for (String city : this.cities) {
            attributeToStyleMap.put(city, new BasicStyle(colorScheme.next()));
        }
        layer.getBasicStyle().setEnabled(false);
        ColorThemingStyle themeStyle = new ColorThemingStyle("City", attributeToStyleMap, new BasicStyle(Color.gray));
        themeStyle.setEnabled(true);
        layer.addStyle(themeStyle);
        layer.setVisible(false);
    }
}

