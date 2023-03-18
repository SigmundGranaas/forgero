package com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.LeveledState;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants;
import com.sigmundgranaas.forgero.minecraft.common.recipe.ForgeroRecipeSerializer;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;

public class GemUpgradeRecipe extends SmithingRecipe {
	public GemUpgradeRecipe(SmithingRecipe recipe) {
		super(recipe.getId(), recipe.base, recipe.addition, recipe.getOutput().copy());
	}

	@Override
	public boolean matches(Inventory inventory, World world) {
		if (super.matches(inventory, world)) {
			var base = base(inventory);
			var addition = addition(inventory);
			if (base.isPresent() && addition.isPresent()) {
				return base.get().level() == addition.get().level();

			}
		}
		return false;
	}

	private Optional<LeveledState> base(Inventory inventory) {
		var target = StateConverter.of(inventory.getStack(0));
		return target.filter(LeveledState.class::isInstance).map(LeveledState.class::cast);
	}

	private Optional<LeveledState> addition(Inventory inventory) {
		var addition = StateConverter.of(inventory.getStack(1));
		return addition.filter(LeveledState.class::isInstance).map(LeveledState.class::cast);
	}

	@Override
	public ItemStack craft(Inventory inventory) {
		var base = base(inventory);
		var output = getOutput().copy();
		if (base.isPresent()) {
			var newBase = base.get().levelUp();
			var nbt = CompoundEncoder.ENCODER.encode(newBase);

			output.getOrCreateNbt().put(NbtConstants.FORGERO_IDENTIFIER, nbt);
			output.getOrCreateNbt().putInt("CustomModelData", newBase.level());
		}
		return output;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	public static class Serializer extends SmithingRecipe.Serializer implements ForgeroRecipeSerializer {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public SmithingRecipe read(Identifier identifier, JsonObject jsonObject) {
			return new GemUpgradeRecipe(super.read(identifier, jsonObject));
		}

		@Override
		public SmithingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
			return new GemUpgradeRecipe(super.read(identifier, packetByteBuf));
		}

		@Override
		public Identifier getIdentifier() {
			return new Identifier(Forgero.NAMESPACE, RecipeTypes.GEM_UPGRADE_RECIPE.getName());
		}

		@Override
		public RecipeSerializer<?> getSerializer() {
			return INSTANCE;
		}
	}
}
