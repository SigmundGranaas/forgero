package com.sigmundgranaas.forgero.client.model;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.texture.utils.Offset;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Unbaked2DTexturedModel implements UnbakedFabricModel {
    public static final String TRANSPARENT_BASE_IDENTIFIER = "transparent_base";
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private final List<PaletteTemplateModel> textures;
    private final Map<String, Offset> offsetMap;
    private final String id;
    private final ModelLoader loader;
    private final Function<SpriteIdentifier, Sprite> textureGetter;

    public Unbaked2DTexturedModel(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, List<PaletteTemplateModel> textures, String id) {
        this.loader = loader;
        this.textureGetter = textureGetter;
        this.textures = textures;
        this.id = id;
        this.offsetMap = new HashMap<>();
    }

    private String textureName(PaletteTemplateModel model) {
        return String.format("%s-%s", model.palette(), model.template().replace(".png", ""));
    }

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
        JsonObject jsonTextures = new JsonObject();
        if (this.textures.size() > 0) {
            for (int i = 0; i < this.textures.size(); i++) {
                var texture = textureName(this.textures.get(i));
                var layer = "layer" + i;
                this.offsetMap.put(layer, textures.get(i).getOffset().orElse(new Offset(0, 0)));
                jsonTextures.addProperty(layer, getTextureBasePath() + texture);
            }
        } else {
            jsonTextures.addProperty("layer" + 1, getTextureBasePath() + TRANSPARENT_BASE_IDENTIFIER);
        }

        return jsonTextures;
    }

    public JsonUnbakedModel buildUnbakedJsonModel() {
        return JsonUnbakedModel.deserialize(BuildJsonModel());
    }

    public String getIdentifier() {
        return id;
    }

    private void applyOffset(ModelElement element) {
        for (ModelElementFace face : element.faces.values()) {
            var offset = offsetMap.getOrDefault(face.textureId, new Offset(0, 0));
            if (offset.x() != 0 && offset.y() != 0) {
                element.from.add((float) offset.x(), (float) offset.y(), 0.001f);
                element.to.add((float) offset.x(), (float) offset.y(), 0.001f);
            }
            break;
        }
    }

    @Override
    public FabricBakedModel bake() {
        JsonUnbakedModel model = this.buildUnbakedJsonModel();
        ModelIdentifier id = this.getId();
        JsonUnbakedModel generated_model = ITEM_MODEL_GENERATOR.create(textureGetter, model);
        for (ModelElement element : generated_model.getElements()) {
            applyOffset(element);
        }
        var bakedModel = generated_model.bake(loader, model, textureGetter, ModelRotation.X0_Y0, id, true);
        //((GeneratedJsonLoader) loader).loadGeneratedJson(generated_model, id);
        return (FabricBakedModel) bakedModel;
    }

    @Override
    public ModelIdentifier getId() {
        return new ModelIdentifier(ForgeroInitializer.MOD_NAMESPACE, getIdentifier(), "inventory");
    }
}
