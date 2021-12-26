package com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.toolpart.ForgeroToolPartItemImpl;
import net.minecraft.client.render.model.*;
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

public abstract class AbstractDynamicModel implements DynamicModel {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    private final ForgeroToolPartItemImpl toolpart;

    public AbstractDynamicModel(ForgeroToolPartItemImpl toolpart) {
        this.toolpart = toolpart;
    }

    protected ForgeroToolPartItemImpl getToolpart() {
        return toolpart;
    }

    @Override
    public String BuildJsonModel() {
        JsonObject binding = new JsonObject();
        binding.add("textures", this.getTextures());
        binding.add("elements", this.getElements());
        binding.addProperty("gui_light", "front");
        return binding.toString();
    }

    @Override
    public JsonUnbakedModel buildUnbakedJsonModel() {
        return JsonUnbakedModel.deserialize(BuildJsonModel());
    }

    @Override
    public ModelIdentifier getModelIdentifier() {
        return new ModelIdentifier(Forgero.MOD_NAMESPACE, itemPartModelIdentifier(), "inventory");
    }

    abstract protected JsonArray getElements();

    abstract protected JsonObject getTextures();

    @Override
    public String itemPartModelIdentifier() {
        return toolpart.getToolPartTypeAndMaterialLowerCase() + "_toolpart";
    }

    protected String getTexture() {
        return Forgero.MOD_NAMESPACE + ":item/" + itemPartModelIdentifier().replace("_toolpart", "");
    }

    @Override
    @Nullable
    public Collection<Identifier> getModelDependencies() {
        return null;
    }

    @Override
    @Nullable
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        return null;
    }

    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        LOGGER.debug("baking: {}", this.getModelIdentifier());
        JsonUnbakedModel model = this.buildUnbakedJsonModel();
        ModelIdentifier id = this.getModelIdentifier();
        ((GeneratedJsonLoader) loader).loadGeneratedJson(model, id);
        return loader.bake(this.getModelIdentifier(), ModelRotation.X0_Y0);
    }
}
