/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.Timer;

public abstract class AbstractMultiKeySelectionManager
implements JComboBox.KeySelectionManager {
    private StringBuffer currentSearch = new StringBuffer();
    private Timer resetTimer;
    private static final int RESET_DELAY = 2000;
    protected final String searchField;

    public abstract Object getAttributeValue(Object var1);

    public AbstractMultiKeySelectionManager(String searchField) {
        this.searchField = searchField;
        this.resetTimer = new Timer(2000, new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                AbstractMultiKeySelectionManager.this.currentSearch.setLength(0);
            }
        });
    }

    @Override
    public int selectionForKey(char aKey, ComboBoxModel aModel) {
        if (aKey == '\uffff') {
            this.currentSearch.setLength(0);
            return -1;
        }
        this.resetTimer.stop();
        char key = Character.toUpperCase(aKey);
        this.currentSearch.append(key);
        Object selectedElement = aModel.getSelectedItem();
        int selectedIndex = -1;
        if (selectedElement != null) {
            int i = 0;
            int n = aModel.getSize();
            while (i < n) {
                if (aModel.getElementAt(i) == selectedElement) {
                    selectedIndex = i;
                    break;
                }
                ++i;
            }
        }
        boolean found = false;
        String search = this.currentSearch.toString();
        int i = 0;
        int n = aModel.getSize();
        while (i < n) {
            String element;
            Object attrValue = this.getAttributeValue(aModel.getElementAt(selectedIndex));
            if (attrValue != null && (element = attrValue.toString().toUpperCase()).startsWith(search)) {
                found = true;
                break;
            }
            if (++selectedIndex == n) {
                selectedIndex = 0;
            }
            ++i;
        }
        this.resetTimer.start();
        return found ? selectedIndex : -1;
    }
}

