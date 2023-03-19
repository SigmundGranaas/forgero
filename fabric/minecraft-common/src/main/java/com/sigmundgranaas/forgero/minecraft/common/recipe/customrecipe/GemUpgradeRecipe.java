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
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.LegacySmithingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.stream.Stream;

public class GemUpgradeRecipe extends LegacySmithingRecipe {
	public final Ingredient base;
	public final Ingredient addition;
	final ItemStack result;
	private final Identifier id;

	public GemUpgradeRecipe(Identifier id, Ingredient base, Ingredient addition, ItemStack result) {
		super(id, base, addition, result);
		this.id = id;
		this.base = base;
		this.addition = addition;
		this.result = result;
	}


	@Override
	public boolean matches(Inventory inventory, World world) {
		if (this.base.test(inventory.getStack(0)) && this.addition.test(inventory.getStack(1))) {
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
	public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
		var base = base(inventory);
		var output = getOutput(registryManager).copy();
		if (base.isPresent()) {
			var newBase = base.get().levelUp();
			var nbt = CompoundEncoder.ENCODER.encode(newBase);

			output.getOrCreateNbt().put(NbtConstants.FORGERO_IDENTIFIER, nbt);
			output.getOrCreateNbt().putInt("CustomModelData", newBase.level());
		}
		return output;
	}
	public boolean fits(int width, int height) {
		return width * height >= 2;
	}

	public ItemStack getOutput(DynamicRegistryManager registryManager) {
		return this.result;
	}

	public boolean testTemplate(ItemStack stack) {
		return false;
	}

	public boolean testBase(ItemStack stack) {
		return this.base.test(stack);
	}

	public boolean testAddition(ItemStack stack) {
		return this.addition.test(stack);
	}

	public Identifier getId() {
		return this.id;
	}

	public boolean isEmpty() {
		return Stream.of(this.base, this.addition).anyMatch((ingredient) -> ingredient.getMatchingStacks().length == 0);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializer.SMITHING;
	}

	public static class Serializer implements RecipeSerializer<GemUpgradeRecipe>, ForgeroRecipeSerializer {
		public static final Serializer INSTANCE = new Serializer();
		public GemUpgradeRecipe read(Identifier identifier, JsonObject jsonObject) {
			Ingredient ingredient = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "base"));
			Ingredient ingredient2 = Ingredient.fromJson(JsonHelper.getObject(jsonObject, "addition"));
			ItemStack itemStack = ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "result"));
			return new GemUpgradeRecipe(identifier, ingredient, ingredient2, itemStack);
		}

		public GemUpgradeRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
			Ingredient ingredient = Ingredient.fromPacket(packetByteBuf);
			Ingredient ingredient2 = Ingredient.fromPacket(packetByteBuf);
			ItemStack itemStack = packetByteBuf.readItemStack();
			return new GemUpgradeRecipe(identifier, ingredient, ingredient2, itemStack);
		}

		public void write(PacketByteBuf packetByteBuf, GemUpgradeRecipe smithingRecipe) {
			smithingRecipe.base.write(packetByteBuf);
			smithingRecipe.addition.write(packetByteBuf);
			packetByteBuf.writeItemStack(smithingRecipe.result);
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
