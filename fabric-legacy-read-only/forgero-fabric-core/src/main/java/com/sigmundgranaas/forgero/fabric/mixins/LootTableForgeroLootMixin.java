package com.sigmundgranaas.forgero.fabric.mixins;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.fabric.initialization.datareloader.ForgeroLootInjectionReloader;

import net.minecraft.loot.LootDataType;
import net.minecraft.resource.ResourceReloader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.loot.LootManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootManager.class)
abstract class LootTableForgeroLootMixin {

	@Inject(method = "reload", at = @At("HEAD"))
	private void reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		new ForgeroLootInjectionReloader().reload(manager);
	}
}
