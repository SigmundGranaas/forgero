package com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.Forgero;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.function.Function;

public abstract class Unbaked2DToolPartModel implements UnbakedToolPartModel {
    public static final String TRANSPARENT_BASE_IDENTIFIER = "transparent_base";
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private final ModelLoader loader;
    private final Function<SpriteIdentifier, Sprite> textureGetter;

    public Unbaked2DToolPartModel(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter) {
        this.loader = loader;
        this.textureGetter = textureGetter;
    }

    public abstract String getIdentifier();

    public abstract int getModelLayerIndex();

    public String BuildJsonModel() {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:item/handheld");
        model.add("textures", this.getTextures());
        model.addProperty("gui_light", "front");
        return model.toString();
    }

    protected String getTextureBasePath() {
        return Forgero.MOD_NAMESPACE + ":item/";
    }

    private JsonObject getTextures() {
        JsonObject textures = new JsonObject();
        for (int i = 0; i <= getModelLayerIndex(); i++) {
            if (i == getModelLayerIndex()) {
                textures.addProperty("layer" + i, getTextureBasePath() + getIdentifier());
            } else {
                textures.addProperty("layer" + i, getTextureBasePath() + TRANSPARENT_BASE_IDENTIFIER);
            }
        }
        textures.addProperty("particle", getTextureBasePath() + getIdentifier());
        return textures;
    }

    public JsonUnbakedModel buildUnbakedJsonModel() {
        return JsonUnbakedModel.deserialize(BuildJsonModel());
    }

    @Override
    public FabricBakedModel bake() {
        JsonUnbakedModel model = this.buildUnbakedJsonModel();
        ModelIdentifier id = this.getId();
        JsonUnbakedModel generated_model = ITEM_MODEL_GENERATOR.create(textureGetter, model);
        //((GeneratedJsonLoader) loader).loadGeneratedJson(generated_model, id);
        return (FabricBakedModel) generated_model.bake(loader, model, textureGetter, ModelRotation.X0_Y0, id, false);
    }

    @Override
    public ModelIdentifier getId() {
        return new ModelIdentifier(Forgero.MOD_NAMESPACE, getIdentifier(), "inventory");
    }
}
