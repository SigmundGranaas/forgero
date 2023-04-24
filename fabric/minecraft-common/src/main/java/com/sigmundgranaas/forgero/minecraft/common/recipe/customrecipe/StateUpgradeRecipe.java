package com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.conversion.CachedConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundEncoder;
import com.sigmundgranaas.forgero.minecraft.common.recipe.ForgeroRecipeSerializer;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class StateUpgradeRecipe extends SmithingRecipe {
	public StateUpgradeRecipe(SmithingRecipe recipe) {
		super(recipe.getId(), recipe.base, recipe.addition, recipe.getOutput().copy());
	}

	@Override
	public boolean matches(Inventory inventory, World world) {
		if (inventory.containsAny(ItemStack::isEmpty)) {
			return false;
		}
		if (super.matches(inventory, world)) {
			var originStateOpt = CachedConverter.of(inventory.getStack(0))
					.filter(Composite.class::isInstance)
					.map(Composite.class::cast);
			var upgradeOpt = CachedConverter.of(inventory.getStack(1));
			if (originStateOpt.isPresent() && upgradeOpt.isPresent()) {
				return originStateOpt.get().canUpgrade(upgradeOpt.get());
			}
		}
		return false;
	}

	@Override
	public ItemStack craft(Inventory inventory) {
		var originStateOpt = CachedConverter.of(inventory.getStack(0));
		var upgradeOpt = CachedConverter.of(inventory.getStack(1));
		if (originStateOpt.isPresent() && upgradeOpt.isPresent() && originStateOpt.get() instanceof Composite state) {
			State upgraded = state.upgrade(upgradeOpt.get());
			var output = getOutput().copy();
			output.setNbt(inventory.getStack(0).getOrCreateNbt().copy());
			output.getOrCreateNbt().put(FORGERO_IDENTIFIER, CompoundEncoder.ENCODER.encode(upgraded));
			return output;
		}
		return inventory.getStack(0);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	public static class Serializer extends SmithingRecipe.Serializer implements ForgeroRecipeSerializer {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public SmithingRecipe read(Identifier identifier, JsonObject jsonObject) {
			return new StateUpgradeRecipe(super.read(identifier, jsonObject));
		}

		@Override
		public SmithingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
			return new StateUpgradeRecipe(super.read(identifier, packetByteBuf));
		}

		@Override
		public Identifier getIdentifier() {
			return new Identifier(Forgero.NAMESPACE, RecipeTypes.STATE_UPGRADE_RECIPE.getName());
		}

		@Override
		public RecipeSerializer<?> getSerializer() {
			return INSTANCE;
		}
	}
}
