package com.sigmundgranaas.forgero.recipe.customrecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.Upgradeable;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedSchematicPart;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.service.StateService;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

public class SchematicPartRecipe extends ShapelessRecipe {
	private final StateService service;

	public SchematicPartRecipe(ShapelessRecipe recipe, StateService service) {
		super(recipe.getId(), recipe.getGroup(),recipe.getCategory(), recipe.getOutput(null), recipe.getIngredients());
		this.service = service;
	}

	private List<State> partsFromCraftingInventory(RecipeInputInventory craftingInventory) {
		List<ItemStack> ingredients = new ArrayList<>();
		for (int i = 0; i < craftingInventory.size(); i++) {
			var stack = craftingInventory.getStack(i);
			if (this.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(ingredient -> ingredient.test(stack))) {
				int finalI = i;
				if (ingredients.stream().noneMatch(item -> item.isOf(craftingInventory.getStack(finalI).getItem()))) {
					ingredients.add(craftingInventory.getStack(i));
				}
			}
		}
		return ingredients.stream().map(service::convert).flatMap(Optional::stream).toList();
	}

	@Override
	public ItemStack craft(RecipeInputInventory craftingInventory, DynamicRegistryManager registryManager) {
		var target = service.convert(this.getOutput(null));
		if (target.isPresent()) {
			var targetState = target.get();
			var parts = partsFromCraftingInventory(craftingInventory);
			Optional<ConstructedSchematicPart.SchematicPartBuilder> part = ConstructedSchematicPart.SchematicPartBuilder.builder(parts);
			if (part.isPresent() && targetState instanceof Upgradeable<?> upgradeable) {
				var builder = part.get();
				builder.id(targetState.identifier());
				builder.addUpgrades(upgradeable.slots());
				builder.type(targetState.type());
				return service.convert(builder.build()).orElse(ItemStack.EMPTY);
			}

		}
		return getOutput(registryManager).copy();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SchematicPartRecipeSerializer.INSTANCE;
	}

	public static class SchematicPartRecipeSerializer extends Serializer implements ForgeroRecipeSerializer {
		public static final SchematicPartRecipeSerializer INSTANCE = new SchematicPartRecipeSerializer();

		@Override
		public RecipeSerializer<?> getSerializer() {
			return INSTANCE;
		}

		@Override
		public SchematicPartRecipe read(Identifier identifier, JsonObject jsonObject) {
			return new SchematicPartRecipe(super.read(identifier, jsonObject), StateService.INSTANCE);
		}

		@Override
		public SchematicPartRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
			return new SchematicPartRecipe(super.read(identifier, packetByteBuf), StateService.INSTANCE);
		}

		@Override
		public Identifier getIdentifier() {
			return new Identifier(Forgero.NAMESPACE, RecipeTypes.SCHEMATIC_PART_CRAFTING.getName());
		}
	}
}
