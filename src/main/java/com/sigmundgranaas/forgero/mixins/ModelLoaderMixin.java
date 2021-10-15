package com.sigmundgranaas.forgero.mixins;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.utils.Utils;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {
    @Inject(method = "loadModelFromJson", at = @At("HEAD"), cancellable = true)
    public void loadModelFromJson(Identifier id, CallbackInfoReturnable<JsonUnbakedModel> cir) {
        boolean isForgero = id.getNamespace().equals(Forgero.MOD_NAMESPACE);
        if (isForgero && !id.getPath().startsWith("item/pickaxe") && !id.getPath().startsWith("item/shovel")) {
            String modelParent = "minecraft:item/generated";
            String json = Utils.createModelJson(id.getPath(), modelParent);
            JsonUnbakedModel model = JsonUnbakedModel.deserialize(json);
            model.id = id.toString();
            cir.setReturnValue(model);
            cir.cancel();
        }
    }
}
