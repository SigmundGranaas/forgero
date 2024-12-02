package com.sigmundgranaas.forgero.dynamicresourcepack.mixin;

import java.util.function.Consumer;

import com.sigmundgranaas.forgero.dynamicresourcepack.event.ResourcePackEvent;

import net.minecraft.resource.FileResourcePackProvider;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;


@Mixin(FileResourcePackProvider.class)
public class FileResourcePackProviderMixin {
	@Shadow
	@Final
	private ResourceType type;

	@Inject(method = "register", at = @At("HEAD"))
	public void register(
			Consumer<ResourcePackProfile> adder,
			CallbackInfo ci
	) {
		ResourcePackEvent.registerDynamicPacks(adder, type);
	}
}
