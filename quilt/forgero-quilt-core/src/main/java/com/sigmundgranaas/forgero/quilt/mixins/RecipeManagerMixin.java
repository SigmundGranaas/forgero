package com.sigmundgranaas.forgero.quilt.mixins;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.fabric.registry.RecipeRegistry;
import com.sigmundgranaas.forgero.fabric.resources.RecipeDeletionReloader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

/**
 * RecipeManager mixin
 * <p>
 * Forgero does not come with pre-made recipes for tools and tool parts.
 * Recipes are generated from templates at startup.
 */
@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
	private final List<String> vanillaMaterials = List.of("wooden", "stone", "iron", "golden", "diamond", "netherite");

	private final List<String> vanillaTools = List.of("pickaxe", "shovel", "axe", "sword", "hoe");

	@Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("HEAD"))
	public void interceptApply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
		RecipeRegistry.INSTANCE.registerRecipes(map);
		if (ForgeroConfigurationLoader.configuration.convertVanillaRecipesToForgeroTools) {
			convertAllVanillaToolRecipes(map);
		}
		if (ForgeroConfigurationLoader.configuration.disableVanillaRecipes || ForgeroConfigurationLoader.configuration.disableVanillaTools) {
			removeAllVanillaToolRecipes(map);
		}
		if (ForgeroConfigurationLoader.configuration.enableCustomRecipeDeletion) {
			deleteRecipes(map, resourceManager);
		}
	}

	private void deleteRecipes(Map<Identifier, JsonElement> map, ResourceManager resourceManager) {
		RecipeDeletionReloader.reload(resourceManager);
		RecipeDeletionReloader.entries.forEach(map::remove);
	}

	private void removeAllVanillaToolRecipes(Map<Identifier, JsonElement> map) {
		vanillaMaterials.stream()
				.map(material -> vanillaTools.stream().map(tool -> new Identifier(String.format("%s_%s", material, tool))).toList())
				.flatMap(List::stream)
				.forEach(map::remove);
	}

	private void convertAllVanillaToolRecipes(Map<Identifier, JsonElement> map) {
		vanillaMaterials.stream()
				.map(material -> vanillaTools.stream().map(tool -> new Identifier(String.format("%s_%s", material, tool))).toList())
				.flatMap(List::stream)
				.map(id -> Optional.ofNullable(map.get(id)))
				.flatMap(Optional::stream)
				.forEach(this::convertMinecraftToForgeroNameSpace);
	}

	private void convertMinecraftToForgeroNameSpace(JsonElement recipe) {
		if (recipe instanceof JsonObject object && object.has("result") && object.get("result").getAsJsonObject().has("item")) {
			String newItem = resultItemRenamer(object.getAsJsonObject("result").get("item").getAsString());
			object.getAsJsonObject("result").addProperty("item", newItem);
		}
	}

	private String resultItemRenamer(String item) {
		String result = item;

		if (result.contains("minecraft")) {
			result = result.replace("minecraft", Forgero.NAMESPACE);
		}
		if (result.contains("golden")) {
			result = result.replace("golden", "gold");
		}
		if (result.contains("wooden")) {
			result = result.replace("wooden", "oak");
		}
		if (result.contains("_")) {
			result = result.replace("_", ELEMENT_SEPARATOR);
		}
		return result;
	}
}
