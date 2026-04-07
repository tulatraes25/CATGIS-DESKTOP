package ar.com.catgis;

import javax.swing.Icon;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class CatgisModuleAction {

    private final String id;
    private final String name;
    private final String description;
    private final Icon icon;
    private final Runnable handler;
    private final ModuleActionPlacement placement;
    private final BooleanSupplier availability;

    public CatgisModuleAction(String id,
                              String name,
                              String description,
                              Icon icon,
                              Runnable handler,
                              ModuleActionPlacement placement,
                              BooleanSupplier availability) {
        this.id = Objects.requireNonNullElse(id, "");
        this.name = Objects.requireNonNullElse(name, "");
        this.description = Objects.requireNonNullElse(description, "");
        this.icon = icon;
        this.handler = handler;
        this.placement = placement != null ? placement : ModuleActionPlacement.MODULE_MENU;
        this.availability = availability;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Icon getIcon() {
        return icon;
    }

    public ModuleActionPlacement getPlacement() {
        return placement;
    }

    public boolean isAvailable() {
        return availability == null || availability.getAsBoolean();
    }

    public void run() {
        if (handler != null) {
            handler.run();
        }
    }
}
