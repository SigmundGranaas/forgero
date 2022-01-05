package com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
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
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

public class ToolModel2D implements DynamicModel {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    protected final ForgeroToolItem tool;

    public ToolModel2D(ForgeroToolItem tool) {
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
        String headTexture = getTextureBase() + tool.getTool().getToolHead().getPrimaryMaterial().getName() + "_" + ToolPartModelTypeToFilename(getHeadType());
        String handleTexture = getTextureBase() + tool.getTool().getToolHandle().getPrimaryMaterial().getName() + "_" + ToolPartModelTypeToFilename(getHandleType());
        textures.addProperty("layer0", handleTexture);
        textures.addProperty("layer1", getTextureBase() + "transparent_base");
        textures.addProperty("layer2", headTexture);
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
        return tool.getTool().getToolIdentifierString();
    }

    @Override
    public ModelIdentifier getModelIdentifier() {
        String itemPartModelIdentifier = itemPartModelIdentifier();
        return new ModelIdentifier(Forgero.MOD_NAMESPACE, itemPartModelIdentifier, "inventory");
    }

    public ToolPartModelType getHeadType() {
        //TODO add sword
        return switch (((ForgeroToolItem) tool).getToolType()) {
            case PICKAXE -> ToolPartModelType.PICKAXEHEAD;
            case SHOVEL -> ToolPartModelType.SHOVELHEAD;
            case SWORD -> ToolPartModelType.AXEHEAD;
        };
    }

    public String ToolPartModelTypeToFilename(ToolPartModelType modelType) {
        return modelType.toString().toLowerCase(Locale.ROOT).replace("_", "");
    }

    public ToolPartModelType getHandleType() {
        return switch (((ForgeroToolItem) tool).getToolType()) {
            case PICKAXE -> ToolPartModelType.FULLHANDLE;
            case SHOVEL -> ToolPartModelType.MEDIUMHANDLE;
            case SWORD -> ToolPartModelType.SHORTHANDLE;
        };
    }

    public ToolPartModelType getBindingType() {
        return switch (((ForgeroToolItem) tool).getToolType()) {
            case PICKAXE -> ToolPartModelType.PICKAXEBINDING;
            case SHOVEL -> ToolPartModelType.SHOVELBINDING;
            case SWORD -> ToolPartModelType.AXEHEAD;
        };
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
