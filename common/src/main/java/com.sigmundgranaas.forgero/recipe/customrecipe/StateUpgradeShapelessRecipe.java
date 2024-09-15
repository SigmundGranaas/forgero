package com.sigmundgranaas.forgero.recipe.customrecipe;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;
import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.item.nbt.v2.CompoundEncoder;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.service.StateService;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Dynamic;

public class StateUpgradeShapelessRecipe extends ShapelessRecipe {
	private final StateService service;

	public StateUpgradeShapelessRecipe(ShapelessRecipe recipe, StateService service) {
		super(recipe.getId(), recipe.getGroup(), CraftingRecipeCategory.EQUIPMENT, recipe.getOutput(null), recipe.getIngredients());
		this.service = service;
	}

	@Override
	public boolean matches(RecipeInputInventory craftingInventory, World world) {
		if (super.matches(craftingInventory, world)) {
			var root = findRoot(craftingInventory, world.getRegistryManager())
					.filter(Composite.class::isInstance)
					.map(Composite.class::cast);
			var upgrade = findUpgrade(craftingInventory, world.getRegistryManager());
			if (root.isPresent() && upgrade.isPresent()) {
				return root.get().canUpgrade(upgrade.get());
			}
		}
		return false;
	}


	private Optional<State> findUpgrade(Inventory inventory, DynamicRegistryManager manager) {
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			var state = service.convert(stack);
			if (state.isPresent() && !isSameStateShallow(stack, getOutput(manager))) {
				return state;
			}
		}
		return Optional.empty();
	}


	private boolean isSameStateShallow(ItemStack reference, ItemStack comparator) {
		return service.convert(reference).map(Identifiable::identifier).orElse("Missing").equals(service.convert(comparator).map(Identifiable::identifier).orElse(EMPTY_IDENTIFIER));
	}

	private Optional<State> findRoot(Inventory inventory, DynamicRegistryManager manager) {
		return findRootIndex(inventory, manager).map(inventory::getStack).flatMap(service::convert);
	}

	private Optional<Integer> findRootIndex(Inventory inventory, DynamicRegistryManager manager) {
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack stack = inventory.getStack(i);
			var state = service.convert(stack);
			if (state.isPresent() && isSameStateShallow(stack, getOutput(manager))) {
				return Optional.of(i);
			}
		}
		return Optional.empty();
	}

	@Override
	public ItemStack craft(RecipeInputInventory craftingInventory, DynamicRegistryManager manager) {
		var originStateOpt = findRoot(craftingInventory, manager);
		var upgradeOpt = findUpgrade(craftingInventory, manager);
		var originIndex = findRootIndex(craftingInventory, manager);
		if (originStateOpt.isPresent() && upgradeOpt.isPresent() && originIndex.isPresent() && originStateOpt.get() instanceof Composite state) {
			State upgraded = state.upgrade(upgradeOpt.get());
			var output = getOutput(manager).copy();
			if (craftingInventory.getStack(originIndex.get()).hasNbt()) {
				output.setNbt(craftingInventory.getStack(originIndex.get()).getOrCreateNbt().copy());
			}
			output.getOrCreateNbt().put(FORGERO_IDENTIFIER, CompoundEncoder.ENCODER.encode(upgraded));
			return output;
		}
		return getOutput(manager).copy();
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
