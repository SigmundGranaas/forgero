package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.GeneratedJsonLoader;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Set;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin implements GeneratedJsonLoader {

    @Final
    @Shadow
    private Set<Identifier> modelsToLoad;

    @Final
    @Shadow
    private Map<Identifier, UnbakedModel> unbakedModels;


    @Override
    public void loadGeneratedJson(JsonUnbakedModel unbakedModel, ModelIdentifier id) {
        this.unbakedModels.put(id, unbakedModel);
        this.unbakedModels.put(new Identifier(id.getNamespace(), "item/" + id.getPath()), unbakedModel);
        this.modelsToLoad.addAll(unbakedModel.getModelDependencies());
    }
}
