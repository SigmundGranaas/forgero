package com.sigmundgranaas.forgero.client.forgerotool.model.toolpart;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.math.Direction;

import java.util.Random;
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

    public String getTextureIdentifier() {
        return getIdentifier();
    }

    public abstract int getModelLayerIndex();

    public String BuildJsonModel() {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "minecraft:item/handheld");
        model.add("textures", this.getTextures());
        model.addProperty("gui_light", "front");
        return model.toString();
    }

    protected String getTextureBasePath() {
        return ForgeroInitializer.MOD_NAMESPACE + ":item/";
    }

    protected JsonObject getTextures() {
        JsonObject textures = new JsonObject();

        for (int i = 0; i <= getModelLayerIndex(); i++) {
            if (i == getModelLayerIndex()) {
                textures.addProperty("layer" + i, getTextureBasePath() + getIdentifier());
            } else {
                textures.addProperty("layer" + i, getTextureBasePath() + TRANSPARENT_BASE_IDENTIFIER);
            }
        }
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
        var elements = generated_model.getElements();
        for (ModelElement element : elements) {
            if (element.faces.containsKey(Direction.WEST)) {
                float rand = new Random().nextFloat();
                element.from.add(rand * -0.01f, 0, 0);
                element.to.add(rand * -0.01f, 0, 0);
            }
            if (element.faces.containsKey(Direction.EAST)) {
                float rand = new Random().nextFloat();
                element.from.add(rand * 0.01f, 0, 0);
                element.to.add(rand * 0.01f, 0, 0);
            }
            if (element.faces.containsKey(Direction.UP)) {
                float rand = new Random().nextFloat();
                element.from.add(0, rand * -0.01f, 0);
                element.to.add(0, rand * -0.01f, 0);
            }
            if (element.faces.containsKey(Direction.DOWN)) {
                float rand = new Random().nextFloat();
                element.from.add(0, rand * 0.01f, 0);
                element.to.add(0, rand * 0.01f, 0);
            }
        }
        //((GeneratedJsonLoader) loader).loadGeneratedJson(generated_model, id);
        return (FabricBakedModel) generated_model.bake(loader, model, textureGetter, ModelRotation.X0_Y0, id, true);
    }

    @Override
    public ModelIdentifier getId() {
        return new ModelIdentifier(ForgeroInitializer.MOD_NAMESPACE, getIdentifier(), "inventory");
    }
}
