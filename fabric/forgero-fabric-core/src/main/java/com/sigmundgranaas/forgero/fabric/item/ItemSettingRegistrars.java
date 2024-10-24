package com.sigmundgranaas.forgero.fabric.item;

import com.sigmundgranaas.forgero.core.registry.GenericRegistry;
import com.sigmundgranaas.forgero.core.registry.Registerable;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.item.SettingProcessor;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;


public class ItemSettingRegistrars implements Registerable<SettingProcessor> {
	public static Item.Settings fireProofIfStateContainsNetherite(Item.Settings settings, State state) {
		if (state.name().contains("netherite")) {
			settings.fireproof();
		}
		return settings;
	}

	public static Item.Settings recipeRemainderIfSchematic(Item.Settings settings, State state) {
		if (state.name().contains("schematic")) {
			settings.recipeRemainder(Registries.ITEM.get(new Identifier(state.identifier())));
		}
		return settings;
	}

	@Override
	public void register(GenericRegistry<SettingProcessor> registry) {
		registry.register("forgero:fireproof", ItemSettingRegistrars::fireProofIfStateContainsNetherite);
		registry.register("forgero:recipeRemainder", ItemSettingRegistrars::recipeRemainderIfSchematic);
	}
}
