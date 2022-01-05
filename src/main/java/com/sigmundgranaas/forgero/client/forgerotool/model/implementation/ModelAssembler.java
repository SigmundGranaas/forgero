package com.sigmundgranaas.forgero.client.forgerotool.model.implementation;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolModelAssembler;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolModelBuilder;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelAssembler;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelBuilder;
import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolWithBinding;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings({"ClassCanBeRecord"})
public class ModelAssembler implements ToolModelAssembler, ToolPartModelAssembler {
    private final Function<String, FabricBakedModel> getModel;

    public ModelAssembler(Function<String, FabricBakedModel> getModel) {
        this.getModel = getModel;
    }

    @Override
    public FabricBakedModel assembleToolPartModel(ForgeroToolPart toolPart) {
        ToolPartModelBuilder builder = ToolPartModelBuilder.createBuilder();
        builder.addToolPart(getBaseModel(toolPart));
        if (!(toolPart.getPrimaryMaterial() instanceof EmptySecondaryMaterial)) {
            builder.addSecondaryMaterial(getSecondaryMaterialModel(toolPart));
        }
        return builder.build();
    }

    @Override
    public FabricBakedModel assembleToolPartModel(ForgeroToolTypes toolType, ForgeroToolPart toolPart) {
        ToolPartModelBuilder builder = ToolPartModelBuilder.createBuilder();
        builder.addToolPart(getBaseModel(toolType, toolPart));
        if (!(toolPart.getPrimaryMaterial() instanceof EmptySecondaryMaterial)) {
            builder.addSecondaryMaterial(getSecondaryMaterialModel(toolType, toolPart));
        }
        return builder.build();
    }

    private FabricBakedModel getBaseModel(ForgeroToolTypes type, ForgeroToolPart part) {
        return Optional.ofNullable(getModel.apply(ForgeroIdentifierFactory.INSTANCE.createToolPartModelIdentifier(type, part).getIdentifier())).orElse(new EmptyBakedModel());
    }

    private FabricBakedModel getSecondaryMaterialModel(ForgeroToolTypes type, ForgeroToolPart part) {
        return Optional.ofNullable(getModel.apply(ForgeroIdentifierFactory.INSTANCE.createToolPartModelIdentifier(type, part, part.getSecondaryMaterial()).getSecondaryMaterialIdentifier())).orElse(new EmptyBakedModel());
    }

    private FabricBakedModel getBaseModel(ForgeroToolPart part) {
        return Optional.ofNullable(getModel.apply(ForgeroIdentifierFactory.INSTANCE.createToolPartModelIdentifier(part).getIdentifier())).orElse(new EmptyBakedModel());
    }

    private FabricBakedModel getSecondaryMaterialModel(ForgeroToolPart part) {
        return Optional.ofNullable(getModel.apply(ForgeroIdentifierFactory.INSTANCE.createToolPartModelIdentifier(part, part.getSecondaryMaterial()).getSecondaryMaterialIdentifier())).orElse(new EmptyBakedModel());
    }

    @Override
    public FabricBakedModel assembleToolModel(ForgeroTool tool) {
        FabricBakedModel headModel = assembleToolPartModel(tool.getToolType(), tool.getToolHead());
        FabricBakedModel handleModel = assembleToolPartModel(tool.getToolType(), tool.getToolHandle());

        ToolModelBuilder builder = ToolModelBuilder
                .createToolModelBuilder()
                .addHead(headModel)
                .addHandle(handleModel);

        if (tool instanceof ForgeroToolWithBinding) {
            FabricBakedModel bindingModel = assembleToolPartModel(tool.getToolType(), ((ForgeroToolWithBinding) tool).getBinding());
            builder.addBinding(bindingModel);
        }

        return builder.buildModel();
    }
}
