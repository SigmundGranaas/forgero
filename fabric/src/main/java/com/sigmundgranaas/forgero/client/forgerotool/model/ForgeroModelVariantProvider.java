package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.ForgeroRegistry;
import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.model.ModelRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import org.jetbrains.annotations.Nullable;

import static com.sigmundgranaas.forgero.identifier.texture.toolpart.ToolPartModelTextureIdentifier.DEFAULT_SPLIT_OPERATOR;

public class ForgeroModelVariantProvider implements ModelVariantProvider {
    private final ToolModelVariant toolModelVariant;
    private final ToolPartModelVariant toolPartModelVariant;
    private final CompositeModelVariant compositeModel;

    public ForgeroModelVariantProvider(UnbakedModelCollection collection, ModelRegistry modelRegistry) {
        this.toolModelVariant = new ToolModelVariant(collection);
        this.toolPartModelVariant = new ToolPartModelVariant(collection);
        this.compositeModel = new CompositeModelVariant(collection, modelRegistry);
    }

    @Override
    public @Nullable
    UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) {
        if (ForgeroStateRegistry.COMPOSITES.contains(String.format("%s:%s", modelId.getNamespace(), modelId.getPath()))) {
            return compositeModel;
        }

        if (modelId.getNamespace().equals(ForgeroInitializer.MOD_NAMESPACE) && !modelId.getPath().contains("transparent_base")) {
            String[] elements = modelId.getPath().split(DEFAULT_SPLIT_OPERATOR);
            if (elements.length > 1 && ForgeroRegistry.TOOL.resourceExists(modelId.getPath())) {
                return toolModelVariant;
            } else if (elements.length == 2 && ForgeroRegistry.TOOL_PART.resourceExists(modelId.getPath())) {
                return toolPartModelVariant;
            }

        }
        return null;
    }

}
