/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.toedter.calendar.JDateChooser
 */
package org.saig.core.model.data.widgets.tables.management;

import com.toedter.calendar.JDateChooser;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.SpinnerNumberAndNullModel;

public class RecordAttributesFormDialog
extends JDialog {
    JScrollPane formPanel;
    FeatureSchema fs;
    OKCancelPanel okCancelPanel;

    public RecordAttributesFormDialog(FeatureSchema fs) {
        super(JUMPWorkbench.getFrameInstance());
        this.setTitle(I18N.getString(this.getClass(), "values"));
        this.setModal(true);
        this.fs = fs;
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add((Component)this.createFormPanel(), "Center");
        mainPanel.add((Component)this.createOkCancelPanel(), "South");
        this.setContentPane(mainPanel);
        this.pack();
        GUIUtil.centre(this, JUMPWorkbench.getFrameInstance());
    }

    public JComponent createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        this.formPanel = new JScrollPane(panel);
        this.formPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "form-view")));
        List<String> atributos = this.fs.getAttributeNames();
        Iterator<String> it = atributos.iterator();
        int pos = 0;
        while (it.hasNext()) {
            String atributo = it.next();
            Attribute at = this.fs.getAttribute(atributo);
            JLabel etiqueta = new JLabel(atributo);
            JComponent componente = this.getComponentByAttribute(at);
            FormUtils.addRowInGBL((JComponent)panel, pos, 0, etiqueta, componente, componente instanceof JScrollPane);
            ++pos;
        }
        return this.formPanel;
    }

    private JComponent getComponentByAttribute(Attribute at) {
        Object campo;
        AttributeType type = at.getType();
        if (AttributeType.isDate(type)) {
            campo = new JDateChooser();
            this.setDimensions((JComponent)campo, new Dimension(90, 20));
        } else if (type.equals(AttributeType.INTEGER) || type.equals(AttributeType.BIGINT) || type.equals(AttributeType.LONG)) {
            campo = new JSpinner(new SpinnerNumberAndNullModel(null, Integer.valueOf(-999999999), Integer.valueOf(999999999), (Number)1, 0));
            JComponent editor = ((JSpinner)campo).getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor)editor).getTextField().setEditable(true);
            }
            this.setDimensions((JComponent)campo, new Dimension(90, 20));
        } else if (type.equals(AttributeType.DOUBLE) || type.equals(AttributeType.FLOAT)) {
            campo = new JSpinner(new SpinnerNumberAndNullModel(null, Double.valueOf(-9.99999999E8), Double.valueOf(9.99999999E8), (Number)1.0, 2));
            JComponent editor = ((JSpinner)campo).getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                ((JSpinner.DefaultEditor)editor).getTextField().setEditable(true);
            }
            this.setDimensions((JComponent)campo, new Dimension(90, 20));
        } else if (type.equals(AttributeType.BOOLEAN)) {
            campo = new JCheckBox();
        } else {
            JTextArea area = new JTextArea();
            JScrollPane areaScrollPane = new JScrollPane(area);
            areaScrollPane.setVerticalScrollBarPolicy(22);
            area.setRows(3);
            campo = areaScrollPane;
        }
        return campo;
    }

    private void setDimensions(JComponent comp, Dimension dim) {
        comp.setMinimumSize(dim);
        comp.setMaximumSize(dim);
        comp.setPreferredSize(dim);
    }

    public Map<String, Object> getValues() {
        HashMap<String, Object> values = new HashMap<String, Object>();
        JPanel panel = (JPanel)this.formPanel.getViewport().getComponents()[0];
        Component[] components = panel.getComponents();
        int componentPos = 0;
        int tamComponent = components.length;
        int i = 0;
        while (i < this.fs.getAttributes().size()) {
            boolean asigned = false;
            while (componentPos < tamComponent && !asigned) {
                Component component = components[componentPos];
                ++componentPos;
                if (component instanceof JLabel) continue;
                values.put(this.fs.getAttributeName(i), this.getValueFromComponent(component));
                asigned = true;
            }
            ++i;
        }
        return values;
    }

    private Object getValueFromComponent(Component component) {
        Object value = null;
        if (component instanceof JDateChooser) {
            JDateChooser comp = (JDateChooser)component;
            value = comp.getDate();
        } else if (component instanceof JSpinner) {
            JSpinner comp = (JSpinner)component;
            value = comp.getValue();
        } else if (component instanceof JCheckBox) {
            JCheckBox comp = (JCheckBox)component;
            value = comp.isSelected();
        } else if (component instanceof JScrollPane) {
            JScrollPane comp = (JScrollPane)component;
            JTextArea area = (JTextArea)comp.getViewport().getComponent(0);
            value = area.getText();
        }
        if (value != null && value.equals("")) {
            value = null;
        }
        return value;
    }

    private JPanel createOkCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                RecordAttributesFormDialog.this.setVisible(false);
            }
        });
        return this.okCancelPanel;
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }
}

