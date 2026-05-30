/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.style.full;

import com.vividsolutions.jump.feature.FeatureSchema;
import es.kosmo.desktop.images.DesktopIconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.gui.swing.sldeditor.style.StyleEditor;
import org.saig.core.gui.swing.sldeditor.style.full.FTSMetadataEditor;
import org.saig.core.gui.swing.sldeditor.style.full.RuleMetadataEditor;
import org.saig.core.gui.swing.sldeditor.style.full.StyleMetadataEditor;
import org.saig.core.gui.swing.sldeditor.style.full.TreeStyleEditor;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerChooserDialog;
import org.saig.core.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.gui.swing.sldeditor.util.SymbolizerUtils;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Style;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class FullStyleEditor
extends JComponent
implements SLDEditor,
StyleEditor {
    private static final long serialVersionUID = 1L;
    private FeatureSchema schema;
    private JButton btnCollapse;
    private JButton btnExpand;
    private Object currentObject;
    private JScrollPane scpTree;
    private JPanel treePanel;
    private TreeStyleEditor treeEditor;
    private JButton btnMoveDown;
    private JButton btnMoveUp;
    private JButton btnRemove;
    private JButton btnAdd;
    private JToolBar toolbar;
    private JSplitPane splitPane;
    private Style style;

    public FullStyleEditor(FeatureSchema ft) {
        this(ft, null);
    }

    public FullStyleEditor(FeatureSchema schema, Style s) {
        this.schema = schema;
        this.splitPane = new JSplitPane(1);
        this.toolbar = new JToolBar(0);
        this.toolbar.setFloatable(false);
        this.toolbar.setBorderPainted(false);
        this.toolbar.setRollover(true);
        this.btnAdd = new JButton(DesktopIconLoader.icon("add_16.png"));
        this.btnAdd.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.FullStyleEditor.add"));
        this.btnRemove = new JButton(DesktopIconLoader.icon("delete_16.png"));
        this.btnRemove.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.FullStyleEditor.delete"));
        this.btnMoveUp = new JButton(DesktopIconLoader.icon("up_16.png"));
        this.btnMoveUp.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.FullStyleEditor.move-up"));
        this.btnMoveDown = new JButton(DesktopIconLoader.icon("down_16.png"));
        this.btnMoveDown.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.FullStyleEditor.move-down"));
        this.btnExpand = new JButton(DesktopIconLoader.icon("right_16.png"));
        this.btnExpand.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.FullStyleEditor.expand-tree"));
        this.btnCollapse = new JButton(DesktopIconLoader.icon("left_16.png"));
        this.btnCollapse.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.FullStyleEditor.collapse-tree"));
        this.toolbar.add(this.btnAdd);
        this.toolbar.add(this.btnRemove);
        this.toolbar.add(new JToolBar.Separator());
        this.toolbar.add(this.btnMoveUp);
        this.toolbar.add(this.btnMoveDown);
        this.toolbar.add(new JToolBar.Separator());
        this.toolbar.add(this.btnCollapse);
        this.toolbar.add(this.btnExpand);
        this.treeEditor = new TreeStyleEditor(s, schema, schema.getGeometryType() == 0 && schema.hasAttribute("image"));
        this.treeEditor.setSelectionObject(s);
        this.scpTree = new JScrollPane(this.treeEditor);
        this.treePanel = new JPanel();
        this.treePanel.setLayout(new BorderLayout());
        this.treePanel.setMinimumSize(new Dimension(175, 500));
        this.treePanel.setPreferredSize(new Dimension(175, 500));
        this.treePanel.add((Component)this.toolbar, "North");
        this.treePanel.add(this.scpTree);
        if (s != null) {
            StyleMetadataEditor sme = new StyleMetadataEditor();
            sme.setStyle(s);
            this.splitPane.setRightComponent(sme);
            this.currentObject = s;
        }
        this.splitPane.setLeftComponent(this.treePanel);
        this.setLayout(new BorderLayout());
        this.add(this.splitPane);
        this.treeEditor.expandTree();
        this.treeEditor.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                FullStyleEditor.this.selectionChanged();
            }
        });
        this.btnExpand.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FullStyleEditor.this.treeEditor.expandTree();
            }
        });
        this.btnCollapse.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FullStyleEditor.this.treeEditor.collapseTree();
            }
        });
        this.btnAdd.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FullStyleEditor.this.addElementInTree();
            }
        });
        this.btnRemove.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FullStyleEditor.this.removeElementInTree();
            }
        });
        this.btnMoveDown.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FullStyleEditor.this.moveDownSelection();
            }
        });
        this.btnMoveUp.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                FullStyleEditor.this.moveUpSelection();
            }
        });
    }

    protected void moveDownSelection() {
        Object selection = this.treeEditor.getSelectionObject();
        if (selection == null) {
            return;
        }
        this.treeEditor.moveDown(selection);
        this.treeEditor.setSelectionObject(selection);
    }

    protected void moveUpSelection() {
        Object selection = this.treeEditor.getSelectionObject();
        if (selection == null) {
            return;
        }
        this.treeEditor.moveUp(selection);
        this.treeEditor.setSelectionObject(selection);
    }

    protected void removeElementInTree() {
        Object selection = this.treeEditor.getSelectionObject();
        if (this.treeEditor.wouldRemoveRoot(selection)) {
            DialogFactory.showInformationDialog(this, I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.FullStyleEditor.cannot-remove-this-object-would-have-to-remove-the-style-too"), I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.FullStyleEditor.style-editor"));
            return;
        }
        Object[] siblings = this.treeEditor.getSiblings(selection);
        if (siblings.length == 0) {
            int answer = DialogFactory.showYesNoDialog(this, I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.FullStyleEditor.this-object-is-isolated-will-have-to-remove-its-parent-proceed"), I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.FullStyleEditor.style-editor"));
            if (answer == 0) {
                this.treeEditor.remove(selection);
                if (selection instanceof FeatureTypeStyle) {
                    this.treeEditor.setSelectionObject(this.treeEditor.getStyle());
                    StyleMetadataEditor sme = (StyleMetadataEditor)this.splitPane.getRightComponent();
                    sme.setStyle(this.treeEditor.getStyle());
                }
                this.treeEditor.setSelectionObject(this.style);
            }
        } else {
            Object sibling = this.treeEditor.getSiblingAfter(selection);
            if (sibling == null) {
                sibling = this.treeEditor.getSiblingBefore(selection);
            }
            this.treeEditor.remove(selection);
            if (selection instanceof FeatureTypeStyle) {
                this.treeEditor.setSelectionObject(this.treeEditor.getStyle());
                StyleMetadataEditor sme = (StyleMetadataEditor)this.splitPane.getRightComponent();
                sme.setStyle(this.treeEditor.getStyle());
            }
            this.treeEditor.setSelectionObject(sibling);
        }
    }

    protected void addElementInTree() {
        this.storeChangesIntoStyle();
        SymbolizerChooserDialog dialog = symbolizerEditorFactory.createSymbolizerChooserDialog(this, this.schema);
        dialog.setVisible(true);
        if (!dialog.exitOk()) {
            return;
        }
        Symbolizer symbolizer = dialog.getSelectedSymbolizer();
        Object selection = this.treeEditor.getSelectionObject();
        if (selection == null) {
            return;
        }
        if (selection instanceof Style) {
            FeatureTypeStyle fts = styleBuilder.createFeatureTypeStyle(symbolizer);
            StyleMetadataEditor sme = (StyleMetadataEditor)this.splitPane.getRightComponent();
            this.treeEditor.addFeatureTypeStyle(fts);
            sme.setStyle(this.treeEditor.getStyle());
        } else if (selection instanceof FeatureTypeStyle) {
            Rule r = styleBuilder.createRule(symbolizer);
            this.treeEditor.addRule((FeatureTypeStyle)selection, r);
        } else if (selection instanceof Rule) {
            this.treeEditor.addSymbolizer((Rule)selection, symbolizer);
        } else if (selection instanceof Symbolizer) {
            Rule rule = (Rule)this.treeEditor.findParentObject(selection);
            this.treeEditor.addSymbolizer(rule, symbolizer);
        }
    }

    protected void selectionChanged() {
        this.storeChangesIntoStyle();
        JComponent editorComponent = null;
        Object selection = this.treeEditor.getSelectionObject();
        if (selection == null) {
            this.splitPane.setRightComponent(null);
        } else {
            if (selection instanceof Style) {
                Style style = (Style)selection;
                StyleMetadataEditor sme = new StyleMetadataEditor();
                sme.setStyle(style);
                editorComponent = sme;
            } else if (selection instanceof FeatureTypeStyle) {
                FeatureTypeStyle fts = (FeatureTypeStyle)selection;
                editorComponent = new FTSMetadataEditor(fts, this.schema);
            } else if (selection instanceof Rule) {
                Rule rule = (Rule)selection;
                editorComponent = new RuleMetadataEditor(rule, this.schema);
            } else if (selection instanceof Symbolizer) {
                SymbolizerEditor se = SymbolizerUtils.getSymbolizerEditor((Symbolizer)selection, this.schema);
                se.setSymbolizer((Symbolizer)selection);
                editorComponent = se;
            }
            this.currentObject = selection;
        }
        this.splitPane.setRightComponent(editorComponent);
        this.splitPane.revalidate();
        FormUtils.repackParentWindow(this);
    }

    private void storeChangesIntoStyle() {
        if (this.currentObject != null && this.splitPane.getRightComponent() != null) {
            if (this.currentObject instanceof Style) {
                Style style = (Style)this.currentObject;
                StyleMetadataEditor sme = (StyleMetadataEditor)this.splitPane.getRightComponent();
                sme.fillMetadata(style);
            } else if (this.currentObject instanceof FeatureTypeStyle) {
                FeatureTypeStyle fts = (FeatureTypeStyle)this.currentObject;
                FTSMetadataEditor editor = (FTSMetadataEditor)this.splitPane.getRightComponent();
                editor.fillFeatureTypeStyle(fts);
            } else if (this.currentObject instanceof Rule) {
                Rule rule = (Rule)this.currentObject;
                RuleMetadataEditor editor = (RuleMetadataEditor)this.splitPane.getRightComponent();
                editor.fillRule(rule);
            } else if (this.currentObject instanceof Symbolizer) {
                SymbolizerEditor se = (SymbolizerEditor)this.splitPane.getRightComponent();
                se.getSymbolizer();
            }
        }
    }

    @Override
    public void setStyle(Style s) {
        if (s == null) {
            FeatureTypeStyle featureStyle = styleBuilder.createFeatureTypeStyle(SymbolizerUtils.getDefaultSymbolizer(this.schema));
            Rule r = featureStyle.getRules()[0];
            r.setName(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.FullStyleEditor.rule1"));
            featureStyle.setName(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.FullStyleEditor.featureTypeStyle1"));
            this.style = styleBuilder.createStyle();
            this.style.addFeatureTypeStyle(featureStyle);
        } else {
            this.style = s;
            this.treeEditor.setStyle(s);
            this.treeEditor.setSelectionObject(s);
        }
    }

    @Override
    public Style getStyle() {
        this.storeChangesIntoStyle();
        return this.treeEditor.getStyle();
    }

    @Override
    public boolean canEdit(Style s) {
        return true;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferred = super.getPreferredSize();
        preferred.height = 300;
        return preferred;
    }

    public void setSelectedRule(Rule rule) {
        this.treeEditor.setSelectedRule(rule);
    }
}

