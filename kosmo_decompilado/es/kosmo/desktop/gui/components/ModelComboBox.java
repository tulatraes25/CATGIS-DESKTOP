/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.gui.components;

import es.kosmo.desktop.gui.components.ModelComponent;
import es.kosmo.desktop.gui.components.ModelHelperEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComboBox;

public class ModelComboBox
extends JComboBox
implements ModelComponent {
    private static final long serialVersionUID = 1L;
    private static final String NULLVAL = "------";
    private Map<ModelHelperEvent, ModelComponent> actions;

    public void init(List<?> models, String fieldToShow) {
        this.removeAllItems();
        this.feedComboBox(models);
        this.actions = new HashMap<ModelHelperEvent, ModelComponent>();
        this.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ModelComboBox.this.fireEvents();
            }
        });
    }

    public void feedComboBox(List<?> models) {
        this.addItem(NULLVAL);
        for (Object model : models) {
            this.addItem(model);
        }
    }

    @Override
    public Object getSelectedItem() {
        if (super.getSelectedItem() == NULLVAL) {
            return null;
        }
        return super.getSelectedItem();
    }

    @Override
    public void update() {
    }

    @Override
    public void refresh() {
    }

    public void addAction(ModelHelperEvent event, ModelComponent mc) {
        this.actions.put(event, mc);
    }

    private void fireEvents() {
        Set<ModelHelperEvent> events = this.actions.keySet();
        for (ModelHelperEvent event : events) {
            event.eventFired(this, this.actions.get(event));
        }
    }
}

