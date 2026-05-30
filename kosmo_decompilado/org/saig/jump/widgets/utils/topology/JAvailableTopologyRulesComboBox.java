/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.utils.topology;

import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import org.apache.log4j.Logger;
import org.saig.core.model.relations.topology.ITopologyBinaryRelation;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.core.model.relations.topology.TopologyRelationsRepository;

public class JAvailableTopologyRulesComboBox
extends JComboBox {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(JAvailableTopologyRulesComboBox.class);
    private String sourceLayerName;
    private String targetLayerName;
    private boolean removeBinaryRelations = false;
    private int sourceLayerGeometryType = 0;

    public JAvailableTopologyRulesComboBox() {
        this(false);
    }

    public JAvailableTopologyRulesComboBox(boolean removeBinaryRelations) {
        this.removeBinaryRelations = removeBinaryRelations;
        this.setRenderer(new AvailableTopologyRulesRenderer());
        this.refresh();
    }

    private void refresh() {
        List<String> topologyRelationIds = TopologyRelationsRepository.getTopologyRelations();
        ArrayList<RelationIdNamePair> topologyRelationIdNamePairs = new ArrayList<RelationIdNamePair>();
        this.removeAllItems();
        for (String currentTopologyRelationName : topologyRelationIds) {
            try {
                ITopologyRelation relation = TopologyRelationsRepository.getTopologyRelation(currentTopologyRelationName);
                if (!relation.checkValidGeometryType(this.sourceLayerGeometryType) || this.removeBinaryRelations && relation instanceof ITopologyBinaryRelation) continue;
                RelationIdNamePair pair = new RelationIdNamePair(relation.getId(), relation.getName());
                topologyRelationIdNamePairs.add(pair);
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        Collections.sort(topologyRelationIdNamePairs);
        for (RelationIdNamePair currentPair : topologyRelationIdNamePairs) {
            this.addItem(currentPair.getRelationId());
        }
    }

    @Override
    public Object getSelectedItem() {
        ITopologyRelation selectedItem = null;
        String topologyRelationName = (String)super.getSelectedItem();
        if (topologyRelationName != null) {
            try {
                selectedItem = TopologyRelationsRepository.getTopologyRelation(topologyRelationName);
                if (this.sourceLayerName != null) {
                    selectedItem.setSourceLayerName(this.sourceLayerName);
                }
                if (this.targetLayerName != null && selectedItem instanceof ITopologyBinaryRelation) {
                    ((ITopologyBinaryRelation)selectedItem).setTargetLayerName(this.targetLayerName);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                selectedItem = null;
            }
        }
        return selectedItem;
    }

    public void setSourceLayer(Layer sourceLayer) {
        boolean refresh = false;
        if (sourceLayer != null) {
            refresh = this.sourceLayerName == null || !this.sourceLayerName.equals(sourceLayer.getName());
            this.sourceLayerName = sourceLayer.getName();
            this.sourceLayerGeometryType = sourceLayer.getFeatureSchema().getGeometryType();
        } else {
            refresh = this.sourceLayerName != null;
            this.sourceLayerName = null;
            this.sourceLayerGeometryType = 0;
        }
        if (refresh) {
            this.refresh();
        }
        if (this.isVisible()) {
            this.repaint();
        }
    }

    public void setTargetLayer(Layer targetLayer) {
        this.targetLayerName = targetLayer != null ? targetLayer.getName() : null;
        if (this.isVisible()) {
            this.repaint();
        }
    }

    public void setRemoveBinaryRelations(boolean removeBinary) {
        if (removeBinary != this.removeBinaryRelations) {
            this.removeBinaryRelations = removeBinary;
            this.refresh();
        }
    }

    private class AvailableTopologyRulesRenderer
    extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        private AvailableTopologyRulesRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
            String toolTip = "";
            if (value instanceof String) {
                try {
                    ITopologyRelation relation = TopologyRelationsRepository.getTopologyRelation((String)value);
                    if (relation != null) {
                        toolTip = relation.getDescription();
                        this.setText(String.valueOf(relation.getName()) + " (" + relation.getId() + ")");
                    }
                    this.setToolTipText(StringUtil.formatTooltip(toolTip));
                }
                catch (Exception e) {
                    LOGGER.error((Object)e);
                }
            }
            return this;
        }
    }

    private class RelationIdNamePair
    implements Comparable<RelationIdNamePair> {
        private String relationId;
        private String relationName;

        public RelationIdNamePair(String id, String name) {
            this.relationId = id;
            this.relationName = name;
        }

        @Override
        public int compareTo(RelationIdNamePair o) {
            return this.getRelationName().compareTo(o.getRelationName());
        }

        public String getRelationName() {
            return this.relationName;
        }

        public String getRelationId() {
            return this.relationId;
        }
    }
}

