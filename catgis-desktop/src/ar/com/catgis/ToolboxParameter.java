package ar.com.catgis;

public class ToolboxParameter {

    private final String name;
    private final String label;
    private final ToolboxParameterType type;
    private final boolean required;
    private final String description;

    public ToolboxParameter(String name,
                            String label,
                            ToolboxParameterType type,
                            boolean required,
                            String description) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.required = required;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public ToolboxParameterType getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDescription() {
        return description;
    }
}
