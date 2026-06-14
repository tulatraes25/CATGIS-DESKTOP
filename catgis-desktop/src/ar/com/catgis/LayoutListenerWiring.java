package ar.com.catgis;

import ar.com.catgis.core.model.Layer;
import ar.com.catgis.layout.LayoutElementType;
import ar.com.catgis.layout.LayoutInteractionState;
import ar.com.catgis.layout.LayoutTemplate;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Wires all Swing listeners for MapLayoutComposerDialog.
 * Extracted to reduce line count and separate UI wiring from behavior.
 */
public final class LayoutListenerWiring {

    private LayoutListenerWiring() {
    }

    public static void wireAllListeners(MapLayoutComposerDialog dialog) {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleUpdate();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleUpdate();
            }

            private void handleUpdate() {
                dialog.pushProjectMetadataFromControls();
                dialog.previewPanel.repaint();
            }
        };
        dialog.titleField.getDocument().addDocumentListener(listener);
        dialog.subtitleField.getDocument().addDocumentListener(listener);
        dialog.footerField.getDocument().addDocumentListener(listener);
        dialog.studyField.getDocument().addDocumentListener(listener);
        dialog.companyField.getDocument().addDocumentListener(listener);
        dialog.cartographerField.getDocument().addDocumentListener(listener);
        dialog.imageSourceField.getDocument().addDocumentListener(listener);
        dialog.coordinateReferenceField.getDocument().addDocumentListener(listener);
        dialog.legendTitleField.getDocument().addDocumentListener(listener);
        dialog.legendSubtitleField.getDocument().addDocumentListener(listener);
        dialog.templateCombo.addActionListener(e -> {
            LayoutTemplate template = (LayoutTemplate) dialog.templateCombo.getSelectedItem();
            dialog.interactionState.setTemplate(template);
            dialog.applyTemplateDefaults(template, true);
            dialog.statusLabel.setText("Plantilla activa: " + (template != null ? template.toString() : "Tecnica"));
            dialog.refreshPreviewWorkspace();
        });
        dialog.pageSizeCombo.addActionListener(e -> dialog.refreshPreviewWorkspace());
        dialog.orientationCombo.addActionListener(e -> dialog.refreshPreviewWorkspace());
        dialog.dpiCombo.addActionListener(e -> dialog.refreshPreviewWorkspace());
        dialog.legendPlacementCombo.addActionListener(e -> dialog.previewPanel.repaint());
        dialog.scaleStyleCombo.addActionListener(e -> dialog.previewPanel.repaint());
        dialog.scaleRuleCombo.addActionListener(e -> dialog.previewPanel.repaint());
        dialog.northStyleCombo.addActionListener(e -> dialog.previewPanel.repaint());
        dialog.northStyleCombo.addActionListener(e -> dialog.pushCatmapNorthSettingsToProject());
        dialog.northCheck.addActionListener(e -> {
            dialog.pushCatmapNorthSettingsToProject();
            dialog.refreshLayoutStructureTree();
            dialog.previewPanel.repaint();
        });
        dialog.scaleCheck.addActionListener(e -> {
            dialog.refreshLayoutStructureTree();
            dialog.previewPanel.repaint();
        });
        dialog.legendCheck.addActionListener(e -> {
            dialog.refreshLayoutStructureTree();
            dialog.previewPanel.repaint();
        });
        dialog.gridCheck.addActionListener(e -> dialog.previewPanel.repaint());
        dialog.gridLabelsCheck.addActionListener(e -> dialog.previewPanel.repaint());
        dialog.gridColumnsSpinner.addChangeListener(e -> dialog.previewPanel.repaint());
        dialog.gridRowsSpinner.addChangeListener(e -> dialog.previewPanel.repaint());
        dialog.layoutItemsList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            List<CatmapLayoutItem> selectedItems = dialog.layoutItemsList.getSelectedValuesList();
            CatmapLayoutItem selected = !selectedItems.isEmpty() ? selectedItems.get(0) : null;
            dialog.refreshInspectorFromSelection();
            if (selected != null) {
                dialog.interactionState.selectCustomItem(selected.getId());
                dialog.statusLabel.setText(selectedItems.size() > 1
                        ? selectedItems.size() + " elementos CATMAP seleccionados. Podes alinear, distribuir o editar el principal."
                        : "Elemento CATMAP seleccionado. Arrastralo o redimensionalo desde el layout.");
            } else if (dialog.interactionState.getSelectedElement() == LayoutElementType.CATMAP_ITEM) {
                dialog.interactionState.select(null);
            }
            dialog.previewPanel.repaint();
        });
        dialog.layoutItemsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && dialog.layoutItemsList.getSelectedValue() != null) {
                    dialog.editSelectedCatmapItem();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dialog.handleLayoutItemsListPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dialog.handleLayoutItemsListPopup(e);
            }
        });
        dialog.layoutStructureTree.addTreeSelectionListener(e -> dialog.handleLayoutStructureSelectionChanged());
        dialog.layoutStructureTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
                    dialog.handleLayoutStructureDoubleClick();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dialog.handleLayoutStructurePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dialog.handleLayoutStructurePopup(e);
            }
        });
        dialog.projectLayersList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            Layer selectedLayer = dialog.projectLayersList.getSelectedValue();
            dialog.refreshProjectLayerDetails();
            if (selectedLayer != null) {
                dialog.statusLabel.setText("Capa de proyecto seleccionada en CATMAP: " + selectedLayer.getName());
            }
        });
        dialog.projectLayersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = dialog.projectLayersList.locationToIndex(e.getPoint());
                if (index < 0) {
                    return;
                }
                Rectangle cellBounds = dialog.projectLayersList.getCellBounds(index, index);
                if (cellBounds == null || !cellBounds.contains(e.getPoint())) {
                    return;
                }
                Layer layer = dialog.projectLayersModel.get(index);
                if (layer == null) {
                    return;
                }
                dialog.projectLayersList.setSelectedIndex(index);
                int relativeX = e.getPoint().x - cellBounds.x;
                if (SwingUtilities.isLeftMouseButton(e) && relativeX <= 28) {
                    dialog.toggleProjectLayerVisibility(layer);
                    return;
                }
                if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
                    dialog.openProjectLayerAppearance(layer);
                }
            }
        });
        dialog.projectLayersList.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("SPACE"), "toggle-layer-visible");
        dialog.projectLayersList.getActionMap().put("toggle-layer-visible", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.toggleSelectedProjectLayerVisibility();
            }
        });
        dialog.projectLayersList.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "edit-layer-appearance");
        dialog.projectLayersList.getActionMap().put("edit-layer-appearance", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dialog.openSelectedProjectLayerAppearance();
            }
        });
    }
}
