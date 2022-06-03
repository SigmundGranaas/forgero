package com.sigmundgranaas.forgero.core.data.v1.pojo;

import com.sigmundgranaas.forgero.core.data.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import org.jetbrains.annotations.NotNull;

public class SchematicPojo extends ForgeroDataResource {
    @NotNull
    public ForgeroToolPartTypes type;
    public ForgeroToolTypes toolType;
    //public String model;
    public int materialCount;
    public ModelPojo model;
    public boolean unique = false;

    @Override
    public String getName() {
        if (toolType == null) {
            return super.getName() + "_" + type.getName();
        } else {
            return super.getName() + "_" + toolType.getToolName() + type.getName();
        }
    }
}
