package com.sigmundgranaas.forgero.common.recipe;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.impl.*;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;

import com.sigmundgranaas.forgero.core.recipe.ForgeroEnvironment;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import net.minecraft.inventory.CraftingInventory;
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
import org.slf4j.LoggerFactory;

import java.util.*;

public class ForgeroShapedRecipe extends ShapedRecipe {
	final Map<Character, RecipeIngredient> forgeroKey;
	final RecipeOutput forgeroResult;

	public ForgeroShapedRecipe(Identifier id, String group, CraftingRecipeCategory category, int width, int height, DefaultedList<Ingredient> input, ItemStack output, Map<Character, RecipeIngredient> forgeroKey, RecipeOutput forgeroResult) {
		super(id, group, category, width, height, input, output);
		this.forgeroKey = forgeroKey;
		this.forgeroResult = forgeroResult;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return ForgeroShapedRecipeSerializer.INSTANCE;
	}

	@Override
	public ItemStack craft(RecipeInputInventory recipeInputInventory, DynamicRegistryManager dynamicRegistryManager) {
		try {
			Component finalComponent = assemble(recipeInputInventory);
			return ForgeroEnvironment.getComponentConverter().toStack(finalComponent).orElse(ItemStack.EMPTY);
		} catch (Exception e) {
			// Log error and return empty to prevent crash
			LoggerFactory.getLogger(ForgeroShapedRecipe.class).error("Error crafting Forgero item for recipe " + this.getId(), e);
			return ItemStack.EMPTY;
		}
	}

	private Component assemble(RecipeInputInventory inventory) {
		ComponentRegistry registry = ForgeroEnvironment.getComponentRegistry();
		ComponentConverter converter = ForgeroEnvironment.getComponentConverter();
		ComponentMutater mutater = ForgeroEnvironment.getComponentMutater();

		OpenIdentifier baseComponentId = new OpenIdentifier(forgeroResult.item());
		Component baseComponent = registry.get(baseComponentId)
				.orElseThrow(() -> new IllegalStateException("Base component not found in registry: " + baseComponentId));

		Component processedComponent = baseComponent;

		if (forgeroResult.structure().isPresent()) {
			processedComponent = applyStructure(processedComponent, mutater, converter, inventory);
		}
		if (forgeroResult.upgrades().isPresent()) {
			processedComponent = applyUpgrades(processedComponent, mutater, converter, inventory);
		}
		if (forgeroResult.properties().isPresent()) {
			processedComponent = applyProperties(processedComponent);
		}

		return processedComponent;
	}

	private Component applyStructure(Component base, ComponentMutater mutater, ComponentConverter converter, RecipeInputInventory inventory) {
		Component current = base;
		for (Map.Entry<String, String> entry : forgeroResult.structure().get().entrySet()) {
			OpenIdentifier slotId = new OpenIdentifier(entry.getKey());
			Component part = resolveIngredient(entry.getValue(), converter, inventory);
			current = mutater.setSlot(current, slotId, part);
		}
		return current;
	}

	private Component applyUpgrades(Component base, ComponentMutater mutater, ComponentConverter converter, RecipeInputInventory inventory) {
		Component current = base;
		for (RecipeUpgrade upgrade : forgeroResult.upgrades().get()) {
			OpenIdentifier slotId = new OpenIdentifier(upgrade.slot());
			Component upgradeComponent = resolveIngredient(upgrade.component(), converter, inventory);
			current = mutater.setSlot(current, slotId, upgradeComponent);
		}
		return current;
	}

	private Component resolveIngredient(String source, ComponentConverter converter, RecipeInputInventory inventory) {
		ComponentRegistry registry = ForgeroEnvironment.getComponentRegistry();
		if (source.length() == 1 && forgeroKey.containsKey(source.charAt(0))) {
			char recipeKey = source.charAt(0);
			RecipeIngredient ingredient = forgeroKey.get(recipeKey);
			return findFirstMatchingComponent(ingredient, converter, inventory);
		} else {
			return registry.get(new OpenIdentifier(source))
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

	private Component applyProperties(Component component) {
		Map<String, List<?>> recipeProperties = forgeroResult.properties().get();
		Map<String, List<?>> newProperties = new HashMap<>(component.propertiesAsMap());

		recipeProperties.forEach((key, value) -> newProperties.merge(key, value, (existing, incoming) -> {
			List<Object> merged = new ArrayList<>(existing);
			merged.addAll(incoming);
			return merged;
		}));

		return reconstructComponent(component, Collections.unmodifiableMap(newProperties));
	}

	private Component reconstructComponent(Component original, Map<String, List<?>> newProperties) {
		if (original instanceof StaticComponent sc) {
			return new StaticComponent(sc.id(), sc.tags(), newProperties);
		} else if (original instanceof StaticEquipment se) {
			return new StaticEquipment(se.id(), se.tags(), newProperties);
		} else if (original instanceof ExtensiblePart ep) {
			return new ExtensiblePart(ep.id(), ep.tags(), newProperties, ep.upgrades());
		} else if (original instanceof ExtensibleEquipment ee) {
			return new ExtensibleEquipment(ee.id(), ee.tags(), newProperties, ee.upgrades());
		} else if (original instanceof StructuredPart sp) {
			return new StructuredPart(sp.id(), sp.tags(), newProperties, sp.structure());
		} else if (original instanceof StructuredEquipment se) {
			return new StructuredEquipment(se.id(), se.tags(), newProperties, se.structure());
		} else if (original instanceof StructuredExtensiblePart sep) {
			return new StructuredExtensiblePart(sep.id(), sep.tags(), newProperties, sep.structure(), sep.upgrades());
		} else if (original instanceof StructuredExtensibleEquipment see) {
			return new StructuredExtensibleEquipment(see.id(), see.tags(), newProperties, see.structure(), see.upgrades());
		}
		throw new IllegalStateException("Unknown component type, cannot apply new properties: " + original.getClass().getName());
	}
}
