/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.util.LocaleIconFactory;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.CheckBoxJListSelectionPanel;

public class ConfigAvailablesLanguagePanel
extends OptionsPanel {
    private static final long serialVersionUID = 1L;
    public static final String NAME = I18N.getString("org.saig.jump.widgets.config.ConfigAvailablesLanguagePanel.Available-languages-selection");
    public static final Icon ICON = GUIUtil.toSmallIcon(IconLoader.icon("advancedStyleEditor.png"));
    protected JScrollPane localeListScrollPane;
    protected CheckBoxJListSelectionPanel<Locale> localeList;

    public ConfigAvailablesLanguagePanel() {
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, this.getMainPanel());
        FormUtils.addFiller(this, 1, 0);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.localeListScrollPane = new JScrollPane();
        this.localeList = new CheckBoxJListSelectionPanel<Locale>(this.getAvailableLocales(), I18N.getString("org.saig.jump.widgets.config.ConfigAvailablesLanguagePanel.Available-languages"), new Dimension(50, 100), true, true, new Comparator<Locale>(){

            @Override
            public int compare(Locale o1, Locale o2) {
                return o1.getDisplayName().toLowerCase().compareTo(o2.getDisplayName().toLowerCase());
            }
        }, new LocaleSelectionRenderer());
        this.localeList.addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
            }
        });
        this.localeList.setSelectedObjects(LocaleManager.getAvailablesLocales());
        this.localeListScrollPane.getViewport().add(this.localeList, null);
        this.localeListScrollPane.setMinimumSize(new Dimension(220, 400));
        this.localeListScrollPane.setPreferredSize(new Dimension(220, 400));
        mainPanel.add((Component)this.localeListScrollPane, "Center");
        return mainPanel;
    }

    private List<Locale> getAvailableLocales() {
        ArrayList<Locale> availableLocales = new ArrayList<Locale>();
        Locale[] alocale = Locale.getAvailableLocales();
        int i = 0;
        while (i < alocale.length) {
            if (alocale[i].getCountry().length() == 0 && LocaleIconFactory.getIcon(alocale[i]) != null) {
                availableLocales.add(alocale[i]);
            }
            ++i;
        }
        availableLocales.add(new Locale("es", "EU"));
        availableLocales.add(new Locale("es", "AR"));
        availableLocales.add(new Locale("es", "BO"));
        availableLocales.add(new Locale("es", "CL"));
        availableLocales.add(new Locale("es", "CO"));
        availableLocales.add(new Locale("es", "CR"));
        availableLocales.add(new Locale("es", "DO"));
        availableLocales.add(new Locale("es", "EC"));
        availableLocales.add(new Locale("es", "GT"));
        availableLocales.add(new Locale("es", "HN"));
        availableLocales.add(new Locale("es", "MX"));
        availableLocales.add(new Locale("es", "NI"));
        availableLocales.add(new Locale("es", "PA"));
        availableLocales.add(new Locale("es", "PE"));
        availableLocales.add(new Locale("es", "PR"));
        availableLocales.add(new Locale("es", "PY"));
        availableLocales.add(new Locale("es", "SV"));
        availableLocales.add(new Locale("es", "UY"));
        availableLocales.add(new Locale("es", "VE"));
        availableLocales.add(new Locale("fj"));
        availableLocales.add(new Locale("fo"));
        availableLocales.add(new Locale("hy"));
        availableLocales.add(new Locale("ka"));
        availableLocales.add(new Locale("kl"));
        availableLocales.add(new Locale("km"));
        availableLocales.add(new Locale("lo"));
        return availableLocales;
    }

    @Override
    public String validateInput() {
        return null;
    }

    @Override
    public void okPressed() {
        HashSet<Locale> selectedLocales = new HashSet<Locale>();
        List<Locale> selectedLanguages = this.localeList.getSelectedObjects();
        if (selectedLanguages.isEmpty()) {
            selectedLocales.add(I18N.getLocale());
        } else {
            for (Locale language : selectedLanguages) {
                selectedLocales.add(language);
            }
        }
        LocaleManager.setAvailablesLocales(selectedLocales);
    }

    @Override
    public void init() {
        ArrayList<Locale> availablesLanguages = new ArrayList<Locale>();
        for (Locale locale : LocaleManager.getAvailablesLocales()) {
            availablesLanguages.add(locale);
        }
        this.localeList.setSelectedObjects(availablesLanguages);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private class LocaleSelectionRenderer
    extends CheckBoxJListSelectionPanel.CheckBoxJListCellRenderer {
        private static final long serialVersionUID = 1L;
        private JLabel localeIconLabel;

        @Override
        public void initialize() {
            this.setLayout(new FlowLayout(0));
            this.setOpaque(true);
            this.checkbox.setFont(this.font);
            this.checkbox.setOpaque(true);
            this.localeIconLabel = new JLabel();
            this.add(this.checkbox);
            this.add(this.localeIconLabel);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Locale obj = (Locale)value;
            this.localeIconLabel.setIcon(LocaleIconFactory.getIcon(obj));
            this.localeIconLabel.setText(obj.getDisplayName());
            this.checkbox.setSelected(this.selectionMap.containsKey(obj) && (Boolean)this.selectionMap.get(obj) != false);
            if (isSelected) {
                this.checkbox.setForeground(list.getSelectionForeground());
                this.checkbox.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
                this.setBackground(list.getSelectionBackground());
            } else {
                this.checkbox.setForeground(list.getForeground());
                this.checkbox.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
                this.setBackground(list.getBackground());
            }
            this.setEnabled(list.isEnabled());
            this.setFont(this.font);
            return this;
        }
    }
}

