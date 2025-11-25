package model.servlet;

import java.util.List;

public class CompositeFunctionStructure {
    private String type;
    private String name;
    private String displayName;
    private String description;
    private List<FunctionBlock> blocks;

    // Конструкторы
    public CompositeFunctionStructure() {}

    public CompositeFunctionStructure(String type, String name, String displayName,
                                      String description, List<FunctionBlock> blocks) {
        this.type = type;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.blocks = blocks;
    }

    // Геттеры и сеттеры
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<FunctionBlock> getBlocks() { return blocks; }
    public void setBlocks(List<FunctionBlock> blocks) { this.blocks = blocks; }
}