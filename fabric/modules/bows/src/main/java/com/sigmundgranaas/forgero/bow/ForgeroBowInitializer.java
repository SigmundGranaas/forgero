package com.sigmundgranaas.forgero.bow;

import static com.sigmundgranaas.forgero.bow.entity.DynamicArrowEntity.DYNAMIC_ARROW_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.*;

import com.sigmundgranaas.forgero.bow.entity.DynamicArrowEntity;
import com.sigmundgranaas.forgero.bow.item.DynamicBowItemRegistrationHandler;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.minecraft.common.item.BuildableStateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.ItemRegistries;
import com.sigmundgranaas.forgero.minecraft.common.item.RegistryFactory;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;


public class ForgeroBowInitializer implements ForgeroPreInitializationEntryPoint {
	public static EntityType<DynamicArrowEntity> DYNAMIC_ARROW_ENTITY = Registry.register(Registry.ENTITY_TYPE, DYNAMIC_ARROW_IDENTIFIER, EntityType.Builder.create((EntityType<DynamicArrowEntity> entity, World world) -> new DynamicArrowEntity(entity, world), SpawnGroup.MISC).build(DYNAMIC_ARROW_IDENTIFIER.toString()));


	@Override
	public void onPreInitialization() {
		var settingRegistry = ItemRegistries.SETTING_PROCESSOR;
		var groupRegistry = ItemRegistries.GROUP_CONVERTER;
		var factory = new RegistryFactory<>(groupRegistry);

		var baseConverter = BuildableStateConverter.builder()
				.group(factory::convert)
				.settings(settingProcessor(settingRegistry))
				.item(defaultItem)
				.priority(0)
				.build();

		register(ItemRegistries.STATE_CONVERTER, new DynamicBowItemRegistrationHandler(baseConverter));
	}
}
