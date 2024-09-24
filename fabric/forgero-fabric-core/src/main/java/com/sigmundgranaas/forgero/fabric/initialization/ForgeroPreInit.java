package com.sigmundgranaas.forgero.fabric.initialization;

import static com.sigmundgranaas.forgero.minecraft.common.api.v0.predicate.Registries.*;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.defaultItem;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.settingProcessor;
import static com.sigmundgranaas.forgero.minecraft.common.predicate.block.Adapters.*;
import static com.sigmundgranaas.forgero.minecraft.common.predicate.entity.EntityAdapter.*;
import static com.sigmundgranaas.forgero.minecraft.common.predicate.entity.EntityFlagPredicates.*;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.api.identity.DefaultRules;
import com.sigmundgranaas.forgero.core.api.identity.ModificationRuleRegistry;
import com.sigmundgranaas.forgero.core.api.identity.sorting.SortingRule;
import com.sigmundgranaas.forgero.core.api.identity.sorting.SortingRuleRegistry;
import com.sigmundgranaas.forgero.core.handler.HandlerBuilderRegistry;
import com.sigmundgranaas.forgero.core.model.match.PredicateFactory;
import com.sigmundgranaas.forgero.core.model.match.builders.string.StringIdentifierBuilder;
import com.sigmundgranaas.forgero.core.model.match.builders.string.StringModelBuilder;
import com.sigmundgranaas.forgero.core.model.match.builders.string.StringNameBuilder;
import com.sigmundgranaas.forgero.core.model.match.builders.string.StringSlotBuilder;
import com.sigmundgranaas.forgero.core.model.match.builders.string.StringSlotCategoryBuilder;
import com.sigmundgranaas.forgero.core.model.match.builders.string.StringTypeBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureRegistry;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.core.registry.RegistryFactory;
import com.sigmundgranaas.forgero.core.registry.SoulLevelPropertyRegistry;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.SoulLevelPropertyData;
import com.sigmundgranaas.forgero.core.soul.SoulLevelPropertyDataProcessor;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.fabric.item.ItemGroupRegisters;
import com.sigmundgranaas.forgero.fabric.item.ItemSettingRegistrars;
import com.sigmundgranaas.forgero.fabric.registry.DefaultLevelProperties;
import com.sigmundgranaas.forgero.minecraft.common.entity.Entities;
import com.sigmundgranaas.forgero.minecraft.common.feature.BlockBreakFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.BlockEfficiencyFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.tick.EntityTickFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.onhit.block.OnHitBlockFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.onhit.entity.OnHitEntityFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.OnUseFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.swinghand.SwingHandFeature;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.AfterUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.ConsumeStackHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.ConsumeUpgradeHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.CoolDownHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.DamageHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.BlockFilter;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.CanMineFilter;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.FilterWrapper;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.IsBlockFilter;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.SameBlockFilter;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.SimilarBlockFilter;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.All;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.Average;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.BlockBreakSpeedCalculator;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.Instant;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.Single;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.BlockSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.ColumnSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.PatternSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.RadiusVeinSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.SingleSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.EntityBasedHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.FrostHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.FunctionExecuteHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.MagneticHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.ParticleHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.SoundHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.SummonHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.TeleportHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.swing.EntityHandHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.ExplosionHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock.BlockTargetHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.ConvertHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.EntityTargetHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.FireHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.KnockbackHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.LightningStrikeHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.StatusEffectHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.BlockUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.Consume;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.EntityUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.StopHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.ThrowTridentHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.ThrowableHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.UseHandler;
import com.sigmundgranaas.forgero.minecraft.common.item.BuildableStateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.GemItemRegistrar;
import com.sigmundgranaas.forgero.minecraft.common.item.ItemRegistries;
import com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils;
import com.sigmundgranaas.forgero.minecraft.common.item.tool.DynamicToolItemRegistrationHandler;
import com.sigmundgranaas.forgero.minecraft.common.item.tool.DynamicWeaponItemRegistrationHandler;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.DamagePercentagePredicate;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.MatchContextTypePredicate;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.RandomPredicate;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.WeatherPredicate;
import com.sigmundgranaas.forgero.minecraft.common.predicate.block.BlockPredicateMatcher;
import com.sigmundgranaas.forgero.minecraft.common.predicate.codecs.KeyPair;
import com.sigmundgranaas.forgero.minecraft.common.predicate.entity.EntityAdapter;
import com.sigmundgranaas.forgero.minecraft.common.predicate.entity.EntityPredicate;
import com.sigmundgranaas.forgero.minecraft.common.predicate.flag.FlagGroupPredicate;
import com.sigmundgranaas.forgero.minecraft.common.predicate.world.DimensionPredicate;
import com.sigmundgranaas.forgero.minecraft.common.predicate.world.WorldPredicate;

import net.minecraft.item.ItemGroups;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

public class ForgeroPreInit implements ForgeroPreInitializationEntryPoint {
	@Override
	public void onPreInitialization() {
		soulLevelPropertyReloader();
		DefaultLevelProperties.defaults().forEach(SoulLevelPropertyRegistry::register);
		Entities.register();
		registerPredicateBuilders();
		registerFeatureBuilder();
		registerHandlerBuilders();
		registerItemConverters();
		registerNameModifications();
	}

	private void registerNameModifications() {
		SortingRuleRegistry sorting = SortingRuleRegistry.staticRegistry();
		sorting.registerRule("forgero:schematic", SortingRule.of(Type.SCHEMATIC, 20));
		sorting.registerRule("forgero:material", SortingRule.of(Type.MATERIAL, 10));
		sorting.registerRule("forgero:part", SortingRule.of(Type.PART, 30));

		ModificationRuleRegistry modification = ModificationRuleRegistry.staticRegistry();

		modification.registerRule("forgero:schematic", DefaultRules.schematic.build());
		modification.registerRule("forgero:handle", DefaultRules.handle.build());
		modification.registerRule("forgero:pickaxe", DefaultRules.pickaxe.build());
		modification.registerRule("forgero:sword", DefaultRules.sword.build());
		modification.registerRule("forgero:weapon", DefaultRules.weapon_head.build());
		modification.registerRule("forgero:hoe", DefaultRules.hoe.build());
		modification.registerRule("forgero:axe", DefaultRules.axe.build());
		modification.registerRule("forgero:shovel", DefaultRules.shovel.build());
	}

	private void registerItemConverters() {
		var settingRegistry = ItemRegistries.SETTING_PROCESSOR;
		var groupRegistry = ItemRegistries.GROUP_CONVERTER;

		var converterRegistry = ItemRegistries.STATE_CONVERTER;
		groupRegistry.register("forgero:default", (state) -> ItemGroups.getGroups().get(0));
		var factory = new RegistryFactory<>(groupRegistry);

		RegistryUtils.register(settingRegistry, ItemSettingRegistrars::new);
		RegistryUtils.register(groupRegistry, ItemGroupRegisters::new);

		var baseConverter = BuildableStateConverter.builder()
				.group(factory::convert)
				.settings(settingProcessor(settingRegistry))
				.item(defaultItem)
				.priority(0)
				.build();

		converterRegistry.register("forgero:default", baseConverter);
		RegistryUtils.register(converterRegistry, new DynamicWeaponItemRegistrationHandler(baseConverter));
		RegistryUtils.register(converterRegistry, new DynamicToolItemRegistrationHandler(baseConverter));
		RegistryUtils.register(converterRegistry, new GemItemRegistrar(baseConverter));
	}

	private void registerPredicateBuilders() {
		// Block
		// Key options
		BLOCK_CODEC_REGISTRY.register(BLOCKS_KEY, blocksAdapter());
		BLOCK_CODEC_REGISTRY.register(BLOCK_KEY, blockAdapter());
		BLOCK_CODEC_REGISTRY.register(TAGS_KEY, blockTagsAdapter());
		BLOCK_CODEC_REGISTRY.register(TAG_KEY, blockTagAdapter());

		// Block predicate
		PredicateFactory.register(BlockPredicateMatcher.CODEC);

		// World
		WORLD_CODEC_REGISTRY.register(KeyPair.pair(DimensionPredicate.KEY, DimensionPredicate.KEY_PAIR_CODEC));

		PredicateFactory.register(WorldPredicate.ROOT_CODEC);

		// Entity
		// Flag options
		ENTITY_FLAG_PREDICATE_REGISTRY.register(IS_SNEAKING);
		ENTITY_FLAG_PREDICATE_REGISTRY.register(IS_SPRINTING);
		ENTITY_FLAG_PREDICATE_REGISTRY.register(IS_SWIMMING);
		ENTITY_FLAG_PREDICATE_REGISTRY.register(IS_ON_GROUND);
		ENTITY_FLAG_PREDICATE_REGISTRY.register(IS_USING);

		// Key options
		ENTITY_CODEC_REGISTRY.register(KeyPair.pair(FlagGroupPredicate.KEY, FlagGroupPredicate.CODEC_SPECIFICATION));
		ENTITY_CODEC_REGISTRY.register(KeyPair.pair(ENTITY_POS_KEY, EntityAdapter.entityPosCodec()));
		ENTITY_CODEC_REGISTRY.register(KeyPair.pair(ENTITY_TYPE_KEY, EntityAdapter.entityTypePredicate()));
		ENTITY_CODEC_REGISTRY.register(KeyPair.pair(WORLD_TYPE_KEY, EntityAdapter.entityWorldCodec()));

		// Entity predicate
		PredicateFactory.register(EntityPredicate.CODEC);

		PredicateFactory.register(RandomPredicate.CODEC);

		PredicateFactory.register(new StringModelBuilder());
		PredicateFactory.register(new StringIdentifierBuilder());
		PredicateFactory.register(new StringModelBuilder());
		PredicateFactory.register(new StringSlotBuilder());
		PredicateFactory.register(StringTypeBuilder::new);
		PredicateFactory.register(StringNameBuilder::new);
		PredicateFactory.register(StringSlotCategoryBuilder::new);
		PredicateFactory.register(DamagePercentagePredicate.DamagePercentagePredicateBuilder::new);
		PredicateFactory.register(WeatherPredicate.WeatherPredicateBuilder::new);
		PredicateFactory.register(CanMineFilter.CanMineFilterBuilder::new);
		PredicateFactory.register(MatchContextTypePredicate.MatchContextTypePredicateBuilder::new);
	}

	private void registerFeatureBuilder() {
		FeatureRegistry.register(OnHitEntityFeature.KEY, OnHitEntityFeature.BUILDER);
		FeatureRegistry.register(OnHitBlockFeature.KEY, OnHitBlockFeature.BUILDER);
		FeatureRegistry.register(BlockBreakFeature.KEY, BlockBreakFeature.BUILDER);
		FeatureRegistry.register(EntityTickFeature.KEY, EntityTickFeature.BUILDER);
		FeatureRegistry.register(BlockEfficiencyFeature.KEY, BlockEfficiencyFeature.BUILDER);
		FeatureRegistry.register(OnUseFeature.KEY, OnUseFeature.BUILDER);
		FeatureRegistry.register(SwingHandFeature.KEY, SwingHandFeature.BUILDER);
	}

	private void registerHandlerBuilders() {
		// Function execution
		registerEntityBasedHandler(FunctionExecuteHandler.TYPE, FunctionExecuteHandler.BUILDER);

		registerEntityBasedHandler(SoundHandler.TYPE, SoundHandler.BUILDER);

		registerEntityBasedHandler(ParticleHandler.TYPE, ParticleHandler.BUILDER);

		registerEntityBasedHandler(FrostHandler.TYPE, FrostHandler.BUILDER);

		registerEntityBasedHandler(TeleportHandler.TYPE, TeleportHandler.BUILDER);


		//On hit entity
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, StatusEffectHandler.TYPE, StatusEffectHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, ExplosionHandler.TYPE, ExplosionHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, LightningStrikeHandler.TYPE, LightningStrikeHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, FireHandler.TYPE, FireHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, ConvertHandler.TYPE, ConvertHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, KnockbackHandler.TYPE, KnockbackHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, SummonHandler.TYPE, SummonHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, MagneticHandler.TYPE, MagneticHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, StatusEffectHandler.TYPE, StatusEffectHandler.BUILDER);

		//On hit block
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, StatusEffectHandler.TYPE, StatusEffectHandler.BUILDER);
		HandlerBuilderRegistry.register(BlockTargetHandler.KEY, SummonHandler.TYPE, SummonHandler.BUILDER);
		HandlerBuilderRegistry.register(BlockTargetHandler.KEY, ExplosionHandler.TYPE, ExplosionHandler.BUILDER);
		HandlerBuilderRegistry.register(BlockTargetHandler.KEY, MagneticHandler.TYPE, MagneticHandler.BUILDER);
		HandlerBuilderRegistry.register(BlockTargetHandler.KEY, FireHandler.TYPE, FireHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, StatusEffectHandler.TYPE, StatusEffectHandler.BUILDER);


		// After use
		HandlerBuilderRegistry.register(AfterUseHandler.KEY, ConsumeStackHandler.TYPE, ConsumeStackHandler.BUILDER);
		HandlerBuilderRegistry.register(AfterUseHandler.KEY, ConsumeUpgradeHandler.TYPE, ConsumeUpgradeHandler.BUILDER);
		HandlerBuilderRegistry.register(AfterUseHandler.KEY, DamageHandler.TYPE, DamageHandler.BUILDER);
		HandlerBuilderRegistry.register(AfterUseHandler.KEY, CoolDownHandler.TYPE, CoolDownHandler.BUILDER);

		// On entity tick
		HandlerBuilderRegistry.register(EntityBasedHandler.KEY, MagneticHandler.TYPE, MagneticHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityBasedHandler.KEY, SummonHandler.TYPE, SummonHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityBasedHandler.KEY, StatusEffectHandler.TYPE, StatusEffectHandler.BUILDER);


		// Block selectors
		HandlerBuilderRegistry.register(BlockSelector.KEY, ColumnSelector.TYPE, ColumnSelector.BUILDER);
		HandlerBuilderRegistry.register(BlockSelector.KEY, PatternSelector.TYPE, PatternSelector.BUILDER);
		HandlerBuilderRegistry.register(BlockSelector.KEY, RadiusVeinSelector.TYPE, RadiusVeinSelector.BUILDER);
		HandlerBuilderRegistry.register(BlockSelector.KEY, SingleSelector.TYPE, SingleSelector.BUILDER);

		// Hardness calculators
		HandlerBuilderRegistry.register(BlockBreakSpeedCalculator.KEY, All.TYPE, All.BUILDER);
		HandlerBuilderRegistry.register(BlockBreakSpeedCalculator.KEY, Average.TYPE, Average.BUILDER);
		HandlerBuilderRegistry.register(BlockBreakSpeedCalculator.KEY, Instant.TYPE, Instant.BUILDER);
		HandlerBuilderRegistry.register(BlockBreakSpeedCalculator.KEY, Single.TYPE, Single.BUILDER);

		// Use handlers
		HandlerBuilderRegistry.builder(Consume.TYPE, Consume.BUILDER)
				.register(BlockUseHandler.KEY)
				.register(EntityUseHandler.KEY)
				.register(UseHandler.KEY);
		// Entity use handlers

		// Stop use handlers
		HandlerBuilderRegistry.register(StopHandler.KEY, ThrowTridentHandler.TYPE, ThrowTridentHandler.BUILDER);
		HandlerBuilderRegistry.register(StopHandler.KEY, ThrowableHandler.TYPE, ThrowableHandler.BUILDER);
		HandlerBuilderRegistry.register(StopHandler.KEY, ConsumeStackHandler.TYPE, ConsumeStackHandler.BUILDER);
		HandlerBuilderRegistry.register(StopHandler.KEY, ConsumeUpgradeHandler.TYPE, ConsumeUpgradeHandler.BUILDER);
		HandlerBuilderRegistry.register(StopHandler.KEY, DamageHandler.TYPE, DamageHandler.BUILDER);
		HandlerBuilderRegistry.register(StopHandler.KEY, CoolDownHandler.TYPE, CoolDownHandler.BUILDER);

		// Block filters
		HandlerBuilderRegistry.register(BlockFilter.KEY, FilterWrapper.TYPE, FilterWrapper.BUILDER);
		HandlerBuilderRegistry.register(BlockFilter.KEY, CanMineFilter.TYPE, CanMineFilter.BUILDER);
		HandlerBuilderRegistry.register(BlockFilter.KEY, IsBlockFilter.TYPE, IsBlockFilter.BUILDER);
		HandlerBuilderRegistry.register(BlockFilter.KEY, SameBlockFilter.TYPE, SameBlockFilter.BUILDER);
		HandlerBuilderRegistry.register(BlockFilter.KEY, SimilarBlockFilter.TYPE, SimilarBlockFilter.BUILDER);
		HandlerBuilderRegistry.register(BlockFilter.KEY, BlockPredicateMatcher.TYPE, BlockPredicateMatcher.CODEC);
	}

	private void registerEntityBasedHandler(String key, JsonBuilder<? extends EntityBasedHandler> builder) {
		HandlerBuilderRegistry.builder(key, builder)
				.register(EntityTargetHandler.KEY)
				.register(BlockTargetHandler.KEY)
				.register(EntityBasedHandler.KEY)
				.register(EntityHandHandler.KEY)
				.register(BlockUseHandler.KEY)
				.register(EntityUseHandler.KEY)
				.register(UseHandler.KEY)
				.register(StopHandler.KEY);
	}

	private void soulLevelPropertyReloader() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA)
				.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
					@Override
					public void reload(ResourceManager manager) {
						SoulLevelPropertyRegistry.refresh();
						Gson gson = new Gson();
						for (Resource res : manager.findResources("leveled_soul_properties", path -> path.getPath().endsWith(".json")).values()) {
							try (InputStream stream = res.getInputStream()) {
								SoulLevelPropertyData data = gson.fromJson(new JsonReader(new InputStreamReader(stream)), SoulLevelPropertyData.class);
								SoulLevelPropertyRegistry.register(data.getId(), new SoulLevelPropertyDataProcessor(data));
							} catch (Exception e) {
								Forgero.LOGGER.error(e);
							}
						}
					}

					@Override
					public Identifier getFabricId() {
						return new Identifier(Forgero.NAMESPACE, "soul_level_property");
					}
				});
	}
}
