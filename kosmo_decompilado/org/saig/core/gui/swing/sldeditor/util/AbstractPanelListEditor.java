/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.util;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public abstract class AbstractPanelListEditor
extends JComponent {
    JTabbedPane tbpPanels;
    JToolBar toolbar;
    JButton btnAddPanel;
    JButton btnRemovePanel;
    JButton btnMoveUpPanel;
    JButton btnMoveDownPanel;
    protected boolean allowZeroPanels;

    public AbstractPanelListEditor(boolean allowZeroPanels) {
        this.allowZeroPanels = allowZeroPanels;
        this.tbpPanels = new JTabbedPane();
        this.btnAddPanel = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("Plus.gif")));
        this.btnAddPanel.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.util.AbstractPanelListEditor.add"));
        this.btnRemovePanel = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("Delete.gif")));
        this.btnRemovePanel.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.util.AbstractPanelListEditor.delete"));
        this.btnMoveUpPanel = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("Up3.gif")));
        this.btnMoveUpPanel.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.util.AbstractPanelListEditor.move-up"));
        this.btnMoveDownPanel = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("Down3.gif")));
        this.btnMoveDownPanel.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.util.AbstractPanelListEditor.move-down"));
        FormUtils.forceButtonDimension(this.btnAddPanel);
        FormUtils.forceButtonDimension(this.btnRemovePanel);
        FormUtils.forceButtonDimension(this.btnMoveUpPanel);
        FormUtils.forceButtonDimension(this.btnMoveDownPanel);
        this.toolbar = new JToolBar(1);
        this.toolbar.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 3));
        this.toolbar.setFloatable(false);
        this.toolbar.add(this.btnAddPanel);
        this.toolbar.add(this.btnRemovePanel);
        this.toolbar.add(this.btnMoveUpPanel);
        this.toolbar.add(this.btnMoveDownPanel);
        this.toolbar.setVisible(true);
        this.setLayout(new BorderLayout());
        this.add((Component)this.toolbar, "West");
        this.add((Component)this.tbpPanels, "Center");
        this.tbpPanels.setTabLayoutPolicy(1);
        this.btnAddPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent event) {
                AbstractPanelListEditor.this.addButtonPressed();
            }
        });
        this.btnRemovePanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent event) {
                AbstractPanelListEditor.this.removeButtonPressed();
            }
        });
        this.btnMoveUpPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent event) {
                AbstractPanelListEditor.this.moveUpButtonPressed();
            }
        });
        this.btnMoveDownPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent event) {
                AbstractPanelListEditor.this.moveDownButtonPressed();
            }
        });
        this.tbpPanels.addContainerListener(new ContainerListener(){

            @Override
            public void componentAdded(ContainerEvent e) {
                AbstractPanelListEditor.this.panelChanged();
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                AbstractPanelListEditor.this.panelChanged();
            }
        });
        this.tbpPanels.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                Component comp;
                TabComponent tc = (TabComponent)AbstractPanelListEditor.this.tbpPanels.getSelectedComponent();
                if (tc != null && tc.getComponent() instanceof Placeholder && (comp = AbstractPanelListEditor.this.lazyInitialiazePanel(AbstractPanelListEditor.this.tbpPanels.getSelectedIndex())) != null) {
                    tc.setComponent(comp);
                }
            }
        });
    }

    protected Component lazyInitialiazePanel(int panelIndex) {
        return null;
    }

    protected void panelChanged() {
        boolean enabled = this.tbpPanels.getTabCount() > 1;
        this.btnMoveDownPanel.setEnabled(enabled);
        this.btnMoveUpPanel.setEnabled(enabled);
        this.btnRemovePanel.setEnabled(this.allowZeroPanels || enabled);
    }

    protected abstract void addButtonPressed();

    protected void removeButtonPressed() {
        int index = this.tbpPanels.getSelectedIndex();
        if (index != -1) {
            this.tbpPanels.remove(index);
        }
    }

    protected void moveDownButtonPressed() {
        int index = this.tbpPanels.getSelectedIndex();
        if (index >= 0 && index < this.tbpPanels.getTabCount() - 1) {
            Component c1 = this.tbpPanels.getComponentAt(index);
            String t1 = this.tbpPanels.getTitleAt(index);
            Component c2 = this.tbpPanels.getComponentAt(index + 1);
            String t2 = this.tbpPanels.getTitleAt(index + 1);
            this.tbpPanels.remove(c1);
            this.tbpPanels.remove(c2);
            this.tbpPanels.add(c2, index);
            this.tbpPanels.add(c1, index + 1);
            this.tbpPanels.setTitleAt(index + 1, t1);
            this.tbpPanels.setTitleAt(index, t2);
            this.tbpPanels.setSelectedIndex(index + 1);
        }
    }

    protected void moveUpButtonPressed() {
        int index = this.tbpPanels.getSelectedIndex();
        if (index > 0) {
            Component c1 = this.tbpPanels.getComponentAt(index);
            String t1 = this.tbpPanels.getTitleAt(index);
            Component c2 = this.tbpPanels.getComponentAt(index - 1);
            String t2 = this.tbpPanels.getTitleAt(index - 1);
            this.tbpPanels.remove(c1);
            this.tbpPanels.remove(c2);
            this.tbpPanels.add(c1, index - 1);
            this.tbpPanels.add(c2, index);
            this.tbpPanels.setTitleAt(index - 1, t1);
            this.tbpPanels.setTitleAt(index, t2);
            this.tbpPanels.setSelectedIndex(index - 1);
        }
    }

    protected void removeAllPanels() {
        this.tbpPanels.removeAll();
    }

    protected void addPanel(String title, Component component) {
        this.tbpPanels.add(title, new TabComponent(component));
    }

    protected void addPlaceholder(String title) {
        this.tbpPanels.add(title, new TabComponent(new Placeholder()));
    }

    protected Component[] getPanels() {
        Component[] comps = new Component[this.tbpPanels.getTabCount()];
        int i = 0;
        while (i < comps.length) {
            TabComponent tc = (TabComponent)this.tbpPanels.getComponentAt(i);
            Component comp = tc.getComponent();
            comps[i] = !(comp instanceof Placeholder) ? comp : null;
            ++i;
        }
        return comps;
    }

    protected int getPanelCount() {
        return this.tbpPanels.getTabCount();
    }

    protected void setComponents(Component[] components, String[] titles) {
        if (components == null || components.length == 0) {
            this.tbpPanels.removeAll();
            return;
        }
        if (components.length != titles.length) {
            throw new IllegalArgumentException(I18N.getString("org.saig.core.gui.swing.sldeditor.util.AbstractPanelListEditor.components-and-title-arrays-have-differente-dimension"));
        }
        this.tbpPanels.removeAll();
        int i = 0;
        while (i < components.length) {
            this.tbpPanels.add(titles[i], components[i]);
            ++i;
        }
    }

    public void setSelectedComponent(Component c) {
        this.tbpPanels.setSelectedComponent(c);
    }

    public void setSelectedIndex(int index) {
        this.tbpPanels.setSelectedIndex(index);
    }

    public static void main(String[] args) throws Exception {
        FormUtils.show(new AbstractPanelListEditor(true){
            private int counter = 0;

            @Override
            protected void addButtonPressed() {
                String title = String.valueOf(this.counter++);
                JPanel panel = new JPanel();
                JLabel label = new JLabel(title);
                panel.add(label);
                this.addPanel(title, panel);
            }
        });
    }

    private static class Placeholder
    extends JLabel {
        public Placeholder() {
            super(I18N.getString("org.saig.core.gui.swing.sldeditor.util.AbstractPanelListEditor.placeholder-if-you-see-this-there-is-a-bug-in-the-lazy-initialization-code"));
        }
    }

    private static class TabComponent
    extends JComponent {
        public TabComponent() {
            this.setLayout(new BorderLayout());
            this.add(new Placeholder());
        }

        public TabComponent(Component component) {
            this.setLayout(new BorderLayout());
            this.add(component);
        }

        public Component getComponent() {
            return this.getComponent(0);
        }

        public void setComponent(Component component) {
            this.removeAll();
            this.add(component);
        }
    }
}

