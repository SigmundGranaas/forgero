package com.sigmundgranaas.forgero.minecraft.common.resources;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.minecraft.common.utils.RegistryUtils;
import lombok.Data;

import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.registry.Registry;

public class DisassemblyRecipeLoader {
	private static ImmutableList<DisassemblyRecipe> entries = ImmutableList.<DisassemblyRecipe>builder().build();

	public static ImmutableList<DisassemblyRecipe> getEntries() {
		return entries;
	}


	public static void reload(ResourceManager manager) {
		Gson gson = new Gson();
		List<DisassemblyRecipe> localEntries = new ArrayList<>();
		for (Resource res : manager.findResources("disassembly", path -> path.getPath().endsWith(".json")).values()) {
			try (InputStream stream = res.getInputStream()) {
				DisassemblyRecipeLoader.DisassemblyData data = gson.fromJson(new JsonReader(new InputStreamReader(stream)), DisassemblyRecipeLoader.DisassemblyData.class);
				DisassemblyRecipe.of(data).ifPresent(localEntries::add);
			} catch (Exception e) {
				Forgero.LOGGER.error(e);
			}
		}
		entries = ImmutableList.copyOf(localEntries);
	}

	@Data
	public static class DisassemblyData {
		private List<String> results;
		private String input;
	}

	@Data
	@SuppressWarnings("ClassCanBeRecord")
	public static class DisassemblyRecipe {
		private final List<Item> results;
		private final Ingredient input;


		public static Optional<DisassemblyRecipe> of(DisassemblyData data) {
			Optional<Ingredient> input = RegistryUtils.safeId(data.getInput())
					.flatMap(id -> RegistryUtils.safeRegistryLookup(Registry.ITEM, id))
					.map(Ingredient::ofItems);

			List<Item> results = data.results.stream()
					.map(RegistryUtils::safeId)
					.flatMap(Optional::stream)
					.map(id -> RegistryUtils.safeRegistryLookup(Registry.ITEM, id))
					.flatMap(Optional::stream)
					.toList();

			if (input.isPresent() && results.size() == data.results.size()) {
				return Optional.of(new DisassemblyRecipe(results, input.get()));
			} else {
				return Optional.empty();
			}
		}
	}
}
