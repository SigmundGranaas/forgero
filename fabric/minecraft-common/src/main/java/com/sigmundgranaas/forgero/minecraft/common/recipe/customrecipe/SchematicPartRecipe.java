package com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.Upgradeable;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedSchematicPart;
import com.sigmundgranaas.forgero.minecraft.common.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

import org.apache.commons.lang3.NotImplementedException;

public class SchematicPartRecipe extends ShapelessRecipe {
	private final StateService service;

	public SchematicPartRecipe(ShapelessRecipe recipe, StateService service) {
		super( recipe.getGroup(),recipe.getCategory(), recipe.getResult(null), recipe.getIngredients());
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
		var target = service.convert(this.getResult(null));
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
		return getResult(registryManager).copy();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SchematicPartRecipeSerializer.INSTANCE;
	}

	public static class SchematicPartRecipeSerializer implements RecipeSerializer<SchematicPartRecipe> , ForgeroRecipeSerializer {
		public static final SchematicPartRecipeSerializer INSTANCE = new SchematicPartRecipeSerializer();
		private final ShapelessRecipe.Serializer rootSerializer = new ShapelessRecipe.Serializer();
		@Override
		public Codec<SchematicPartRecipe> codec() {
			return rootSerializer.codec().flatXmap(recipe -> DataResult.success(new SchematicPartRecipe(recipe, StateService.INSTANCE)) ,(recipe) -> {
				throw new NotImplementedException("Serializing ShapedRecipe is not implemented yet.");
			} );
		}

		@Override
		public RecipeSerializer<?> getSerializer() {
			return INSTANCE;
		}

		@Override
		public SchematicPartRecipe read( PacketByteBuf packetByteBuf) {
			return new SchematicPartRecipe(rootSerializer.read( packetByteBuf), StateService.INSTANCE);
		}

		@Override
		public void write(PacketByteBuf buf, SchematicPartRecipe recipe) {
			rootSerializer.write(buf, recipe);
		}

		@Override
		public Identifier getIdentifier() {
			return new Identifier(Forgero.NAMESPACE, RecipeTypes.SCHEMATIC_PART_CRAFTING.getName());
		}
	}
}
