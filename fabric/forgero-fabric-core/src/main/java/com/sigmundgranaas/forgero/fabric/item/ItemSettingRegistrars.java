package com.sigmundgranaas.forgero.fabric.item;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.item.*;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;


public class ItemSettingRegistrars implements Registerable<SettingProcessor> {
	@Override
	public void register(GenericRegistry<SettingProcessor> registry) {
		registry.register("forgero:fireproof", ItemSettingRegistrars::fireProofIfStateContainsNetherite);
		registry.register("forgero:recipeRemainder", ItemSettingRegistrars::recipeRemainderIfSchematic);
	}

	public static Item.Settings fireProofIfStateContainsNetherite(Item.Settings settings, State state){
		if (state.name().contains("netherite")) {
			settings.fireproof();
		}
		return settings;
	}

	public static Item.Settings recipeRemainderIfSchematic(Item.Settings settings, State state){
		if (state.name().contains("schematic")) {
			settings.recipeRemainder(Registry.ITEM.get(new Identifier(state.identifier())));
		}
		return settings;
	}
}
