package com.sigmundgranaas.forgero.generator.mixin;

import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.generator.impl.DataDirectoryRecipeGenerator;
import com.sigmundgranaas.forgero.generator.impl.IdentifiedJson;
import com.sigmundgranaas.forgero.generator.impl.Registries;
import com.sigmundgranaas.forgero.generator.impl.ResourceManagerJsonLoader;
import com.sigmundgranaas.forgero.generator.impl.StringReplacer;
import com.sigmundgranaas.forgero.generator.impl.VariableToMapTransformer;
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
		StringReplacer stringReplacer = new StringReplacer(Registries.operationRegistry()::convert);
		VariableToMapTransformer transformer = new VariableToMapTransformer(Registries.variableConverterRegistry()::convert);
		DataDirectoryRecipeGenerator generator = new DataDirectoryRecipeGenerator(stringReplacer, transformer, "recipe_generators", new ResourceManagerJsonLoader(resourceManager));

		Map<Identifier, JsonObject> recipes = generator.generate().stream()
				.collect(Collectors.toMap(IdentifiedJson::id, IdentifiedJson::json));

		map.putAll(recipes);

	}
}
