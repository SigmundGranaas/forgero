package com.sigmundgranaas.forgero.mixin;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.mod.RecipeGenPlugin;

import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
public class RecipeInjectionMixin {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("Forgero-RecipeInjection");

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("HEAD"))
	public void forgero$injectDynamicRecipes(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
		Map<Identifier, JsonObject> generatedRecipes = RecipeGenPlugin.getGeneratedRecipes();
		
		if (generatedRecipes.isEmpty()) {
			LOGGER.debug("No generated recipes to inject");
			return;
		}

		int beforeCount = map.size();
		map.putAll(generatedRecipes);
		int afterCount = map.size();
		int injected = afterCount - beforeCount;

		LOGGER.info("Injected {} Forgero recipes into RecipeManager (total recipes: {})", injected, afterCount);
	}
}
