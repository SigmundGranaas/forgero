package com.sigmundgranaas.forgero.bow;

import static com.sigmundgranaas.forgero.bow.entity.DynamicArrowEntity.DYNAMIC_ARROW_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.*;

import com.sigmundgranaas.forgero.bow.entity.DynamicArrowEntity;
import com.sigmundgranaas.forgero.bow.handler.LaunchProjectileHandler;
import com.sigmundgranaas.forgero.bow.handler.MountProjectileHandler;
import com.sigmundgranaas.forgero.bow.item.BowGroupRegistrars;
import com.sigmundgranaas.forgero.bow.item.DynamicBowItemRegistrationHandler;
import com.sigmundgranaas.forgero.bow.predicate.BowPullPredicate;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.handler.HandlerBuilderRegistry;
import com.sigmundgranaas.forgero.core.model.match.PredicateFactory;
import com.sigmundgranaas.forgero.core.registry.RegistryFactory;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.StopHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.UseHandler;
import com.sigmundgranaas.forgero.minecraft.common.item.BuildableStateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.ItemRegistries;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

public class ForgeroBowInitializer implements ForgeroPreInitializationEntryPoint {
	public static final RegistryKey<ItemGroup> FORGERO_BOWS_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(Forgero.NAMESPACE, "bows"));
	public static final ItemGroup FORGERO_BOWS = FabricItemGroup.builder()
			.icon(ForgeroBowInitializer::bowIcon)
			.displayName(Text.translatable("itemGroup.forgero.bows"
			))
			.build();

	static {
		Registry.register(Registries.ITEM_GROUP, FORGERO_BOWS_KEY, FORGERO_BOWS);
	}

	public static EntityType<DynamicArrowEntity> DYNAMIC_ARROW_ENTITY = Registry.register(Registries.ENTITY_TYPE, DYNAMIC_ARROW_IDENTIFIER, EntityType.Builder.create((EntityType<DynamicArrowEntity> entity, World world) -> new DynamicArrowEntity(entity, world), SpawnGroup.MISC).build(DYNAMIC_ARROW_IDENTIFIER.toString()));

	private static ItemStack bowIcon() {
		return new ItemStack(Registries.ITEM.get(new Identifier("forgero:oak-bow")));
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

		HandlerBuilderRegistry.register(StopHandler.KEY, LaunchProjectileHandler.TYPE, LaunchProjectileHandler.BUILDER);
		HandlerBuilderRegistry.register(UseHandler.KEY, MountProjectileHandler.TYPE, MountProjectileHandler.BUILDER);
	}
}
