package com.sigmundgranaas.forgero.fabric.mixins;

import net.minecraft.client.render.model.json.ItemModelGenerator;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemModelGenerator.class)
public class ItemModelGeneratorMixin {
	@Shadow
	@Final
	public static List<String> LAYERS;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(CallbackInfo ci) {
		if (LAYERS.size() == 5) {
			LAYERS.addAll(List.of("layer5", "layer6", "layer7", "layer8", "layer9"));
		}
	}
}
