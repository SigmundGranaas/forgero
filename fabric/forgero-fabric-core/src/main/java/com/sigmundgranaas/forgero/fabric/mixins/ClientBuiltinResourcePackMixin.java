package com.sigmundgranaas.forgero.fabric.mixins;

import java.util.function.Consumer;

import com.sigmundgranaas.forgero.minecraft.common.dynamic.DynamicPackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;

@Mixin(ClientBuiltinResourcePackProvider.class)
public class ClientBuiltinResourcePackMixin {
	@Inject(method = "register", at = @At("RETURN"))
	private void addDynamicResourcePacks$forgero(Consumer<ResourcePackProfile> consumer, ResourcePackProfile.Factory factory, CallbackInfo ci) {
		DynamicPackManager.registerDynamicPacks(consumer, ResourceType.CLIENT_RESOURCES);
	}
}
