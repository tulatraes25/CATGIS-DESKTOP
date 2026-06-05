package ar.com.catgis.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LayoutModel {

    private final List<LayoutElement> elements = new ArrayList<>();

    // --- Undo / Redo ---

    private static final int MAX_UNDO = 100;
    private final List<String> undoStack = new ArrayList<>();
    private final List<String> redoStack = new ArrayList<>();

    /**
     * Save a snapshot BEFORE making a change. Call this manually before
     * operations that modify elements in place (move, resize, text change).
     * addElement / removeElement call it automatically.
     */
    public void saveSnapshot() {
        undoStack.add(serializeElements());
        if (undoStack.size() > MAX_UNDO) undoStack.remove(0);
        redoStack.clear();
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    public void undo() {
        if (undoStack.isEmpty()) return;
        redoStack.add(serializeElements());
        String snapshot = undoStack.remove(undoStack.size() - 1);
        restoreFromSnapshot(snapshot);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        undoStack.add(serializeElements());
        String snapshot = redoStack.remove(redoStack.size() - 1);
        restoreFromSnapshot(snapshot);
    }

    private String serializeElements() {
        StringBuilder sb = new StringBuilder();
        for (LayoutElement e : elements) {
            sb.append(ar.com.catgis.catmap.CatmapSerializer.serializeElementRaw(e)).append("\n");
        }
        return sb.toString();
    }

    private void restoreFromSnapshot(String data) {
        elements.clear();
        for (String line : data.split("\n")) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            LayoutElement e = ar.com.catgis.catmap.CatmapSerializer.parseElementRaw(t);
            if (e != null) elements.add(e);
        }
    }

    // --- Element management ---

    public void addElement(LayoutElement e) {
        if (e == null) return;
        saveSnapshot();
        elements.add(e);
    }

    public void removeElement(String id) {
        saveSnapshot();
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

    public LayoutElement findHoverAtMm(double xMm, double yMm) {
        return findTopmostElementAtMm(xMm, yMm);
    }

    public LayoutElement findTopmostElementAtMm(double xMm, double yMm) {
        List<LayoutElement> sorted = getVisibleElementsSortedByZ();
        double toleranceMm = 5.0;
        for (int i = sorted.size() - 1; i >= 0; i--) {
            LayoutElement e = sorted.get(i);
            if (e.containsMm(xMm, yMm)) return e;
        }
        for (int i = sorted.size() - 1; i >= 0; i--) {
            LayoutElement e = sorted.get(i);
            double ex = e.getBoundsMm().x, ey = e.getBoundsMm().y;
            double ew = e.getBoundsMm().width, eh = e.getBoundsMm().height;
            double minW = Math.max(ew, toleranceMm), minH = Math.max(eh, toleranceMm);
            double extX = ex - (minW - ew) / 2;
            double extY = ey - (minH - eh) / 2;
            if (xMm >= extX && xMm <= extX + minW && yMm >= extY && yMm <= extY + minH) return e;
        }
        return null;
    }

    public void moveToFront(LayoutElement e) {
        normalizeZOrder();
        e.setZOrder(elements.stream().mapToInt(LayoutElement::getZOrder).max().orElse(0) + 1);
    }

    public void moveToBack(LayoutElement e) {
        normalizeZOrder();
        e.setZOrder(0);
        for (LayoutElement other : elements) if (other != e && other.getZOrder() > 0) other.setZOrder(other.getZOrder() + 1);
    }

    public void moveUp(LayoutElement e) {
        int current = e.getZOrder();
        for (LayoutElement other : elements) {
            if (other != e && other.getZOrder() == current + 1) {
                other.setZOrder(current);
                e.setZOrder(current + 1);
                return;
            }
        }
        e.setZOrder(current + 1);
    }

    public void moveDown(LayoutElement e) {
        int current = e.getZOrder();
        if (current <= 0) return;
        for (LayoutElement other : elements) {
            if (other != e && other.getZOrder() == current - 1) {
                other.setZOrder(current);
                e.setZOrder(current - 1);
                return;
            }
        }
        e.setZOrder(current - 1);
    }

    private void normalizeZOrder() {
        List<LayoutElement> sorted = getVisibleElementsSortedByZ();
        sorted.addAll(elements.stream().filter(el -> !el.isVisible()).collect(java.util.stream.Collectors.toList()));
        for (int i = 0; i < sorted.size(); i++) sorted.get(i).setZOrder(i);
    }

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

    // Multi-page support
    private int currentPage = 0;
    private final List<java.util.List<LayoutElement>> pages = new java.util.ArrayList<>();
    {
        pages.add(elements); // page 0 uses the main elements list
    }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int p) { currentPage = Math.max(0, Math.min(p, pages.size() - 1)); }
    public int getPageCount() { return pages.size(); }

    public void addPage() {
        pages.add(new java.util.ArrayList<>());
        currentPage = pages.size() - 1;
    }

    public List<LayoutElement> getCurrentPageElements() {
        if (currentPage == 0) return elements;
        return pages.get(currentPage);
    }
}
