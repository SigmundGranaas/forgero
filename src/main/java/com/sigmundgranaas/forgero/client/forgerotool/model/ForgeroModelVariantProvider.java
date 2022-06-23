package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import org.jetbrains.annotations.Nullable;

import static com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartModelTextureIdentifier.DEFAULT_SPLIT_OPERATOR;

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
            String[] elements = modelId.getPath().split(DEFAULT_SPLIT_OPERATOR);

            if (elements.length > 1 && ForgeroRegistry.TOOL.resourceExists(modelId.getPath())) {
                return new ToolModelVariant(collection);
            } else if (elements.length == 2 && ForgeroRegistry.TOOL_PART.resourceExists(modelId.getPath())) {
                return new ToolPartModelVariant(collection);
            }

        }
        return null;
    }
}
