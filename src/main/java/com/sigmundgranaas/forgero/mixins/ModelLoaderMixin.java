package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.GeneratedJsonLoader;
import com.sigmundgranaas.forgero.utils.Utils;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin implements GeneratedJsonLoader {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Final
    @Shadow
    private Set<Identifier> modelsToLoad;

    @Final
    @Shadow
    private Map<Identifier, UnbakedModel> unbakedModels;


    @Inject(method = "loadModelFromJson", at = @At("HEAD"), cancellable = true)
    public void loadModelFromJson(Identifier id, CallbackInfoReturnable<JsonUnbakedModel> cir) {
        boolean isForgero = id.getNamespace().equals(Forgero.MOD_NAMESPACE);
        if (isForgero && !id.getPath().startsWith("item/pickaxe") && !id.getPath().startsWith("item/shovel")) {
            String modelParent = "item/generated";
            String modelJson = Utils.createModelJson(id.getPath(), modelParent);
            JsonUnbakedModel model = JsonUnbakedModel.deserialize(modelJson);
            cir.setReturnValue(model);
            cir.cancel();
        }
    }

    @Override
    public void loadGeneratedJson(JsonUnbakedModel unbakedModel, ModelIdentifier id) {
        this.unbakedModels.put(id, unbakedModel);
        this.unbakedModels.put(new Identifier(id.getNamespace(), "item/" + id.getPath()), unbakedModel);
        this.modelsToLoad.addAll(unbakedModel.getModelDependencies());
    }
}
