package com.sigmundgranaas.forgero.fabric.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;

import com.sigmundgranaas.forgero.fabric.initialization.datareloader.ForgeroLootInjectionReloader;

import net.fabricmc.fabric.api.loot.v2.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.fabricmc.fabric.impl.loot.LootUtil;

import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
@Mixin(LootManager.class)
abstract class LootTableForgeroLootMixin {

	@Inject(method = "apply*", at = @At("HEAD"))
	private void apply(Map<Identifier, JsonObject> jsonMap, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
		new ForgeroLootInjectionReloader().reload(resourceManager);
	}
}
