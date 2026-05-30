/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.util;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.apache.log4j.Logger;
import org.saig.jump.widgets.util.FilteringComboBoxModel;
import org.saig.jump.widgets.util.FilteringModel;

public class FilteringJComboBox<T>
extends JComboBox
implements ListDataListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FilteringJComboBox.class);
    private JTextComponent input;

    public FilteringJComboBox() {
        super(new FilteringComboBoxModel());
    }

    public FilteringJComboBox(FilteringComboBoxModel<T> filteringModel) {
        super(filteringModel);
    }

    public void installJTextComponent(JTextComponent input) {
        if (input == null) {
            throw new IllegalArgumentException("No se puede instalar un TextComponent nulo");
        }
        if (this.input != null) {
            this.uninstallJTextComponent();
        }
        this.input = input;
        ComboBoxModel model = this.getModel();
        input.getDocument().addDocumentListener((DocumentListener)((Object)model));
    }

    public void uninstallJTextComponent() {
        if (this.input == null) {
            throw new IllegalStateException("No hay TextComponent instalado");
        }
        ComboBoxModel model = this.getModel();
        this.input.getDocument().removeDocumentListener((DocumentListener)((Object)model));
        this.input = null;
    }

    public void setModel(ComboBoxModel model) {
        if (!(model instanceof FilteringComboBoxModel)) {
            throw new IllegalArgumentException("El nuevo ComboBoxModel debe ser FilteringComboBoxModel");
        }
        JTextComponent preInput = this.input;
        if (preInput != null) {
            this.uninstallJTextComponent();
        }
        if (this.getModel() != null) {
            ((AbstractListModel)((Object)this.getModel())).removeListDataListener(this);
        }
        super.setModel(model);
        model.addListDataListener(this);
        if (preInput != null) {
            this.installJTextComponent(preInput);
            try {
                ((FilteringModel)((Object)this.getModel())).filter(preInput.getDocument().getText(0, preInput.getDocument().getLength()));
            }
            catch (BadLocationException ble) {
                LOGGER.error((Object)"", (Throwable)ble);
            }
        }
    }

    public void addElement(T element) {
        ((FilteringModel)((Object)this.getModel())).addElement(element);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("FilteringJComboBox Demo");
        f.setDefaultCloseOperation(3);
        ArrayList<String> locales = new ArrayList<String>();
        Locale[] localeArray = Locale.getAvailableLocales();
        int n = localeArray.length;
        int n2 = 0;
        while (n2 < n) {
            Locale loc = localeArray[n2];
            locales.add(loc.toString());
            ++n2;
        }
        Collections.sort(locales);
        FilteringJComboBox box = new FilteringJComboBox(new FilteringComboBoxModel(locales));
        box.setPreferredSize(new Dimension(150, 20));
        JTextField jtf = new JTextField();
        jtf.setPreferredSize(new Dimension(150, 20));
        box.installJTextComponent(jtf);
        JPanel pane = new JPanel(new FlowLayout());
        f.setContentPane(pane);
        f.getContentPane().add(jtf);
        f.getContentPane().add(box);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public FilteringComboBoxModel<T> getModel() {
        return (FilteringComboBoxModel)super.getModel();
    }
}

