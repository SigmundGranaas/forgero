package com.sigmundgranaas.forgero.bow;

import static com.sigmundgranaas.forgero.bow.Attributes.*;
import static com.sigmundgranaas.forgero.bow.entity.DynamicArrowEntity.DYNAMIC_ARROW_IDENTIFIER;
import static com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttributeModificationRegistry.modificationBuilder;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.*;

import java.util.List;

import com.sigmundgranaas.forgero.bow.entity.DynamicArrowEntity;
import com.sigmundgranaas.forgero.bow.handler.LaunchProjectileHandler;
import com.sigmundgranaas.forgero.bow.handler.MountProjectileHandler;
import com.sigmundgranaas.forgero.bow.item.BowGroupRegistrars;
import com.sigmundgranaas.forgero.bow.item.DynamicBowItemRegistrationHandler;
import com.sigmundgranaas.forgero.bow.predicate.BowPullPredicate;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.handler.HandlerBuilderRegistry;
import com.sigmundgranaas.forgero.core.model.match.PredicateFactory;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.registry.RegistryFactory;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.StopHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.UseHandler;
import com.sigmundgranaas.forgero.minecraft.common.item.BuildableStateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.ItemRegistries;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipAttributeRegistry;

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

		var bows = List.of(DRAW_POWER, DRAW_SPEED, ACCURACY, Weight.KEY, Durability.KEY);
		TooltipAttributeRegistry.filterBuilder()
				.type(Type.BOW)
				.attributes(bows)
				.register();

		TooltipAttributeRegistry.filterBuilder()
				.type(Type.BOW_LIMB)
				.attributes(bows)
				.register();

		var arrows = List.of(AttackDamage.KEY, ACCURACY, Weight.KEY);
		TooltipAttributeRegistry.filterBuilder()
				.type(Type.ARROW_HEAD)
				.attributes(bows)
				.register();

		TooltipAttributeRegistry.filterBuilder()
				.type(Type.ARROW)
				.attributes(arrows)
				.register();

		var materials = List.of(DRAW_POWER, DRAW_SPEED, ACCURACY);
		TooltipAttributeRegistry.filterBuilder()
				.type(Type.MATERIAL)
				.attributes(materials)
				.register();

		modificationBuilder()
				.attributeKey(DRAW_SPEED)
				.modification(reduceByWeight)
				.register();

		modificationBuilder()
				.attributeKey(DRAW_SPEED)
				.modification(minDrawSpeed)
				.register();
	}


}
