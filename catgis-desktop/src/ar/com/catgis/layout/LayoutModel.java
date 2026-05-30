package ar.com.catgis.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LayoutModel {

    private final List<LayoutElement> elements = new ArrayList<>();

    public void addElement(LayoutElement e) {
        if (e != null) elements.add(e);
    }

    public void removeElement(String id) {
        elements.removeIf(e -> e.getId().equals(id));
    }

    public List<LayoutElement> getElements() {
        return new ArrayList<>(elements);
    }

    public List<LayoutElement> getVisibleElementsSortedByZ() {
        return elements.stream()
                .filter(LayoutElement::isVisible)
                .sorted(Comparator.comparingInt(LayoutElement::getZOrder))
                .collect(Collectors.toList());
    }

    public LayoutElement findElementAtMm(double xMm, double yMm) {
        List<LayoutElement> sorted = getVisibleElementsSortedByZ();
        for (int i = sorted.size() - 1; i >= 0; i--) {
            LayoutElement e = sorted.get(i);
            if (!e.isLocked() && e.containsMm(xMm, yMm)) {
                return e;
            }
        }
        return null;
    }

    public void moveToFront(LayoutElement e) {
        int maxZ = elements.stream().mapToInt(LayoutElement::getZOrder).max().orElse(0);
        e.setZOrder(maxZ + 1);
    }

    public void moveToBack(LayoutElement e) {
        int minZ = elements.stream().mapToInt(LayoutElement::getZOrder).min().orElse(0);
        e.setZOrder(minZ - 1);
    }

    public void moveUp(LayoutElement e) { e.setZOrder(e.getZOrder() + 1); }
    public void moveDown(LayoutElement e) { e.setZOrder(e.getZOrder() - 1); }

    public void clearSelection() {
        for (LayoutElement e : elements) e.setSelected(false);
    }

    public LayoutElement getSelected() {
        return elements.stream().filter(LayoutElement::isSelected).findFirst().orElse(null);
    }

    public int size() { return elements.size(); }

    public int nextZ() {
        return elements.stream().mapToInt(LayoutElement::getZOrder).max().orElse(0) + 1;
    }
}
