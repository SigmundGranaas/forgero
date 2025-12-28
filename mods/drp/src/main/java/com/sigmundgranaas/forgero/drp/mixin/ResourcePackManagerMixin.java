package com.sigmundgranaas.forgero.drp.mixin;

import com.sigmundgranaas.forgero.drp.impl.pack.DRPResourcePackProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProvider;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Mixin that injects the DRP resource pack provider into Minecraft's ResourcePackManager.
 * <p>
 * This ensures DRP packs are available for both server data (tags, recipes) and client resources.
 */
@Mixin(ResourcePackManager.class)
public class ResourcePackManagerMixin {
	@Shadow
	@Final
	@Mutable
	private Set<ResourcePackProvider> providers;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(ResourcePackProvider[] providers, CallbackInfo ci) {
		Set<ResourcePackProvider> newProviders = new LinkedHashSet<>(this.providers);
		newProviders.add(new DRPResourcePackProvider());
		this.providers = newProviders;
	}
}
