package ar.com.catgis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ToolboxAlgorithm {

    private final String id;
    private final String name;
    private final String category;
    private final String description;
    private final List<ToolboxInputType> inputTypes = new ArrayList<>();
    private final List<ToolboxParameter> parameters = new ArrayList<>();
    private final ToolboxOutputType outputType;
    private final String assistantOperation;

    public ToolboxAlgorithm(String id,
                            String name,
                            String category,
                            String description,
                            ToolboxOutputType outputType,
                            String assistantOperation) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.outputType = outputType;
        this.assistantOperation = assistantOperation;
    }

    public ToolboxAlgorithm addInput(ToolboxInputType inputType) {
        if (inputType != null) {
            inputTypes.add(inputType);
        }
        return this;
    }

    public ToolboxAlgorithm addParameter(ToolboxParameter parameter) {
        if (parameter != null) {
            parameters.add(parameter);
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public List<ToolboxInputType> getInputTypes() {
        return Collections.unmodifiableList(inputTypes);
    }

    public List<ToolboxParameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public ToolboxOutputType getOutputType() {
        return outputType;
    }

    public String getAssistantOperation() {
        return assistantOperation;
    }
}
