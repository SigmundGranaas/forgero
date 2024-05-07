package com.sigmundgranaas.forgero.minecraft.common.item.tool;

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

import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.itemClassPreparer;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.typeMatcher;

public class DynamicWeaponItemRegistrationHandler implements Registerable<RankableConverter<StateProvider, ItemData>> {
	private final BuildableStateConverter defaultStateConverter;

	public DynamicWeaponItemRegistrationHandler(BuildableStateConverter defaultStateConverter) {
		this.defaultStateConverter = defaultStateConverter;
	}

	@Override
	public void register(GenericRegistry<RankableConverter<StateProvider, ItemData>> registry) {
		var base = defaultStateConverter.toBuilder()
				.priority(1)
				.build();

		registry.register("forgero:sword", base.toBuilder()
				.matcher(typeMatcher("SWORD"))
				.item(itemClassPreparer(this::sword))
				.build());
	}

	private Item sword(StateProvider provider, Item.Settings settings, RegistryUtils.DynamicToolItemSettings params) {
		// Attack damage is now added to the material value inside the sworditem
		// Because ForgeroMaterials are a proxy for the state, we need to set the input attack damage to 0
		return new DynamicSwordItem(new ForgeroMaterial(provider, params.ingredient(), StateService.INSTANCE), 0, params.attackSpeed(), settings, provider);
	}

}
