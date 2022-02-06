package com.sigmundgranaas.forgero.client.forgerotool.model.implementation;

import com.sigmundgranaas.forgero.client.forgerotool.model.*;
import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolWithBinding;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
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
        if (!(toolPart.getSecondaryMaterial() instanceof EmptySecondaryMaterial)) {
            builder.addSecondaryMaterial(getSecondaryMaterialModel(toolPart));
        }
        if (!(toolPart.getGem() instanceof EmptyGem)) {
            builder.addGem(getGemModel(toolPart));
        }
        return builder.build();
    }

    @Override
    public FabricBakedModel assembleToolPartModel(ForgeroToolTypes toolType, ForgeroToolPart toolPart) {
        ToolPartModelBuilder builder = ToolPartModelBuilder.createBuilder();
        builder.addToolPart(getBaseModel(toolType, toolPart));
        if (!(toolPart.getSecondaryMaterial() instanceof EmptySecondaryMaterial)) {
            builder.addSecondaryMaterial(getSecondaryMaterialModel(toolType, toolPart, toolPart.getSecondaryMaterial()));
        }
        if (!(toolPart.getGem() instanceof EmptyGem)) {
            builder.addGem(getGemModel(toolType, toolPart));
        }
        return builder.build();
    }

    private FabricBakedModel getGemModel(ForgeroToolTypes toolType, ForgeroToolPart toolPart) {
        return Optional.ofNullable(getModel.apply(ToolPartModelType.getModelType(toolPart, toolType).toFileName() + "_gem")).orElse(new EmptyBakedModel());
    }

    private FabricBakedModel getGemModel(ForgeroToolPart toolPart) {
        return Optional.ofNullable(getModel.apply(ToolPartModelType.getModelType(toolPart).toFileName() + "_gem")).orElse(new EmptyBakedModel());
    }

    private FabricBakedModel getBaseModel(ForgeroToolTypes type, ForgeroToolPart part) {
        return Optional.ofNullable(getModel.apply(ForgeroIdentifierFactory.INSTANCE.createToolPartModelIdentifier(type, part).getIdentifier())).orElse(new EmptyBakedModel());
    }

    private FabricBakedModel getSecondaryMaterialModel(ForgeroToolTypes type, ForgeroToolPart part, SecondaryMaterial secondaryMaterial) {
        return Optional.ofNullable(getModel.apply(ForgeroIdentifierFactory.INSTANCE.createToolPartModelIdentifier(type, part, secondaryMaterial).getIdentifier())).orElse(new EmptyBakedModel());
    }

    private FabricBakedModel getBaseModel(ForgeroToolPart part) {
        return Optional.ofNullable(getModel.apply(ForgeroIdentifierFactory.INSTANCE.createToolPartModelIdentifier(part).getIdentifier())).orElse(new EmptyBakedModel());
    }

    private FabricBakedModel getSecondaryMaterialModel(ForgeroToolPart part) {
        return Optional.ofNullable(getModel.apply(ForgeroIdentifierFactory.INSTANCE.createToolPartModelIdentifier(part, part.getSecondaryMaterial()).getIdentifier())).orElse(new EmptyBakedModel());
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
