package com.sigmundgranaas.forgero.bow;

import static com.sigmundgranaas.forgero.bow.entity.DynamicArrowEntity.DYNAMIC_ARROW_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.*;

import com.sigmundgranaas.forgero.bow.entity.DynamicArrowEntity;
import com.sigmundgranaas.forgero.bow.item.BowGroupRegistrars;
import com.sigmundgranaas.forgero.bow.item.DynamicBowItemRegistrationHandler;
import com.sigmundgranaas.forgero.bow.predicate.BowPullPredicate;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.model.match.PredicateFactory;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.minecraft.common.item.BuildableStateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.ItemRegistries;
import com.sigmundgranaas.forgero.core.registry.RegistryFactory;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;


public class ForgeroBowInitializer implements ForgeroPreInitializationEntryPoint {
	public static final ItemGroup FORGERO_BOWS = FabricItemGroupBuilder.create(
					new Identifier(Forgero.NAMESPACE, "trinkets"))
			.icon(ForgeroBowInitializer::bowIcon)
			.build();

	public static EntityType<DynamicArrowEntity> DYNAMIC_ARROW_ENTITY = Registry.register(Registry.ENTITY_TYPE, DYNAMIC_ARROW_IDENTIFIER, EntityType.Builder.create((EntityType<DynamicArrowEntity> entity, World world) -> new DynamicArrowEntity(entity, world), SpawnGroup.MISC).build(DYNAMIC_ARROW_IDENTIFIER.toString()));

	private static ItemStack bowIcon() {
		return new ItemStack(Registry.ITEM.get(new Identifier("forgero:oak-bow")));
	}

	@Override
	public void onPreInitialization() {
		PredicateFactory.register(BowPullPredicate.BowPullPredicateBuilder::new);


		var settingRegistry = ItemRegistries.SETTING_PROCESSOR;
		var groupRegistry = ItemRegistries.GROUP_CONVERTER;

		register(groupRegistry, BowGroupRegistrars::new);

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
