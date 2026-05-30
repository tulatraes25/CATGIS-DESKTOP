/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.dnd;

public class DragAndDropLock {
    private static boolean locked = false;
    private static boolean startedDnD = false;
    private static boolean resizing = false;

    public static boolean isLocked() {
        return locked;
    }

    public static void setLocked(boolean isLocked) {
        locked = isLocked;
    }

    public static boolean isDragAndDropStarted() {
        return startedDnD;
    }

    public static void setDragAndDropStarted(boolean dndStarted) {
        startedDnD = dndStarted;
    }

    public static boolean isResizing() {
        return resizing;
    }

    public static void setResizing(boolean isResizing) {
        resizing = isResizing;
    }
}

