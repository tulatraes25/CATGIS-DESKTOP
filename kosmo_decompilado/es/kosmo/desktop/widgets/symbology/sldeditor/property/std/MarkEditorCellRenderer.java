/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.sldeditor.property.std;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.renderer.LegendIconMaker;
import org.saig.core.styling.FillImpl;
import org.saig.core.styling.GraphicImpl;
import org.saig.core.styling.MarkImpl;
import org.saig.core.styling.PointSymbolizerImpl;
import org.saig.core.styling.Symbolizer;
import org.saig.core.util.ColorUtil;
import org.saig.jump.lang.I18N;

public class MarkEditorCellRenderer
extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;
    private static final int ICON_SIZE = 20;
    private static final int MARK_SIZE = 16;
    private static final int ICON_CACHE_SIZE = 30;
    private static Map<String, Icon> iconCache = new LinkedHashMap<String, Icon>(30, 0.75f, true){
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Icon> eldest) {
            return this.size() > 30;
        }
    };
    private static Map<String, String> translatedMarkNames = new HashMap<String, String>();

    public MarkEditorCellRenderer() {
        translatedMarkNames = new HashMap<String, String>();
        translatedMarkNames.put("square", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultMarkEditor.square"));
        translatedMarkNames.put("circle", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultMarkEditor.circle"));
        translatedMarkNames.put("triangle", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultMarkEditor.triangle"));
        translatedMarkNames.put("star", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultMarkEditor.star"));
        translatedMarkNames.put("cross", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultMarkEditor.cross"));
        translatedMarkNames.put("arrow", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultMarkEditor.arrow"));
        translatedMarkNames.put("x", I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultMarkEditor.x"));
        translatedMarkNames.put("hatch", "hatch");
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel)super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
        if (value != null) {
            String markWKName = (String)value;
            Icon icon = iconCache.get(markWKName);
            if (icon == null) {
                FillImpl fill = new FillImpl();
                fill.setColor(new LiteralExpressionImpl(ColorUtil.toHex(Color.RED)));
                fill.setOpacity(new LiteralExpressionImpl(1.0));
                MarkImpl mark = new MarkImpl(markWKName);
                mark.setFill(fill);
                GraphicImpl gr = new GraphicImpl();
                gr.addMark(mark);
                gr.setSize(new LiteralExpressionImpl(16));
                PointSymbolizerImpl baseSymb = new PointSymbolizerImpl();
                baseSymb.setGraphic(gr);
                Color transparentColor = new Color(0.0f, 0.0f, 0.0f, 0.0f);
                icon = new ImageIcon(LegendIconMaker.reallyMakeLegendIcon(20, 20, transparentColor, new Symbolizer[]{baseSymb}));
                iconCache.put(markWKName, icon);
            }
            label.setIcon(icon);
            if (translatedMarkNames.containsKey(markWKName)) {
                label.setText(translatedMarkNames.get(markWKName));
            }
        }
        return label;
    }
}

