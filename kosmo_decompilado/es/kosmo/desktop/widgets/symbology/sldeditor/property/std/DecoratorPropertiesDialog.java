/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.widgets.symbology.sldeditor.property.std;

import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import es.kosmo.core.renderer.decorators.DecoratorFactory;
import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.AbstractDecoratorConfigPanel;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class DecoratorPropertiesDialog
extends JDialog
implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DecoratorPropertiesDialog.class);
    public static final String ADD_DECORATOR_TITLE = I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DecoratorPropertiesDialog.Add-new-decorator");
    public static final String EDIT_DECORATOR_TITLE = I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DecoratorPropertiesDialog.Edit-decorator");
    private JPanel decoratorTypeSelectionPanel;
    private JComboBox decoratorTypeSelectionComboBox;
    private JPanel decoratorPropertiesPanel;
    private CardLayout decoratorConfigPanelCardLayout;
    private OKCancelPanel okCancelPanel;
    private FeatureSchema layerSchema;
    private Map<String, AbstractDecoratorConfigPanel> decoratorIDToConfigPanelMap = new HashMap<String, AbstractDecoratorConfigPanel>();

    public DecoratorPropertiesDialog(JFrame owner, boolean modal, IDecorator decorator, FeatureSchema schema) {
        super((Frame)owner, modal);
        if (decorator == null) {
            this.setTitle(ADD_DECORATOR_TITLE);
        } else {
            this.setTitle(EDIT_DECORATOR_TITLE);
        }
        this.layerSchema = schema;
        this.initialize();
        this.refresh(decorator);
        this.pack();
    }

    private void refresh(IDecorator decorator) {
        List<String> registeredDecorators = DecoratorFactory.getInstance().getDecorators();
        Collections.sort(registeredDecorators, Collator.getInstance(LocaleManager.getActiveLocale()));
        for (String decoratorID : registeredDecorators) {
            try {
                IDecorator builtDecorator = DecoratorFactory.getInstance().getDecorator(decoratorID);
                AbstractDecoratorConfigPanel configPanel = DecoratorFactory.getInstance().getDecoratorConfigPanel(decoratorID);
                configPanel.setSchema(this.layerSchema);
                configPanel.setDecorator(builtDecorator);
                if (!builtDecorator.isCompatible(this.layerSchema.getGeometryType())) continue;
                this.decoratorTypeSelectionComboBox.addItem(builtDecorator);
                this.decoratorPropertiesPanel.add((Component)configPanel, decoratorID);
                this.decoratorIDToConfigPanelMap.put(decoratorID, configPanel);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        if (decorator != null) {
            this.decoratorTypeSelectionComboBox.setSelectedItem(decorator);
            this.refreshVisibleComponents(decorator);
        }
    }

    private void refreshVisibleComponents(IDecorator selectedDecorator) {
        if (selectedDecorator != null) {
            this.decoratorConfigPanelCardLayout.show(this.decoratorPropertiesPanel, selectedDecorator.getName());
            AbstractDecoratorConfigPanel panel = this.decoratorIDToConfigPanelMap.get(selectedDecorator.getName());
            if (panel != null) {
                panel.setDecorator(selectedDecorator);
            }
        }
        this.pack();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getDecoratorTypeSelectionPanel());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getDecoratorPropertiesPanel());
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.getOkCancelPanel());
    }

    private JPanel getDecoratorTypeSelectionPanel() {
        if (this.decoratorTypeSelectionPanel == null) {
            this.decoratorTypeSelectionPanel = new JPanel(new GridBagLayout());
            JLabel decoratorTypeLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DecoratorPropertiesDialog.Decorator-type")) + ": ");
            this.decoratorTypeSelectionComboBox = new JComboBox();
            this.decoratorTypeSelectionComboBox.addActionListener(this);
            this.decoratorTypeSelectionComboBox.setRenderer(new DecoratorTypesComboBoxRenderer());
            FormUtils.addRowInGBL((JComponent)this.decoratorTypeSelectionPanel, 0, 0, decoratorTypeLabel, (JComponent)this.decoratorTypeSelectionComboBox);
        }
        return this.decoratorTypeSelectionPanel;
    }

    private JPanel getDecoratorPropertiesPanel() {
        if (this.decoratorPropertiesPanel == null) {
            this.decoratorConfigPanelCardLayout = new CardLayout();
            this.decoratorPropertiesPanel = new JPanel(this.decoratorConfigPanelCardLayout);
            this.decoratorPropertiesPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DecoratorPropertiesDialog.Decorator-properties")));
        }
        return this.decoratorPropertiesPanel;
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(this);
        }
        return this.okCancelPanel;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okCancelPanel.setOKPressed(false);
        }
        super.setVisible(visible);
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.okCancelPanel)) {
            IDecorator baseDecorator;
            AbstractDecoratorConfigPanel panel;
            String validation;
            if (this.okCancelPanel.wasOKPressed() && StringUtils.isNotEmpty((String)(validation = (panel = this.decoratorIDToConfigPanelMap.get((baseDecorator = (IDecorator)this.decoratorTypeSelectionComboBox.getSelectedItem()).getName())).validateInput()))) {
                DialogFactory.showWarningDialog(this, validation, this.getTitle());
                return;
            }
            this.setVisible(false);
        } else if (e.getSource().equals(this.decoratorTypeSelectionComboBox)) {
            this.refreshVisibleComponents((IDecorator)this.decoratorTypeSelectionComboBox.getSelectedItem());
        }
    }

    public IDecorator getDecorator() {
        IDecorator baseDecorator = (IDecorator)this.decoratorTypeSelectionComboBox.getSelectedItem();
        AbstractDecoratorConfigPanel panel = this.decoratorIDToConfigPanelMap.get(baseDecorator.getName());
        return panel.getDecorator(baseDecorator);
    }

    class DecoratorTypesComboBoxRenderer
    extends JLabel
    implements ListCellRenderer {
        public DecoratorTypesComboBoxRenderer() {
            this.setOpaque(true);
            this.setVerticalAlignment(0);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }
            this.setIcon(((IDecorator)value).getIcon());
            this.setText(value.toString());
            return this;
        }
    }
}

