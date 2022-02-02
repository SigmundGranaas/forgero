package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.ToolPartModelFactoryImpl;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartCollection;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.Map;
import java.util.function.Function;

public interface ToolPartModelFactory {
    static ToolPartModelFactory createFactory(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter) {
        return new ToolPartModelFactoryImpl(loader, textureGetter, ForgeroToolPartCollection.INSTANCE.getToolParts(), MaterialCollection.INSTANCE.getMaterialsAsList());
    }

    Map<String, FabricBakedModel> createToolPartModels();
}
