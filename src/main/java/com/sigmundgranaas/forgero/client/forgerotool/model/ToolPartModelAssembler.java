package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.ModelAssembler;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;

import java.util.function.Function;

public interface ToolPartModelAssembler {
    static ToolPartModelAssembler createAssembler(Function<String, FabricBakedModel> modelGetter) {
        return new ModelAssembler(modelGetter);
    }

    FabricBakedModel assembleToolPartModel(ForgeroToolPart toolPart);

    FabricBakedModel assembleToolPartModel(ForgeroToolTypes toolType, ForgeroToolPart toolPart);
}
