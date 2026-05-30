/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.QualifiedName
 */
package es.kosmo.desktop.widgets.sdi.wfs;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.widgets.sdi.wfs.WFSAttributesCheckboxTreeVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.deegree.datatypes.QualifiedName;
import org.saig.jump.widgets.util.CheckBoxNode;
import org.saig.jump.widgets.util.CheckboxTree;
import org.saig.jump.widgets.util.trees.INavigableTreeVisitor;

public class WFSAttributesCheckboxTree
extends CheckboxTree {
    private static final long serialVersionUID = 1L;

    @Override
    public INavigableTreeVisitor getTreeVisitor() {
        return new WFSAttributesCheckboxTreeVisitor();
    }

    @Override
    public void buildTree(Map<String, Vector<?>> elements) {
        this.buildTree(elements, new TreeMap<String, Boolean>());
    }

    public void buildTree(Map<String, Vector<?>> elements, Map<String, Boolean> featureTypesEnabledMap) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        CheckboxTree.NamedVector<CheckBoxNode> currentVector = null;
        for (String featureTypeName : elements.keySet()) {
            boolean isEnabled = featureTypesEnabledMap.get(featureTypeName);
            Vector<?> currentElements = elements.get(featureTypeName);
            ArrayList<CheckBoxNode> nodes = new ArrayList<CheckBoxNode>();
            Iterator<?> itElements = currentElements.iterator();
            while (itElements.hasNext()) {
                Object currentElement;
                boolean isGeometryProperty = (currentElement = itElements.next()) instanceof QualifiedName;
                nodes.add(new CheckBoxNode(currentElement, null, true, !isGeometryProperty));
            }
            CheckBoxNode node = new CheckBoxNode(featureTypeName, IconLoader.icon("world.png"), isEnabled, isEnabled, nodes);
            DefaultMutableTreeNode featTypeNode = new DefaultMutableTreeNode(node);
            currentVector = new CheckboxTree.NamedVector<CheckBoxNode>(this, featureTypeName, nodes);
            JTree.DynamicUtilTreeNode.createChildren(featTypeNode, currentVector);
            root.add(featTypeNode);
        }
        DefaultTreeModel model = new DefaultTreeModel(root);
        this.setModel(model);
        this.expandPath(new TreePath(model.getRoot()));
    }
}

