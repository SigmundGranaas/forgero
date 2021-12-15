package com.sigmundgranaas.forgero.client.forgerotool.model;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import org.jetbrains.annotations.Nullable;

public record ForgeroToolModelProvider(
        ToolModelManager modelManager) implements ModelVariantProvider {

    @Override
    public @Nullable
    UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) {
        if (modelManager.isQualifiedModelManager(modelId)) {
            return new ForgeroToolModel(modelManager);
        }
        return null;
    }
}
