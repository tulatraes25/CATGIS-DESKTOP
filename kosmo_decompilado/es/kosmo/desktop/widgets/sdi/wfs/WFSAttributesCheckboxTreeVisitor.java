/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.sdi.wfs;

import java.util.ArrayList;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.saig.jump.widgets.util.CheckBoxNode;
import org.saig.jump.widgets.util.trees.AbstractNavigableTreeVisitor;

public class WFSAttributesCheckboxTreeVisitor
extends AbstractNavigableTreeVisitor {
    @Override
    protected void visit(DefaultTreeModel model, DefaultMutableTreeNode node, Object param) {
        Map selectedElements = (Map)param;
        Object userObject = node.getUserObject();
        if (userObject instanceof String) {
            int i = 0;
            while (i < node.getChildCount()) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(i);
                this.visit(model, childNode, param);
                ++i;
            }
        }
        if (!(userObject instanceof CheckBoxNode)) {
            return;
        }
        CheckBoxNode checkBoxNode = (CheckBoxNode)userObject;
        if (!checkBoxNode.isSelected()) {
            return;
        }
        String text = (String)checkBoxNode.getText();
        ArrayList<Object> attrNamesSelected = new ArrayList<Object>();
        int i = 0;
        while (i < node.getChildCount()) {
            CheckBoxNode checkBoxChildNode;
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(i);
            Object currentUserObject = childNode.getUserObject();
            if (currentUserObject instanceof CheckBoxNode && (checkBoxChildNode = (CheckBoxNode)currentUserObject).isSelected()) {
                attrNamesSelected.add(checkBoxChildNode.getText());
            }
            ++i;
        }
        selectedElements.put(text, attrNamesSelected);
    }

    @Override
    protected void processNode(DefaultTreeModel model, DefaultMutableTreeNode childNode, Object param) {
    }
}

