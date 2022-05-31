package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ClassCanBeRecord")
public class ForgeroModelVariantProvider implements ModelVariantProvider {
    private final UnbakedModelCollection collection;

    public ForgeroModelVariantProvider(UnbakedModelCollection collection) {
        this.collection = collection;
    }

    @Override
    public @Nullable
    UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) {
        if (modelId.getNamespace().equals(ForgeroInitializer.MOD_NAMESPACE) && !modelId.getPath().contains("transparent_base")) {
            String[] elements = modelId.getPath().split("_");

            if (elements.length > 1 && ForgeroToolTypes.isTool(elements[1])) {
                return new ToolModelVariant(collection);
            } else if (ToolPartModelType.isItemModelIdentifier(elements)) {
                return new ToolPartModelVariant(collection);
            }

        }
        return null;
    }
}
