package com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.BaseComposite;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompositeEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants;
import com.sigmundgranaas.forgero.minecraft.common.recipe.ForgeroRecipeSerializer;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class StateCraftingRecipe extends ShapedRecipe {

    public StateCraftingRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.getCategory(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput());
    }

	@Override
	public boolean matches(CraftingInventory craftingInventory, World world) {
		if (super.matches(craftingInventory, world)) {
			if (result().isPresent() && result().get() instanceof Composite result) {
				boolean isSameMaterial = IntStream.range(0, craftingInventory.size())
						.mapToObj(craftingInventory::getStack)
						.map(this::convertHead)
						.flatMap(Optional::stream)
						.map(State::identifier)
						.map(id -> id.split(":")[1])
						.anyMatch(name -> name.split("-")[0].equals(result.name().split("-")[0]));

				return isSameMaterial;

			}
		}
		return false;
	}

	private Optional<State> convertHead(ItemStack stack) {
		var converted = StateConverter.of(stack);
		if (converted.isPresent() && (converted.get().test(Type.SWORD_BLADE) || converted.get().test(Type.TOOL_PART_HEAD))) {
			return converted;
		}
		return Optional.empty();
	}

	private List<State> partsFromCraftingInventory(CraftingInventory craftingInventory) {
		List<ItemStack> ingredients = new ArrayList<>();
		for (int i = 0; i < craftingInventory.size(); i++) {
			var stack = craftingInventory.getStack(i);
			if (this.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(ingredient -> ingredient.test(stack))) {
				ingredients.add(craftingInventory.getStack(i));
			}
		}
		return ingredients.stream().map(StateConverter::of).flatMap(Optional::stream).toList();
	}

	@Override
	public ItemStack craft(CraftingInventory craftingInventory) {
		var target = StateConverter.of(this.getOutput());
		if (target.isPresent()) {
			var targetState = target.get();
			var parts = partsFromCraftingInventory(craftingInventory);
			var toolBuilderOpt = ConstructedTool.ToolBuilder.builder(parts);
			BaseComposite.BaseCompositeBuilder<?> builder;
			if (toolBuilderOpt.isPresent()) {
				builder = toolBuilderOpt.get();
			} else {
				builder = Construct.builder();
				parts.forEach(builder::addIngredient);
			}

			builder.type(targetState.type())
					.name(targetState.name())
					.nameSpace(targetState.nameSpace());

			var nbt = new CompositeEncoder().encode(builder.build());
			var output = getOutput().copy();
			output.getOrCreateNbt().put(NbtConstants.FORGERO_IDENTIFIER, nbt);
			return output;
		}
		return getOutput().copy();
	}

	private Optional<State> result() {
		return StateConverter.of(getOutput());
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return StateCraftingRecipeSerializer.INSTANCE;
	}

	public static class StateCraftingRecipeSerializer extends Serializer implements ForgeroRecipeSerializer {
		public static final StateCraftingRecipeSerializer INSTANCE = new StateCraftingRecipeSerializer();

		@Override
		public StateCraftingRecipe read(Identifier identifier, JsonObject jsonObject) {
			return new StateCraftingRecipe(super.read(identifier, jsonObject));
		}

		@Override
		public StateCraftingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
			return new StateCraftingRecipe(super.read(identifier, packetByteBuf));
		}

		@Override
		public Identifier getIdentifier() {
			return new Identifier(Forgero.NAMESPACE, RecipeTypes.STATE_CRAFTING_RECIPE.getName());
		}

		@Override
		public RecipeSerializer<?> getSerializer() {
			return INSTANCE;
		}
	}
}
