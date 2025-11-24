package model.servlet;

import java.util.List;

// Структура составной функции
public class CompositeFunctionStructure {
    private String type;
    private String name;
    private String displayName;
    private String description;
    private List<FunctionBlock> blocks;

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<FunctionBlock> getBlocks() {
        return blocks;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBlocks(List<FunctionBlock> blocks) {
        this.blocks = blocks;
    }

    // Геттеры и сеттеры
}
