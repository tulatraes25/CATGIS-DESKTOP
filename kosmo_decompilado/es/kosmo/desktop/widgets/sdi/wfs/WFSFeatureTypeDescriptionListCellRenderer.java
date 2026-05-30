/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package es.kosmo.desktop.widgets.sdi.wfs;

import es.kosmo.desktop.widgets.sdi.wfs.WFSFeatureTypeDescription;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import org.apache.commons.lang.StringUtils;

public class WFSFeatureTypeDescriptionListCellRenderer
extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;
    protected static int MAX_CHARACTERS_PER_LINE = 80;

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel)super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
        WFSFeatureTypeDescription wfsTD = (WFSFeatureTypeDescription)value;
        label.setText("<HTML><P>" + wfsTD.getTitle() + " [ <I>" + wfsTD.getQualifiedName() + "</I> ] </P></HTML>");
        label.setToolTipText(this.fixTooltip(wfsTD.getAbstract()));
        return label;
    }

    private String fixTooltip(String tooltip) {
        if (StringUtils.isEmpty((String)tooltip) || tooltip.length() < MAX_CHARACTERS_PER_LINE) {
            return tooltip;
        }
        StringBuilder fixedTooltip = new StringBuilder();
        fixedTooltip.append("<HTML>");
        String[] words = StringUtils.splitByWholeSeparator((String)tooltip, null);
        int currentLinePosition = 0;
        String[] stringArray = words;
        int n = words.length;
        int n2 = 0;
        while (n2 < n) {
            String word = stringArray[n2];
            fixedTooltip.append(word);
            fixedTooltip.append(" ");
            if ((currentLinePosition += word.length() + 1) > MAX_CHARACTERS_PER_LINE) {
                currentLinePosition = 0;
                fixedTooltip.append("<BR>");
            }
            ++n2;
        }
        fixedTooltip.append("</HTML>");
        return fixedTooltip.toString();
    }
}

