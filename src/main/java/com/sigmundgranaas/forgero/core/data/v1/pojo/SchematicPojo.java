package com.sigmundgranaas.forgero.core.data.v1.pojo;

import com.sigmundgranaas.forgero.core.data.v1.ForgeroDataResource;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SchematicPojo extends ForgeroDataResource {
    @NotNull
    public ForgeroToolPartTypes type;
    public ForgeroToolTypes toolType;
    //public String model;
    public int materialCount;
    @NotNull
    public List<ModelContainerPojo> models = new ArrayList<>();
    public boolean unique = false;

    @Override
    public String getName() {
        return super.getName();
    }
}
