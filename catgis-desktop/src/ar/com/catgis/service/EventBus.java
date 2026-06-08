package ar.com.catgis.service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * Lightweight Event Bus for decoupling CATGIS components.
 * <p>
 * Replaces direct calls to {@code CatgisDesktopApp.mapPanel} etc.
 * Components publish events; subscribers react without knowing each other.
 * </p>
 * <pre>
 *   // Subscribe
 *   EventBus.on(EventBus.EventType.LAYER_SELECTED, e -> {
 *       Layer layer = (Layer) e.data;
 *       quickStylePanel.setLayer(layer);
 *   });
 *
 *   // Publish
 *   EventBus.emit(EventBus.EventType.LAYER_SELECTED, selectedLayer);
 * </pre>
 */
public final class EventBus {

    private EventBus() {}

    /** All event types in the system. */
    public enum EventType {
        /** Layer added to project. Data: Layer. */
        LAYER_ADDED,
        /** Layer removed from project. Data: Layer. */
        LAYER_REMOVED,
        /** Layer visibility toggled. Data: Layer. */
        LAYER_VISIBILITY_CHANGED,
        /** Selected layer changed. Data: Layer (or null if none). */
        LAYER_SELECTED,
        /** Layer style changed (color, width, etc.). Data: Layer. */
        LAYER_STYLE_CHANGED,
        /** Layer order changed (drag reorder). Data: null. */
        LAYER_ORDER_CHANGED,
        /** Project opened or created. Data: Project. */
        PROJECT_CHANGED,
        /** Map extent changed (zoom, pan). Data: Envelope. */
        MAP_EXTENT_CHANGED,
        /** Project has unsaved changes. Data: Boolean. */
        PROJECT_DIRTY_STATE_CHANGED,
        /** Measurement started/ended. Data: Boolean (active). */
        MEASUREMENT_ACTIVE,
        /** Tool mode changed. Data: String (tool name). */
        TOOL_MODE_CHANGED,
        /** Feature selected. Data: SimpleFeature. */
        FEATURE_SELECTED,
        /** Selection cleared. Data: null. */
        SELECTION_CLEARED,
        /** Status message. Data: String. */
        STATUS_MESSAGE,
        /** Sync floating vector edit toolbar. Data: null. */
        TOOLBAR_SYNC
    }

    /** An event with type and optional data. */
    public record Event(EventType type, Object data) {
        /** Cast data to the expected type. */
        @SuppressWarnings("unchecked")
        public <T> T getData() { return (T) data; }
    }

    private static final EnumMap<EventType, List<Consumer<Event>>> listeners = new EnumMap<>(EventType.class);

    static {
        for (EventType t : EventType.values()) {
            listeners.put(t, new ArrayList<>());
        }
    }

    /** Subscribe to an event type. Returns a Runnable that removes the listener. */
    public static Runnable on(EventType type, Consumer<Event> handler) {
        synchronized (listeners) {
            listeners.get(type).add(handler);
        }
        return () -> {
            synchronized (listeners) {
                listeners.get(type).remove(handler);
            }
        };
    }

    /** Convenience: subscribe with a direct data consumer. */
    public static <T> Runnable subscribe(EventType type, java.util.function.Consumer<T> handler) {
        return on(type, e -> handler.accept(e.getData()));
    }

    /** Emit an event to all subscribers. */
    public static void emit(EventType type, Object data) {
        Event event = new Event(type, data);
        List<Consumer<Event>> copy;
        synchronized (listeners) {
            copy = new ArrayList<>(listeners.get(type));
        }
        for (Consumer<Event> handler : copy) {
            try {
                handler.accept(event);
            } catch (Exception e) {
                System.err.println("[EventBus] Error in " + type + " handler: " + e.getMessage());
            }
        }
    }

    /** Emit with null data. */
    public static void emit(EventType type) {
        emit(type, null);
    }

    /** Remove all listeners (for cleanup). */
    public static void clear() {
        synchronized (listeners) {
            for (EventType t : EventType.values()) {
                listeners.get(t).clear();
            }
        }
    }
}
