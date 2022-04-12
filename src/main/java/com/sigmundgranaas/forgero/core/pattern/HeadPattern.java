package com.sigmundgranaas.forgero.core.pattern;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

import java.util.List;

public class HeadPattern extends Pattern {
    private final ForgeroToolTypes toolType;

    public HeadPattern(ForgeroToolPartTypes type, String name, List<Property> properties, ForgeroToolTypes toolType, int rarity, String model) {
        super(type, name, properties, rarity, model);
        this.toolType = toolType;
    }

    @Override
    public String getPatternIdentifier() {
        return String.format("%shead_pattern_%s", toolType.getToolName(), getName());
    }

    public ForgeroToolTypes getToolType() {
        return toolType;
    }
}
