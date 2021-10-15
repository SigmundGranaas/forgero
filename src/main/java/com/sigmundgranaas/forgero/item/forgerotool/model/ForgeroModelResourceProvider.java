package com.sigmundgranaas.forgero.item.forgerotool.model;

import com.sigmundgranaas.forgero.Forgero;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import org.jetbrains.annotations.Nullable;

public class ForgeroModelResourceProvider implements ModelVariantProvider {
    private final ForgeroPartModels partModels;

    public ForgeroModelResourceProvider(ForgeroPartModels partModels) {
        this.partModels = partModels;
    }


    @Override
    public @Nullable UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) throws ModelProviderException {
        if (modelId.getNamespace().equals(Forgero.MOD_NAMESPACE) && modelId.getPath().startsWith("pickaxe") || modelId.getPath().startsWith("shovel")) {
            return new ForgeroToolModel(partModels);
        }
        return null;
    }
}
