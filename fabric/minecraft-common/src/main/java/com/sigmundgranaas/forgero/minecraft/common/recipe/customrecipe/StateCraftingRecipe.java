package com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe;

import static com.sigmundgranaas.forgero.core.customdata.ContainerVisitor.VISITOR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.BaseComposite;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.customdata.EnchantmentVisitor;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompositeEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants;
import com.sigmundgranaas.forgero.minecraft.common.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class StateCraftingRecipe extends ShapedRecipe {
	private final StateService service;

	public StateCraftingRecipe(ShapedRecipe recipe, StateService service) {
		super(recipe.getId(), recipe.getGroup(), recipe.getCategory(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput(null));
		this.service = service;
	}

	@Override
	public boolean matches(RecipeInputInventory craftingInventory, World world) {
		if (super.matches(craftingInventory, world)) {
			if (result().isPresent() && result().get() instanceof Composite result) {

				return IntStream.range(0, craftingInventory.size())
						.mapToObj(craftingInventory::getStack)
						.map(this::convertHead)
						.flatMap(Optional::stream)
						.map(State::identifier)
						.map(id -> id.split(":")[1])
						.anyMatch(name -> name.split("-")[0].equals(result.name().split("-")[0]));
			}
		}
		return false;
	}

	private Optional<State> convertHead(ItemStack stack) {
		var converted = service.convert(stack);
		Predicate<State> isPartHead = (part) ->
								part.test(Type.WEAPON_HEAD) ||
								part.test(Type.TOOL_PART_HEAD) ||
								part.test(Type.ARROW_HEAD) ||
								part.test(Type.BOW_LIMB);
		if (converted.isPresent() && isPartHead.test(converted.get())) {
			return converted;
		}
		return Optional.empty();
	}

	private List<State> partsFromCraftingInventory(RecipeInputInventory craftingInventory) {
		List<ItemStack> ingredients = new ArrayList<>();
		for (int i = 0; i < craftingInventory.size(); i++) {
			var stack = craftingInventory.getStack(i);
			if (this.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(ingredient -> ingredient.test(stack))) {
				ingredients.add(craftingInventory.getStack(i));
			}
		}
		return ingredients.stream().map(service::convert).flatMap(Optional::stream).toList();
	}

	private List<State> upgradesFromCraftingInventory(RecipeInputInventory craftingInventory, DynamicRegistryManager registryManager) {
		List<State> upgrades = new ArrayList<>();
		for (int i = 0; i < craftingInventory.size(); i++) {
			var stack = craftingInventory.getStack(i);
			if (this.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(ingredient -> ingredient.test(stack))) {
				var state = service.convert(stack);
				if (state.isPresent() && (state.get().test(Type.BINDING) || state.get().test(Type.SWORD_GUARD))) {
					upgrades.add(state.get());
				}
			}
		}
		return upgrades;

	}

	@Override
	public ItemStack craft(RecipeInputInventory craftingInventory, DynamicRegistryManager registryManager) {
		var target = service.convert(this.getOutput(null));
		if (target.isPresent()) {
			var targetState = target.get();
			var upgrades = upgradesFromCraftingInventory(craftingInventory, null);
			var parts = partsFromCraftingInventory(craftingInventory).stream().filter(state -> !upgrades.contains(state)).toList();
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

			if (targetState instanceof ConstructedTool constructedTool) {
				builder.addSlotContainer(constructedTool.toolBuilder().getUpgradeContainer());
			}

			for (State upgrade : upgrades) {
				builder.addUpgrade(upgrade);
			}


			var state = builder.build();
			var nbt = new CompositeEncoder().encode(state);
			var output = getOutput(registryManager).copy();
			output.getOrCreateNbt().put(NbtConstants.FORGERO_IDENTIFIER, nbt);
			if (toolBuilderOpt.isPresent()) {
				state.accept(VISITOR)
						.map(EnchantmentVisitor::ofAll)
						.orElse(Collections.emptyList())
						.forEach(enchantment -> enchantment.embed(output));
			}
			output.setCount(getOutput(registryManager).getCount());
			return output;
		}
		return getOutput(registryManager).copy();
	}

	private Optional<State> result() {
		return service.convert(getOutput(null));
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return StateCraftingRecipeSerializer.INSTANCE;
	}

	public static class StateCraftingRecipeSerializer extends Serializer implements ForgeroRecipeSerializer {
		public static final StateCraftingRecipeSerializer INSTANCE = new StateCraftingRecipeSerializer();

		@Override
		public StateCraftingRecipe read(Identifier identifier, JsonObject jsonObject) {
			return new StateCraftingRecipe(super.read(identifier, jsonObject), StateService.INSTANCE);
		}

		@Override
		public StateCraftingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
			ShapedRecipe recipe = super.read(identifier, packetByteBuf);
			return new StateCraftingRecipe(recipe, StateService.INSTANCE);
		}

		@Override
		public void write(PacketByteBuf packetByteBuf, ShapedRecipe shapedRecipe) {
			super.write(packetByteBuf, shapedRecipe);
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
