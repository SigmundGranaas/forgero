package com.sigmundgranaas.forgero.client.forgerotool.model.implementation;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelFactory;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.Basic2DToolPartModel;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.Unbaked2DToolPartModel;
import com.sigmundgranaas.forgero.client.texture.FabricTextureIdentifierFactory;
import com.sigmundgranaas.forgero.core.skin.ForgeroToolPartTextureRegistry;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


public class ToolPartModelFactoryImpl implements ToolPartModelFactory {
    private final Map<String, FabricBakedModel> toolPartModels;
    private final ModelLoader loader;
    private final Function<SpriteIdentifier, Sprite> textureGetter;


    public ToolPartModelFactoryImpl(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter) {
        this.loader = loader;
        this.textureGetter = textureGetter;
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

        ForgeroToolPartTextureRegistry.getInstance(new FabricTextureIdentifierFactory()).getTextures().stream().map(texture -> new Basic2DToolPartModel(loader, textureGetter, texture)).forEach(models::add);


        models.forEach(model -> toolPartModels.put(model.getIdentifier(), model.bake()));
    }
}
