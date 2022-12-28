package com.sigmundgranaas.forgerofabric.mixins;

import net.minecraft.client.render.model.json.JsonUnbakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(JsonUnbakedModel.class)
public interface JsonUnbakedModelOverrideMixin {
    @Accessor
    void setParent(JsonUnbakedModel parent);
}
