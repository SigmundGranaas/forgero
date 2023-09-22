package com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe;

import java.util.Optional;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.LeveledState;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants;
import com.sigmundgranaas.forgero.minecraft.common.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class GemUpgradeRecipe implements SmithingRecipe {
	public final Ingredient base;
	public final Ingredient addition;
	final ItemStack result;
	private final StateService service;

	public static final Integer baseIndex = 1;
	public static final Integer additionIndex = 2;

	public GemUpgradeRecipe(Ingredient base, Ingredient addition, ItemStack result) {
		this.base = base;
		this.addition = addition;
		this.result = result;
		this.service = StateService.INSTANCE;
	}


	@Override
	public boolean matches(Inventory inventory, World world) {
		if (this.base.test(inventory.getStack(baseIndex)) && this.addition.test(inventory.getStack(additionIndex))) {
			var base = base(inventory);
			var addition = addition(inventory);
			if (base.isPresent() && addition.isPresent()) {
				return base.get().level() == addition.get().level();

			}
		}
		return false;
	}

	private Optional<LeveledState> base(Inventory inventory) {
		var target = service.convert(inventory.getStack(baseIndex));
		return target.filter(LeveledState.class::isInstance).map(LeveledState.class::cast);
	}

	private Optional<LeveledState> addition(Inventory inventory) {
		var addition = service.convert(inventory.getStack(additionIndex));
		return addition.filter(LeveledState.class::isInstance).map(LeveledState.class::cast);
	}

	@Override
	public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
		var base = base(inventory);
		var output = getResult(registryManager).copy();
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

	public ItemStack getResult(DynamicRegistryManager registryManager) {
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


	@Override
	public DefaultedList<Ingredient> getIngredients() {
		DefaultedList<Ingredient> list = DefaultedList.ofSize(3, Ingredient.EMPTY);
		list.set(baseIndex, base);
		list.set(additionIndex, addition);
		return list;
	}

	public boolean isEmpty() {
		return Stream.of(this.base, this.addition).anyMatch((ingredient) -> ingredient.getMatchingStacks().length == 0);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return GemUpgradeRecipe.Serializer.INSTANCE.getSerializer();
	}

	public static class Serializer implements RecipeSerializer<GemUpgradeRecipe>, ForgeroRecipeSerializer {
		public static final Serializer INSTANCE = new Serializer();

		private static final Codec<GemUpgradeRecipe> CODEC = RecordCodecBuilder.create((instance) -> {
			return instance.group( Ingredient.ALLOW_EMPTY_CODEC.fieldOf("base").forGetter((recipe) -> {
				return recipe.base;
			}), Ingredient.ALLOW_EMPTY_CODEC.fieldOf("addition").forGetter((recipe) -> {
				return recipe.addition;
			}), RecipeCodecs.CRAFTING_RESULT.fieldOf("result").forGetter((recipe) -> {
				return recipe.result;
			})).apply(instance,GemUpgradeRecipe::new);
		});
		@Override
		public Codec<GemUpgradeRecipe> codec() {
			return CODEC;
		}

		public GemUpgradeRecipe read(PacketByteBuf packetByteBuf) {
			Ingredient ingredient = Ingredient.fromPacket(packetByteBuf);
			Ingredient ingredient2 = Ingredient.fromPacket(packetByteBuf);
			ItemStack itemStack = packetByteBuf.readItemStack();
			return new GemUpgradeRecipe( ingredient, ingredient2, itemStack);
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
