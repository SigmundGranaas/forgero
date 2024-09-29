package com.sigmundgranaas.forgero.generator.mixin;

import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.abstractions.utils.ModLoaderUtils;
import com.sigmundgranaas.forgero.generator.impl.DataDirectoryRecipeGenerator;
import com.sigmundgranaas.forgero.generator.impl.IdentifiedJson;
import com.sigmundgranaas.forgero.generator.impl.RecipeGenerator;
import com.sigmundgranaas.forgero.generator.impl.Registries;
import com.sigmundgranaas.forgero.generator.impl.ResourceManagerJsonLoader;
import com.sigmundgranaas.forgero.generator.impl.StringReplacer;
import com.sigmundgranaas.forgero.generator.impl.VariableToMapTransformer;

import org.jetbrains.annotations.NotNull;
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
	public void forgero$injectDynamicRecipes(@NotNull Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
		RecipeGenerator recipeGenerator = new RecipeGenerator(
				new StringReplacer(Registries.operationRegistry()::convert),
				new VariableToMapTransformer(Registries.variableConverterRegistry()::convert),
				ModLoaderUtils::isModPresent
		);
		DataDirectoryRecipeGenerator generator = new DataDirectoryRecipeGenerator(
				"recipe_generators", new ResourceManagerJsonLoader(resourceManager), recipeGenerator);
		Map<Identifier, JsonObject> recipes = generator.generate().stream()
		                                               .collect(Collectors.toMap(IdentifiedJson::id, IdentifiedJson::json));
		map.putAll(recipes);
	}
}
