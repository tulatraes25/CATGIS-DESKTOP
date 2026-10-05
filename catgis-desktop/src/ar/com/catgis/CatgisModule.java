package ar.com.catgis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CatgisModule {

    private final String id;
    private final String name;
    private final ModuleCategory category;
    private final String description;
    private final String kosmoOrigin;
    private final boolean core;
    private boolean enabled;
    private final List<CatgisModuleAction> actions = new ArrayList<>();

    public CatgisModule(String id,
                        String name,
                        ModuleCategory category,
                        String description,
                        String kosmoOrigin,
                        boolean core,
                        boolean enabled) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.kosmoOrigin = kosmoOrigin;
        this.core = core;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getKosmoOrigin() {
        return kosmoOrigin;
    }

    public boolean isCore() {
        return core;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<CatgisModuleAction> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public CatgisModule addAction(CatgisModuleAction action) {
        if (action != null) {
            actions.add(action);
        }
        return this;
    }
}
