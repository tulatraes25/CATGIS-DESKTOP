package ar.com.catgis.layout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel displaying layout elements as a list with z-order indicators.
 * Supports selection, reordering via buttons, and context menu delegation.
 */
public class ElementListPanel extends JPanel {

    private final LayoutModel model;
    private final DefaultListModel<LayoutElement> listModel = new DefaultListModel<>();
    private final JList<LayoutElement> elementList = new JList<>(listModel);

    public ElementListPanel(LayoutModel model) {
        this.model = model;
        setLayout(new BorderLayout());
        elementList.setCellRenderer(new ElementCellRenderer());
        elementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(elementList), BorderLayout.CENTER);
    }

    /**
     * Rebuild the list from the model.
     */
    public void refresh() {
        LayoutElement selected = elementList.getSelectedValue();
        listModel.clear();
        for (LayoutElement el : model.getElements()) {
            listModel.addElement(el);
        }
        if (selected != null && listModel.contains(selected)) {
            elementList.setSelectedValue(selected, true);
        }
    }

    public LayoutElement getSelectedElement() {
        return elementList.getSelectedValue();
    }

    public JList<LayoutElement> getList() { return elementList; }

    private static class ElementCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                       boolean selected, boolean focus) {
            super.getListCellRendererComponent(list, value, index, selected, focus);
            if (value instanceof LayoutElement el) {
                setText(el.getName());
                setIcon(el.isVisible() ? visibleIcon() : hiddenIcon());
                if (el.isLocked()) setForeground(Color.GRAY);
            }
            return this;
        }
        private Icon visibleIcon() { return UIManager.getIcon("FileView.fileIcon"); }
        private Icon hiddenIcon() { return UIManager.getIcon("FileView.hardDriveIcon"); }
    }
}
