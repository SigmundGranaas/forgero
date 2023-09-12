package com.sigmundgranaas.forgero.fabric.item;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.state.MaterialBased;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.item.DefaultStateItem;
import com.sigmundgranaas.forgero.minecraft.common.item.ForgeroMaterial;
import com.sigmundgranaas.forgero.minecraft.common.item.GemItem;
import com.sigmundgranaas.forgero.minecraft.common.item.tool.DynamicAxeItem;
import com.sigmundgranaas.forgero.minecraft.common.item.tool.DynamicHoeItem;
import com.sigmundgranaas.forgero.minecraft.common.item.tool.DynamicPickaxeItem;
import com.sigmundgranaas.forgero.minecraft.common.item.tool.DynamicShovelItem;
import com.sigmundgranaas.forgero.minecraft.common.item.tool.DynamicSwordItem;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class StateToItemConverter {
	private final StateProvider provider;

	public StateToItemConverter(StateProvider provider) {
		this.provider = provider;
	}

	public static StateToItemConverter of(StateProvider provider) {
		return new StateToItemConverter(provider);
	}

	public Item convert() {
		var context = MatchContext.of();
		var state = provider.get();
		if (state.type().test(Type.of("SWORD"), context) || state.type().test(Type.of("TOOL"), context)) {
			Item item = createTool();
			ItemGroupEvents.modifyEntriesEvent(getItemGroup(provider.get())).register(entries -> entries.add(item));
			return item;
		} else if (state.type().test(Type.GEM)) {
			Item item = new GemItem(getItemSettings(state), state, StateService.INSTANCE);
			ItemGroupEvents.modifyEntriesEvent(getItemGroup(provider.get())).register(entries -> entries.add(item));
			return item;
		}
		return defaultStateItem();
	}

	private Item createTool() {
		var context = MatchContext.of();
		var state = provider.get();
		int attack_damage = (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE);
		float attack_speed = state.stream().applyAttribute(AttributeType.ATTACK_SPEED);
		Optional<State> ingredientState = Optional.empty();
		if (state instanceof ConstructedTool tool) {
			if (tool.getHead() instanceof MaterialBased based) {
				ingredientState = Optional.of(based.baseMaterial());
			}
		}
		StateService service = StateService.INSTANCE;
		Ingredient ingredient = ingredientState
				.flatMap(service::convert)
				.map(Ingredient::ofStacks)
				.orElse(ToolMaterials.WOOD.getRepairIngredient());

		if (state.type().test(Type.of("SWORD"), context)) {
			return new DynamicSwordItem((new ForgeroMaterial(provider, ingredient, service)), (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), provider);
		} else if (state.type().test(Type.of("PICKAXE"), context)) {
			return new DynamicPickaxeItem(new ForgeroMaterial(provider, ingredient, service), (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), provider);
		} else if (state.type().test(Type.of("AXE"), context)) {
			return new DynamicAxeItem((new ForgeroMaterial(provider, ingredient, service)), attack_damage, attack_speed, getItemSettings(state), () -> state);
		} else if (state.type().test(Type.of("HOE"), context)) {
			return new DynamicHoeItem((new ForgeroMaterial(provider, ingredient, service)), (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), provider);
		} else if (state.type().test(Type.of("SHOVEL"), context)) {
			return new DynamicShovelItem((new ForgeroMaterial(provider, ingredient, service)), (int) state.stream().applyAttribute(AttributeType.ATTACK_DAMAGE), state.stream().applyAttribute(AttributeType.ATTACK_SPEED), getItemSettings(state), provider);
		}
		return new DefaultStateItem(getItemSettings(state), provider);
	}

	public Identifier id() {
		return new Identifier(provider.get().nameSpace(), provider.get().name());
	}

	private Item defaultStateItem() {
		var item = new DefaultStateItem(new Item.Settings(), provider);
		ItemGroupEvents.modifyEntriesEvent(getItemGroup(provider.get())).register(entries -> entries.add(item));
		return item;
	}

	public RegistryKey<ItemGroup> getItemGroup(State state) {
		if (state.test(Type.TOOL)) {
			return net.minecraft.item.ItemGroups.TOOLS;
		} else if (state.test(Type.WEAPON)) {
			return net.minecraft.item.ItemGroups.COMBAT;
		} else if (state.test(Type.PART)) {
			return ItemGroups.FORGERO_TOOL_PARTS_KEY;
		} else if (state.test(Type.SCHEMATIC)) {
			return ItemGroups.FORGERO_SCHEMATICS_KEY;
		} else if (state.test(Type.TRINKET)) {
			return ItemGroups.FORGERO_GEMS_KEY;
		}
		return net.minecraft.item.ItemGroups.INGREDIENTS;
	}

	private Item.Settings getItemSettings(State state) {
		var settings = new Item.Settings();

		if (state.name().contains("schematic")) {
			settings.recipeRemainder(Registries.ITEM.get(new Identifier(state.identifier())));
		}

		if (state.name().contains("netherite")) {
			settings.fireproof();
		}
		return settings;
	}
}
