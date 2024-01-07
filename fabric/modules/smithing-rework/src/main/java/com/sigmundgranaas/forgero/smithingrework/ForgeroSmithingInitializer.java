package com.sigmundgranaas.forgero.smithingrework;

import static com.sigmundgranaas.forgero.bow.entity.DynamicArrowEntity.DYNAMIC_ARROW_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock.ASSEMBLY_STATION_ITEM;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.*;

import com.sigmundgranaas.forgero.bow.entity.DynamicArrowEntity;
import com.sigmundgranaas.forgero.bow.item.BowGroupRegistrars;
import com.sigmundgranaas.forgero.bow.item.DynamicBowItemRegistrationHandler;
import com.sigmundgranaas.forgero.bow.predicate.BowPullPredicate;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.model.match.PredicateFactory;
import com.sigmundgranaas.forgero.core.registry.RegistryFactory;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.minecraft.common.item.BuildableStateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.ItemRegistries;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

public class ForgeroSmithingInitializer implements ForgeroPreInitializationEntryPoint {
	public static final RegistryKey<ItemGroup> FORGERO_SMITHING_KEY =  RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(Forgero.NAMESPACE, "smithing"));

	@Override
	public void onPreInitialization() {


		var settingRegistry = ItemRegistries.SETTING_PROCESSOR;
		var groupRegistry = ItemRegistries.GROUP_CONVERTER;

		register(groupRegistry, BowGroupRegisterars::new);

		var factory = new RegistryFactory<>(groupRegistry);


		var baseConverter = BuildableStateConverter.builder()
				.group(factory::convert)
				.settings(settingProcessor(settingRegistry))
				.item(defaultItem)
				.priority(0)
				.build();

	}
}
