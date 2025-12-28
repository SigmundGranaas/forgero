package com.sigmundgranaas.forgero.common.recipe;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ForgeroShapedRecipe extends ShapedRecipe {
	private static final Logger LOGGER = LoggerFactory.getLogger(ForgeroShapedRecipe.class);
	final Map<Character, RecipeIngredient> forgeroKey;
	final RecipeOutput forgeroResult;
	private final RecipeServices services;

	public ForgeroShapedRecipe(
			Identifier id,
			String group,
			CraftingRecipeCategory category,
			int width,
			int height,
			DefaultedList<Ingredient> input,
			ItemStack output,
			Map<Character, RecipeIngredient> forgeroKey,
			RecipeOutput forgeroResult,
			RecipeServices services
	) {
		super(id, group, category, width, height, input, output);
		this.forgeroKey = forgeroKey;
		this.forgeroResult = forgeroResult;
		this.services = services;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ForgeroShapedRecipeSerializer.INSTANCE;
	}

	@Override
	public ItemStack craft(RecipeInputInventory recipeInputInventory, DynamicRegistryManager dynamicRegistryManager) {
		try {
			Component finalComponent = assemble(recipeInputInventory);
			return services.converter().toStack(finalComponent).orElse(ItemStack.EMPTY);
		} catch (Exception e) {
			// Log error and return empty to prevent crash
			LOGGER.error("Error crafting Forgero item for recipe {}", this.getId(), e);
			return ItemStack.EMPTY;
		}
	}

	private Component assemble(RecipeInputInventory inventory) {
		ComponentRegistry registry = services.registry();
		ComponentConverter converter = services.converter();
		ComponentMutater mutater = services.mutater();

		// 1. Get the base component from the registry
		OpenIdentifier baseComponentId = OpenIdentifier.parse(forgeroResult.item());
		Component baseComponent = registry.get(baseComponentId)
				.orElseThrow(() -> new IllegalStateException("Base component not found in registry: " + baseComponentId));

		// 2. Build a declarative Mutation object from the recipe definition
		ComponentMutater.Mutation.Builder mutationBuilder = new ComponentMutater.Mutation.Builder();

		forgeroResult.structure().ifPresent(structure ->
				structure.forEach((slotId, source) -> {
					Component part = resolveIngredient(source, converter, inventory);
					mutationBuilder.withStructure(OpenIdentifier.parse(slotId), part);
				}));

		forgeroResult.upgrades().ifPresent(upgrades ->
				upgrades.forEach(upgrade -> {
					Component upgradeComponent = resolveIngredient(upgrade.component(), converter, inventory);
					mutationBuilder.withUpgrade(OpenIdentifier.parse(upgrade.slot()), upgradeComponent);
				}));

		forgeroResult.properties().ifPresent(mutationBuilder::withProperties);

		// 3. Apply the mutation
		return mutater.apply(baseComponent, mutationBuilder.build());
	}

	private Component resolveIngredient(String source, ComponentConverter converter, RecipeInputInventory inventory) {
		ComponentRegistry registry = services.registry();
		// Check if source is a single character key in the recipe map
		if (source.length() == 1 && forgeroKey.containsKey(source.charAt(0))) {
			char recipeKey = source.charAt(0);
			RecipeIngredient ingredient = forgeroKey.get(recipeKey);
			return findFirstMatchingComponent(ingredient, converter, inventory);
		} else {
			// Otherwise, assume it's a direct component ID
			return registry.get(OpenIdentifier.parse(source))
					.orElseThrow(() -> new IllegalStateException("Component not found in registry: " + source));
		}
	}

	private Component findFirstMatchingComponent(RecipeIngredient ingredient, ComponentConverter converter, RecipeInputInventory inventory) {
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			if (stack.isEmpty()) continue;

			boolean itemMatch = ingredient.item()
					.map(item -> Registries.ITEM.getId(stack.getItem()).toString().equals(item))
					.orElse(false);

			boolean tagMatch = ingredient.tag()
					.map(tag -> stack.isIn(TagKey.of(Registries.ITEM.getKey(), new Identifier(tag))))
					.orElse(false);

			if (itemMatch || tagMatch) {
				return converter.toComponent(stack)
						.orElseThrow(() -> new IllegalStateException("Could not convert ingredient ItemStack to Component: " + stack));
			}
		}
		throw new IllegalStateException("No matching item found in inventory for ingredient: " + ingredient);
	}
}
