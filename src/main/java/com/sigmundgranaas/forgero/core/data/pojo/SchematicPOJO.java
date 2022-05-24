package com.sigmundgranaas.forgero.core.data.pojo;

import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;

public class SchematicPOJO extends ForgeroDataResource {
    public ForgeroToolPartTypes type;
    public ForgeroToolTypes toolType;
    public String model;
    public int materialCount;

    @Override
    public String getName() {
        if (toolType == null) {
            return super.getName() + "_" + type.getName();
        } else {
            return super.getName() + "_" + toolType.getToolName() + type.getName();
        }
    }
}
