package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.ModelAssembler;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;

import java.util.function.Function;

public interface ToolModelAssembler {
    static ToolModelAssembler createAssembler(Function<String, FabricBakedModel> modelGetter) {
        return new ModelAssembler(modelGetter);
    }

    FabricBakedModel assembleToolModel(ForgeroTool tool);
}