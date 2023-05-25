package com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundEncoder;
import com.sigmundgranaas.forgero.minecraft.common.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;

public class StateUpgradeShapelessRecipe extends ShapelessRecipe {
	private final StateService service;

	public StateUpgradeShapelessRecipe(ShapelessRecipe recipe, StateService service) {
		super(recipe.getId(), recipe.getGroup(), CraftingRecipeCategory.EQUIPMENT, recipe.getOutput(), recipe.getIngredients());
		this.service = service;
	}

	private Optional<State> findUpgrade(Inventory inventory) {
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			var state = service.convert(stack);
			if (state.isPresent() && !isSameStateShallow(stack, getOutput())) {
				return state;
			}
		}
		return Optional.empty();
	}


	private boolean isSameStateShallow(ItemStack reference, ItemStack comparator) {
		return service.convert(reference).map(Identifiable::identifier).orElse("Missing").equals(service.convert(comparator).map(Identifiable::identifier).orElse(EMPTY_IDENTIFIER));
	}

	private Optional<State> findRoot(Inventory inventory) {
		return findRootIndex(inventory).map(inventory::getStack).flatMap(service::convert);
	}

	private Optional<Integer> findRootIndex(Inventory inventory) {
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			var state = service.convert(stack);
			if (state.isPresent() && isSameStateShallow(stack, getOutput())) {
				return Optional.of(i);
			}
		}
		return Optional.empty();
	}

	@Override
	public ItemStack craft(CraftingInventory craftingInventory) {
		var originStateOpt = findRoot(craftingInventory);
		var upgradeOpt = findUpgrade(craftingInventory);
		var originIndex = findRootIndex(craftingInventory);
		if (originStateOpt.isPresent() && upgradeOpt.isPresent() && originIndex.isPresent() && originStateOpt.get() instanceof Composite state) {
			State upgraded = state.upgrade(upgradeOpt.get());
			var output = getOutput().copy();
			if (craftingInventory.getStack(originIndex.get()).hasNbt()) {
				output.setNbt(craftingInventory.getStack(originIndex.get()).getOrCreateNbt().copy());
			}
			output.getOrCreateNbt().put(FORGERO_IDENTIFIER, CompoundEncoder.ENCODER.encode(upgraded));
			return output;
		}
		return getOutput().copy();
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return StateUpgradeShapelessRecipeSerializer.INSTANCE;
	}

	public static class StateUpgradeShapelessRecipeSerializer extends Serializer implements ForgeroRecipeSerializer {
		public static final StateUpgradeShapelessRecipeSerializer INSTANCE = new StateUpgradeShapelessRecipeSerializer();

		@Override
		public RecipeSerializer<?> getSerializer() {
			return INSTANCE;
		}

		@Override
		public StateUpgradeShapelessRecipe read(Identifier identifier, JsonObject jsonObject) {
			return new StateUpgradeShapelessRecipe(super.read(identifier, jsonObject), StateService.INSTANCE);
		}

		@Override
		public StateUpgradeShapelessRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
			return new StateUpgradeShapelessRecipe(super.read(identifier, packetByteBuf), StateService.INSTANCE);
		}

		@Override
		public Identifier getIdentifier() {
			return new Identifier(Forgero.NAMESPACE, RecipeTypes.STATE_UPGRADE_CRAFTING_TABLE_RECIPE.getName());
		}
	}
}
