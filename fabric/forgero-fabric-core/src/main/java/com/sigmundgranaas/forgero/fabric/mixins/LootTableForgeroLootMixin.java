package com.sigmundgranaas.forgero.fabric.mixins;

import java.util.Map;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.fabric.initialization.datareloader.ForgeroLootInjectionReloader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.loot.LootManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Mixin(LootManager.class)
abstract class LootTableForgeroLootMixin {

	@Inject(method = "apply*", at = @At("HEAD"))
	private void apply(Map<Identifier, JsonObject> jsonMap, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
		new ForgeroLootInjectionReloader().reload(resourceManager);
	}
}
