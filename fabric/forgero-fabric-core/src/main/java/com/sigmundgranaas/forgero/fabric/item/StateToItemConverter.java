package com.sigmundgranaas.forgero.fabric.item;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.state.MaterialBased;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Context;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.DefaultStateItem;
import com.sigmundgranaas.forgero.minecraft.common.item.ForgeroMaterial;
import com.sigmundgranaas.forgero.minecraft.common.item.GemItem;
import com.sigmundgranaas.forgero.minecraft.common.item.tool.*;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

public class StateToItemConverter {
	private final StateProvider provider;

	public StateToItemConverter(StateProvider provider) {
		this.provider = provider;
	}

	public static StateToItemConverter of(StateProvider provider) {
		return new StateToItemConverter(provider);
	}

	public Item convert() {
		var context = Context.of();
		var state = provider.get();
		if (state.type().test(Type.of("SWORD"), context) || state.type().test(Type.of("TOOL"), context)) {
			return createTool();
		} else if (state.type().test(Type.GEM)) {
			return new GemItem(getItemSettings(state), state);
		}
		return defaultStateItem();
	}

	private Item createTool() {
		var context = Context.of();
		var state = provider.get();
		int attack_damage = (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE);
		float attack_speed = state.stream().applyAttribute(AttributeType.ATTACK_SPEED);
		Optional<State> ingredientState = Optional.empty();
		if (state instanceof ConstructedTool tool) {
			if (tool.getHead() instanceof MaterialBased based) {
				ingredientState = Optional.of(based.baseMaterial());
			}
		}
		Ingredient ingredient = ingredientState.map(StateConverter::of).map(Ingredient::ofStacks).orElse(ToolMaterials.WOOD.getRepairIngredient());
		if (state.type().test(Type.of("SWORD"), context)) {
			return new DynamicSwordItem((new ForgeroMaterial(provider, ingredient)), (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), provider);
		} else if (state.type().test(Type.of("PICKAXE"), context)) {
			return new DynamicPickaxeItem(new ForgeroMaterial(provider, ingredient), (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), provider);
		} else if (state.type().test(Type.of("AXE"), context)) {
			return new DynamicAxeItem((new ForgeroMaterial(provider, ingredient)), attack_damage, attack_speed, getItemSettings(state), () -> state);
		} else if (state.type().test(Type.of("HOE"), context)) {
			return new DynamicHoeItem((new ForgeroMaterial(provider, ingredient)), (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), provider);
		} else if (state.type().test(Type.of("SHOVEL"), context)) {
			return new DynamicShovelItem((new ForgeroMaterial(provider, ingredient)), (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), provider);
		}
		return new DefaultStateItem(getItemSettings(state), provider);
	}

	public Identifier id() {
		return new Identifier(provider.get().nameSpace(), provider.get().name());
	}

	private Item defaultStateItem() {
		return new DefaultStateItem(new Item.Settings().group(getItemGroup(provider.get())), provider);
	}

	private ItemGroup getItemGroup(State state) {
		if (state.test(Type.TOOL)) {
			return ItemGroup.TOOLS;
		} else if (state.test(Type.WEAPON)) {
			return ItemGroup.COMBAT;
		} else if (state.test(Type.PART)) {
			return ItemGroups.FORGERO_TOOL_PARTS;
		} else if (state.test(Type.SCHEMATIC)) {
			return ItemGroups.FORGERO_SCHEMATICS;
		} else if (state.test(Type.TRINKET)) {
			return ItemGroups.FORGERO_GEMS;
		}
		return ItemGroup.MISC;
	}

	private Item.Settings getItemSettings(State state) {
		var settings = new Item.Settings();

		settings.group(getItemGroup(state));

		if (state.name().contains("schematic")) {
			settings.recipeRemainder(Registry.ITEM.get(new Identifier(state.identifier())));
		}

		if (state.name().contains("netherite")) {
			settings.fireproof();
		}
		return settings;
	}
}
