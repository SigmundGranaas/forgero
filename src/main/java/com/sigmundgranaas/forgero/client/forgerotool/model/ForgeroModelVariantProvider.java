package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.Forgero;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import org.jetbrains.annotations.Nullable;

public class ForgeroModelVariantProvider implements ModelVariantProvider {
    private final UnbakedModelCollection collection;

    public ForgeroModelVariantProvider(UnbakedModelCollection collection) {
        this.collection = collection;
    }

    @Override
    public @Nullable UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) throws ModelProviderException {
        if (modelId.getNamespace().equals(Forgero.MOD_NAMESPACE) && modelId.getPath().contains("transparent_base")) {
            String[] elements = modelId.getPath().split("_");
            if (elements.length > 3) {
                return new ToolModelVariant(collection);
            } else {
                return new ToolPartModelVariant(collection);
            }
        }
        return null;
    }
}
