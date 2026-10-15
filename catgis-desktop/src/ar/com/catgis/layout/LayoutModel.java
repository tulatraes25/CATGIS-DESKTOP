package ar.com.catgis.layout;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LayoutModel {

    private final List<LayoutElement> elements = new ArrayList<>();

    // --- Undo / Redo ---

    private static final int MAX_UNDO = 100;
    // History belongs to an individual page. A switch cannot allow undo
    // to alter a different page's elements.
    private final List<List<String>> pageUndoStacks = new ArrayList<>();
    private final List<List<String>> pageRedoStacks = new ArrayList<>();

    private List<String> undoStack() { return pageUndoStacks.get(currentPage); }
    private List<String> redoStack() { return pageRedoStacks.get(currentPage); }

    private List<LayoutElement> activeElements() { return pages.get(currentPage); }

    private void requireActivePageElement(LayoutElement element) {
        if (element == null || !activeElements().contains(element)) {
            throw new IllegalArgumentException("CATMAP element does not belong to the active page.");
        }
    }

    /**
     * Save a snapshot BEFORE making a change. Call this manually before
     * operations that modify elements in place (move, resize, text change).
     * addElement / removeElement call it automatically.
     */
    public void saveSnapshot() {
        List<String> undo = undoStack();
        undo.add(serializeElements());
        if (undo.size() > MAX_UNDO) undo.remove(0);
        redoStack().clear();
    }

    public boolean canUndo() { return !undoStack().isEmpty(); }
    public boolean canRedo() { return !redoStack().isEmpty(); }

    public void undo() {
        List<String> undo = undoStack();
        if (undo.isEmpty()) return;
        redoStack().add(serializeElements());
        String snapshot = undo.remove(undo.size() - 1);
        restoreFromSnapshot(snapshot);
    }

    public void redo() {
        List<String> redo = redoStack();
        if (redo.isEmpty()) return;
        undoStack().add(serializeElements());
        String snapshot = redo.remove(redo.size() - 1);
        restoreFromSnapshot(snapshot);
    }

    private String serializeElements() {
        StringBuilder sb = new StringBuilder();
        for (LayoutElement e : activeElements()) {
            sb.append(ar.com.catgis.catmap.CatmapSerializer.serializeElementRaw(e)).append("\n");
        }
        return sb.toString();
    }

    private void restoreFromSnapshot(String data) {
        List<LayoutElement> active = activeElements();
        active.clear();
        for (String line : data.split("\n")) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            LayoutElement e = ar.com.catgis.catmap.CatmapSerializer.parseElementRaw(t);
            if (e != null) active.add(e);
        }
    }

    // --- Element management ---

    public void addElement(LayoutElement e) {
        if (e == null) return;
        saveSnapshot();
        activeElements().add(e);
    }

    public void removeElement(String id) {
        saveSnapshot();
        activeElements().removeIf(e -> e.getId().equals(id));
    }

    public List<LayoutElement> getElements() {
        return new ArrayList<>(activeElements());
    }

    public List<LayoutElement> getVisibleElementsSortedByZ() {
        return activeElements().stream()
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
        requireActivePageElement(e);
        normalizeZOrder();
        e.setZOrder(activeElements().stream().mapToInt(LayoutElement::getZOrder).max().orElse(0) + 1);
    }

    public void moveToBack(LayoutElement e) {
        requireActivePageElement(e);
        normalizeZOrder();
        e.setZOrder(0);
        for (LayoutElement other : activeElements()) if (other != e && other.getZOrder() > 0) other.setZOrder(other.getZOrder() + 1);
    }

    public void moveUp(LayoutElement e) {
        requireActivePageElement(e);
        int current = e.getZOrder();
        for (LayoutElement other : activeElements()) {
            if (other != e && other.getZOrder() == current + 1) {
                other.setZOrder(current);
                e.setZOrder(current + 1);
                return;
            }
        }
        e.setZOrder(current + 1);
    }

    public void moveDown(LayoutElement e) {
        requireActivePageElement(e);
        int current = e.getZOrder();
        if (current <= 0) return;
        for (LayoutElement other : activeElements()) {
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
        sorted.addAll(activeElements().stream().filter(el -> !el.isVisible()).collect(java.util.stream.Collectors.toList()));
        for (int i = 0; i < sorted.size(); i++) sorted.get(i).setZOrder(i);
    }

    public void clearSelection() {
        for (LayoutElement e : activeElements()) e.setSelected(false);
    }

    public LayoutElement getSelected() {
        return activeElements().stream().filter(LayoutElement::isSelected).findFirst().orElse(null);
    }

    public int size() { return activeElements().size(); }

    public int nextZ() {
        return activeElements().stream().mapToInt(LayoutElement::getZOrder).max().orElse(0) + 1;
    }

    // --- Grouping ---

    /**
     * Group the given elements under a new group ID.
     * Elements in a group should be moved/resized as a unit.
     * Returns the group ID, or null if fewer than 2 elements.
     */
    public String groupElements(List<LayoutElement> els) {
        if (els == null || els.size() < 2) return null;
        for (LayoutElement element : els) {
            requireActivePageElement(element);
        }
        saveSnapshot();
        String gid = "group-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        for (LayoutElement e : els) {
            e.setGroupId(gid);
        }
        return gid;
    }

    /** Remove grouping for the given group ID. */
    public void ungroupElements(String gid) {
        if (gid == null) return;
        saveSnapshot();
        for (LayoutElement e : activeElements()) {
            if (gid.equals(e.getGroupId())) {
                e.setGroupId(null);
            }
        }
    }

    /** Get all elements that belong to a group. */
    public List<LayoutElement> getGroupMembers(String gid) {
        if (gid == null) return java.util.Collections.emptyList();
        return activeElements().stream()
                .filter(e -> gid.equals(e.getGroupId()))
                .collect(Collectors.toList());
    }

    /** Get all elements that belong to ANY group. */
    public List<LayoutElement> getGroupedElements() {
        return activeElements().stream()
                .filter(e -> e.getGroupId() != null)
                .collect(Collectors.toList());
    }

    /** Get the group ID of the selected element, or null. */
    public String getSelectedGroupId() {
        LayoutElement sel = getSelected();
        return sel != null ? sel.getGroupId() : null;
    }

    /** Move all members of a group by delta (dx, dy) in mm. */
    public void translateGroup(String gid, double dx, double dy) {
        if (gid == null) return;
        for (LayoutElement e : activeElements()) {
            if (gid.equals(e.getGroupId())) {
                Rectangle2D.Double b = e.getBoundsMm();
                e.setBoundsMm(b.x + dx, b.y + dy, b.width, b.height);
            }
        }
    }

    // Multi-page support
    private int currentPage = 0;
    private final List<java.util.List<LayoutElement>> pages = new java.util.ArrayList<>();
    {
        pages.add(elements); // page 0 uses the main elements list
        pageUndoStacks.add(new ArrayList<>());
        pageRedoStacks.add(new ArrayList<>());
    }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int p) { currentPage = Math.max(0, Math.min(p, pages.size() - 1)); }
    public int getPageCount() { return pages.size(); }

    public void addPage() {
        if (pages.size() >= 200) {
            throw new IllegalStateException("CATMAP supports a maximum of 200 pages.");
        }
        pages.add(new java.util.ArrayList<>());
        pageUndoStacks.add(new ArrayList<>());
        pageRedoStacks.add(new ArrayList<>());
        currentPage = pages.size() - 1;
    }

    public List<LayoutElement> getCurrentPageElements() {
        if (currentPage == 0) return elements;
        return pages.get(currentPage);
    }

    /**
     * Snapshot an arbitrary page without switching the active editor page.
     * CATMAP persistence must not mutate the active page merely to save it.
     */
    public List<LayoutElement> getPageElements(int page) {
        if (page < 0 || page >= pages.size()) {
            throw new IndexOutOfBoundsException("CATMAP page index out of range: " + page);
        }
        return new ArrayList<>(pages.get(page));
    }
}
