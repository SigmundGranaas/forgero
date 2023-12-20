package com.sigmundgranaas.forgero.generator.mixin;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.generator.impl.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Mixin(RecipeManager.class)
public class RecipeInjectionMixin {

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("HEAD"))
	public void forgero$injectDynamicRecipes(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
		Registries.recipeProviderRegistry().values()
				.stream()
				.map(Supplier::get)
				.flatMap(Collection::stream)
				.forEach(recipe -> map.put(recipe.id(), recipe.json()));
	}
}
