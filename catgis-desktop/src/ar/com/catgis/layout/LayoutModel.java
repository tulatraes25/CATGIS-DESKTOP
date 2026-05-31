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
        return findTopmostElementAtMm(xMm, yMm);
    }

    public LayoutElement findTopmostElementAtMm(double xMm, double yMm) {
        List<LayoutElement> sorted = getVisibleElementsSortedByZ();
        double toleranceMm = 5.0;
        for (int i = sorted.size() - 1; i >= 0; i--) {
            LayoutElement e = sorted.get(i);
            double ex = e.getBoundsMm().x, ey = e.getBoundsMm().y;
            double ew = e.getBoundsMm().width, eh = e.getBoundsMm().height;
            double minW = Math.max(ew, toleranceMm), minH = Math.max(eh, toleranceMm);
            double extX = ex - (minW - ew) / 2;
            double extY = ey - (minH - eh) / 2;
            if (xMm >= extX && xMm <= extX + minW && yMm >= extY && yMm <= extY + minH) {
                return e;
            }
        }
        return null;
    }

    public LayoutElement findHoverAtMm(double xMm, double yMm) {
        return findTopmostElementAtMm(xMm, yMm);
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
