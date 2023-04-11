package com.sigmundgranaas.drp.mixin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.sigmundgranaas.drp.api.DynamicPackManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourceType;

@Mixin(ResourcePackManager.class)
public class ResourcePackManagerMixin {
	@Shadow
	@Final
	@Mutable
	private Set<ResourcePackProvider> providers;

	@Inject(method = "<init>*", at = @At("RETURN"))
	public void construct$forgero(ResourcePackProfile.Factory arg, ResourcePackProvider[] resourcePackProviders, CallbackInfo info) {
		providers = new LinkedHashSet<>(providers);

		List<ResourcePackProfile> packs = new ArrayList<>();
		DynamicPackManager.registerDynamicPacks(packs::add, ResourceType.SERVER_DATA);

		Function<ResourcePackProfile, ResourcePackProvider> packProvider = (ResourcePackProfile profile) -> (Consumer<ResourcePackProfile> consumer, ResourcePackProfile.Factory factory) -> consumer.accept(profile);
		packs.forEach(pack -> providers.add(packProvider.apply(pack)));
	}

}
