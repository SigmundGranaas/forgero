package com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class ToolModel2D implements DynamicModel {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    protected final ForgeroTool tool;

    public ToolModel2D(ForgeroTool tool) {
        this.tool = tool;
    }

    @Override
    public String BuildJsonModel() {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "builtin/generated");
        model.add("textures", this.getTextures());
        model.addProperty("gui_light", "front");
        return model.toString();
    }

    protected JsonElement getTextures() {
        JsonObject textures = new JsonObject();
        String headTexture = getTextureBase() + tool.getToolHead().getToolPartTypeAndMaterialLowerCase();
        String handleTexture = getTextureBase() + tool.getToolHandle().getToolPartTypeAndMaterialLowerCase();
        textures.addProperty("layer0", handleTexture);
        textures.addProperty("layer1", headTexture);
        textures.addProperty("particle", headTexture);
        return textures;
    }

    protected String getTextureBase() {
        return Forgero.MOD_NAMESPACE + ":item/";
    }


    @Override
    public JsonUnbakedModel buildUnbakedJsonModel() {
        return JsonUnbakedModel.deserialize(BuildJsonModel());
    }

    @Override
    public String itemPartModelIdentifier() {
        return tool.getIdentifier().getPath();
    }

    @Override
    public ModelIdentifier getModelIdentifier() {
        return new ModelIdentifier(Forgero.MOD_NAMESPACE, itemPartModelIdentifier(), "inventory");
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return null;
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return null;
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        JsonUnbakedModel model = this.buildUnbakedJsonModel();
        ModelIdentifier id = (ModelIdentifier) this.tool.getIdentifier();
        JsonUnbakedModel generated_model = ITEM_MODEL_GENERATOR.create(textureGetter, model);
        ((GeneratedJsonLoader) loader).loadGeneratedJson(generated_model, id);
        return loader.bake(id, ModelRotation.X0_Y0);
    }
}
