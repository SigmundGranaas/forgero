package com.sigmundgranaas.forgero.client.forgerotool.model.implementation;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelFactory;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.SecondaryMaterial2DModel;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.Unbaked2DToolPartModel;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.binding.BindingModel2D;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.handle.HandleModel2D;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.head.HeadModel2D;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartBinding;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.*;
import java.util.function.Function;


public class ToolPartModelFactoryImpl implements ToolPartModelFactory {
    private final Map<String, FabricBakedModel> toolPartModels;
    private final ModelLoader loader;
    private final Function<SpriteIdentifier, Sprite> textureGetter;
    private final List<ForgeroToolPart> toolParts;
    private final List<ForgeroMaterial> materials;

    public ToolPartModelFactoryImpl(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, List<ForgeroToolPart> toolParts, List<ForgeroMaterial> materials) {
        this.loader = loader;
        this.textureGetter = textureGetter;
        this.toolParts = toolParts;
        this.materials = materials;
        this.toolPartModels = new HashMap<>();
    }

    @Override
    public Map<String, FabricBakedModel> createToolPartModels() {
        if (toolPartModels.isEmpty()) {
            createModels();
        }
        return toolPartModels;
    }

    private void createModels() {
        List<Unbaked2DToolPartModel> models = new ArrayList<>();

        Arrays.stream(ToolPartModelType.values()).forEach(modelType -> materials.stream().filter(material -> material instanceof SecondaryMaterial).map(SecondaryMaterial.class::cast).forEach(secondaryMaterial -> models.add(new SecondaryMaterial2DModel(loader, textureGetter, secondaryMaterial, modelType))));

        toolParts.forEach(toolpart -> {
            models.add(createModelsFromToolPart(toolpart, ToolPartModelType.getModelType(toolpart)));
            Arrays.stream(ForgeroToolTypes.values()).forEach(toolTypes -> models.add(createModelsFromToolPart(toolpart, ToolPartModelType.getModelType(toolpart, toolTypes))));
        });

        models.forEach(model -> toolPartModels.put(model.getIdentifier(), model.bake()));
    }

    private Unbaked2DToolPartModel createModelsFromToolPart(ForgeroToolPart toolPart, ToolPartModelType modelType) {
        return switch (toolPart.getToolPartType()) {
            case HEAD -> createToolPartHead((ToolPartHead) toolPart, modelType);
            case HANDLE -> createToolPartHandle((ToolPartHandle) toolPart, modelType);
            case BINDING -> createToolPartBinding((ToolPartBinding) toolPart, modelType);
        };
    }

    private Unbaked2DToolPartModel createToolPartHandle(ToolPartHandle handle, ToolPartModelType modelType) {
        return new HandleModel2D(loader, textureGetter, handle, modelType);
    }

    private Unbaked2DToolPartModel createToolPartHead(ToolPartHead head, ToolPartModelType modelType) {
        return new HeadModel2D(loader, textureGetter, head);
    }

    private Unbaked2DToolPartModel createToolPartBinding(ToolPartBinding binding, ToolPartModelType modelType) {
        return new BindingModel2D(loader, textureGetter, binding, modelType);
    }
}
