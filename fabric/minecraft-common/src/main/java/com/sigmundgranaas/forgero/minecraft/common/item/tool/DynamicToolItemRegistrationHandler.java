package com.sigmundgranaas.forgero.minecraft.common.item.tool;

import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.itemClassPreparer;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.typeMatcher;

import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.minecraft.common.item.BuildableStateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.registry.GenericRegistry;
import com.sigmundgranaas.forgero.minecraft.common.item.ItemData;
import com.sigmundgranaas.forgero.core.registry.RankableConverter;
import com.sigmundgranaas.forgero.core.registry.Registerable;
import com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.Item;

public class DynamicToolItemRegistrationHandler implements Registerable<RankableConverter<StateProvider, ItemData>> {
	private final BuildableStateConverter defaultStateConverter;

	public DynamicToolItemRegistrationHandler(BuildableStateConverter defaultStateConverter) {
		this.defaultStateConverter = defaultStateConverter;
	}

	@Override
	public void register(GenericRegistry<RankableConverter<StateProvider, ItemData>> registry) {
		var base = defaultStateConverter.toBuilder()
				.priority(1)
				.build();

		registry.register("forgero:dynamic_pickaxe", base.toBuilder()
				.matcher(typeMatcher("PICKAXE"))
				.item(itemClassPreparer(this::pickaxe))
				.build());

		registry.register("forgero:dynamic_axe", base.toBuilder()
				.matcher(typeMatcher("AXE"))
				.item(itemClassPreparer(this::axe))
				.build());

		registry.register("forgero:dynamic_hoe", base.toBuilder()
				.matcher(typeMatcher("HOE"))
				.item(itemClassPreparer(this::hoe))
				.build());

		registry.register("forgero:dynamic_shovel", base.toBuilder()
				.matcher(typeMatcher("SHOVEL"))
				.item(itemClassPreparer(this::shovel))
				.build());
	}

	private Item pickaxe(StateProvider provider, Item.Settings settings, RegistryUtils.DynamicToolItemSettings params) {
		return new DynamicPickaxeItem(new ForgeroMaterial(provider, params.ingredient(), StateService.INSTANCE), params.attackDamage(), params.attackSpeed(), settings, provider);
	}

	private Item hoe(StateProvider provider, Item.Settings settings, RegistryUtils.DynamicToolItemSettings params) {
		return new DynamicHoeItem(new ForgeroMaterial(provider, params.ingredient(), StateService.INSTANCE), params.attackDamage(), params.attackSpeed(), settings, provider);
	}

	private Item shovel(StateProvider provider, Item.Settings settings, RegistryUtils.DynamicToolItemSettings params) {
		return new DynamicShovelItem(new ForgeroMaterial(provider, params.ingredient(), StateService.INSTANCE), params.attackDamage(), params.attackSpeed(), settings, provider);
	}

	private Item axe(StateProvider provider, Item.Settings settings, RegistryUtils.DynamicToolItemSettings params) {
		return new DynamicAxeItem(new ForgeroMaterial(provider, params.ingredient(), StateService.INSTANCE), params.attackDamage(), params.attackSpeed(), settings, provider);
	}
}
