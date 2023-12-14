package com.sigmundgranaas.forgero.fabric.initialization;

import static com.sigmundgranaas.forgero.minecraft.common.entity.Entities.SOUL_ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.defaultItem;
import static com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils.settingProcessor;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.Forgero;
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
import com.sigmundgranaas.forgero.core.registry.SoulLevelPropertyRegistry;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.SoulLevelPropertyData;
import com.sigmundgranaas.forgero.core.soul.SoulLevelPropertyDataProcessor;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.fabric.item.ItemGroupRegisters;
import com.sigmundgranaas.forgero.fabric.item.ItemSettingRegistrars;
import com.sigmundgranaas.forgero.fabric.registry.DefaultLevelProperties;
import com.sigmundgranaas.forgero.minecraft.common.entity.Entities;
import com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity;
import com.sigmundgranaas.forgero.minecraft.common.feature.BlockBreakFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.BlockEfficiencyFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.EntityTickFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.OnHitBlockFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.OnHitEntityFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.OnUseFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.SwingHandFeature;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.AfterUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.ConsumeStackHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.ConsumeUpgradeHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.CoolDownHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.DamageHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.CanMineFilter;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.All;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.Average;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.BlockBreakSpeedCalculator;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.Diminishing;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.Instant;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness.Single;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.BlockSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.ColumnSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.PatternSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.selector.RadiusVeinSelector;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.EntityBasedHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.FrostHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.FunctionExecuteHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.MagneticHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.ParticleHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.SoundHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.SummonHandler;
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
import com.sigmundgranaas.forgero.minecraft.common.item.RegistryFactory;
import com.sigmundgranaas.forgero.minecraft.common.item.RegistryUtils;
import com.sigmundgranaas.forgero.minecraft.common.item.tool.DynamicToolItemRegistrationHandler;
import com.sigmundgranaas.forgero.minecraft.common.item.tool.DynamicWeaponItemRegistrationHandler;
import com.sigmundgranaas.forgero.minecraft.common.match.BowPullPredicate;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.BlockPredicateMatcher;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.DamagePercentagePredicate;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.EntityPredicateMatcher;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.RandomPredicate;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.WeatherPredicate;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.PredicateWriterFactory;

import net.minecraft.item.ItemGroup;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

public class ForgeroPreInit implements ForgeroPreInitializationEntryPoint {
	@Override
	public void onPreInitialization() {
		FabricDefaultAttributeRegistry.register(SOUL_ENTITY, SoulEntity.createSoulEntities());
		soulLevelPropertyReloader();
		DefaultLevelProperties.defaults().forEach(SoulLevelPropertyRegistry::register);
		Entities.register();
		registerPredicateBuilders();
		registerFeatureBuilder();
		registerHandlerBuilders();
		registerItemConverters();
	}

	private void registerItemConverters() {
		var settingRegistry = ItemRegistries.SETTING_PROCESSOR;
		var groupRegistry = ItemRegistries.GROUP_CONVERTER;

		var converterRegistry = ItemRegistries.STATE_CONVERTER;
		var factory = new RegistryFactory<>(groupRegistry);

		RegistryUtils.register(settingRegistry, ItemSettingRegistrars::new);
		RegistryUtils.register(groupRegistry, ItemGroupRegisters::new);
		groupRegistry.register("forgero:default", (state) -> ItemGroup.MISC);

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
		PredicateFactory.register(new StringModelBuilder());
		PredicateFactory.register(new StringIdentifierBuilder());
		PredicateFactory.register(new StringModelBuilder());
		PredicateFactory.register(new StringSlotBuilder());
		PredicateFactory.register(StringTypeBuilder::new);
		PredicateFactory.register(StringNameBuilder::new);
		PredicateFactory.register(StringSlotCategoryBuilder::new);
		PredicateFactory.register(DamagePercentagePredicate.DamagePercentagePredicateBuilder::new);
		PredicateFactory.register(EntityPredicateMatcher.EntityPredicateBuilder::new);
		PredicateFactory.register(BlockPredicateMatcher.BlockPredicateBuilder::new);
		PredicateFactory.register(WeatherPredicate.WeatherPredicateBuilder::new);
		PredicateFactory.register(CanMineFilter.CanMineFilterBuilder::new);
		PredicateFactory.register(RandomPredicate.RandomPredicatePredicateBuilder::new);
		PredicateFactory.register(BowPullPredicate.BowPullPredicateBuilder::new);

		//Writers
		PredicateWriterFactory.register(EntityPredicateMatcher.EntityPredicateWriter::builder);
		PredicateWriterFactory.register(BlockPredicateMatcher.BlockPredicateWriter::builder);
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

		//On hit entity
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, StatusEffectHandler.TYPE, StatusEffectHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, ExplosionHandler.TYPE, ExplosionHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, LightningStrikeHandler.TYPE, LightningStrikeHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, FireHandler.TYPE, FireHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, ConvertHandler.TYPE, ConvertHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, KnockbackHandler.TYPE, KnockbackHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, SummonHandler.TYPE, SummonHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityTargetHandler.KEY, MagneticHandler.TYPE, MagneticHandler.BUILDER);

		//On hit block
		HandlerBuilderRegistry.register(BlockTargetHandler.KEY, SummonHandler.TYPE, SummonHandler.BUILDER);
		HandlerBuilderRegistry.register(BlockTargetHandler.KEY, ExplosionHandler.TYPE, ExplosionHandler.BUILDER);
		HandlerBuilderRegistry.register(BlockTargetHandler.KEY, MagneticHandler.TYPE, MagneticHandler.BUILDER);
		HandlerBuilderRegistry.register(BlockTargetHandler.KEY, FireHandler.TYPE, FireHandler.BUILDER);

		// After use
		HandlerBuilderRegistry.register(AfterUseHandler.KEY, ConsumeStackHandler.TYPE, ConsumeStackHandler.BUILDER);
		HandlerBuilderRegistry.register(AfterUseHandler.KEY, ConsumeUpgradeHandler.TYPE, ConsumeUpgradeHandler.BUILDER);
		HandlerBuilderRegistry.register(AfterUseHandler.KEY, DamageHandler.TYPE, DamageHandler.BUILDER);
		HandlerBuilderRegistry.register(AfterUseHandler.KEY, CoolDownHandler.TYPE, CoolDownHandler.BUILDER);

		// On entity tick
		HandlerBuilderRegistry.register(EntityBasedHandler.KEY, MagneticHandler.TYPE, MagneticHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityBasedHandler.KEY, SummonHandler.TYPE, SummonHandler.BUILDER);

		// Block selectors
		HandlerBuilderRegistry.register(BlockSelector.KEY, ColumnSelector.TYPE, ColumnSelector.BUILDER);
		HandlerBuilderRegistry.register(BlockSelector.KEY, PatternSelector.TYPE, PatternSelector.BUILDER);
		HandlerBuilderRegistry.register(BlockSelector.KEY, RadiusVeinSelector.TYPE, RadiusVeinSelector.BUILDER);

		// Hardness calculators
		HandlerBuilderRegistry.register(BlockBreakSpeedCalculator.KEY, All.TYPE, All.BUILDER);
		HandlerBuilderRegistry.register(BlockBreakSpeedCalculator.KEY, Average.TYPE, Average.BUILDER);
		HandlerBuilderRegistry.register(BlockBreakSpeedCalculator.KEY, Diminishing.TYPE, Diminishing.BUILDER);
		HandlerBuilderRegistry.register(BlockBreakSpeedCalculator.KEY, Instant.TYPE, Instant.BUILDER);
		HandlerBuilderRegistry.register(BlockBreakSpeedCalculator.KEY, Single.TYPE, Single.BUILDER);

		// Use handlers
		HandlerBuilderRegistry.builder(Consume.TYPE, Consume.BUILDER)
				.register(BlockUseHandler.KEY)
				.register(EntityUseHandler.KEY)
				.register(UseHandler.KEY);
		// Entity use handlers

		// Block use handlers

		// Stop use handlers
		HandlerBuilderRegistry.register(StopHandler.KEY, ThrowTridentHandler.TYPE, ThrowTridentHandler.BUILDER);
		HandlerBuilderRegistry.register(StopHandler.KEY, ThrowableHandler.TYPE, ThrowableHandler.BUILDER);
		HandlerBuilderRegistry.register(StopHandler.KEY, ConsumeStackHandler.TYPE, ConsumeStackHandler.BUILDER);
		HandlerBuilderRegistry.register(StopHandler.KEY, ConsumeUpgradeHandler.TYPE, ConsumeUpgradeHandler.BUILDER);
		HandlerBuilderRegistry.register(StopHandler.KEY, DamageHandler.TYPE, DamageHandler.BUILDER);
		HandlerBuilderRegistry.register(StopHandler.KEY, CoolDownHandler.TYPE, CoolDownHandler.BUILDER);

		// Block filters
		// Soonish
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
