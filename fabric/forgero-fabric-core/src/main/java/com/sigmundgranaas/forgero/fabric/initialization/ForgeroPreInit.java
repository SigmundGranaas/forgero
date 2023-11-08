package com.sigmundgranaas.forgero.fabric.initialization;

import static com.sigmundgranaas.forgero.minecraft.common.entity.Entities.SOUL_ENTITY;

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
import com.sigmundgranaas.forgero.core.registry.SoulLevelPropertyRegistry;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.SoulLevelPropertyData;
import com.sigmundgranaas.forgero.core.soul.SoulLevelPropertyDataProcessor;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.fabric.registry.DefaultLevelProperties;
import com.sigmundgranaas.forgero.minecraft.common.entity.Entities;
import com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity;
import com.sigmundgranaas.forgero.minecraft.common.match.BowPullPredicate;
import com.sigmundgranaas.forgero.minecraft.common.match.DamagePercentagePredicate;
import com.sigmundgranaas.forgero.minecraft.common.property.handler.PatternBreaking;
import com.sigmundgranaas.forgero.minecraft.common.property.handler.TaggedPatternBreaking;
import com.sigmundgranaas.forgero.minecraft.common.feature.BlockBreakFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.BlockEfficiencyFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.EntityTickFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.OnHitBlockFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.OnHitEntityFeature;
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
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.EntityHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.FunctionExecuteHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.MagneticHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.ParticleHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.SoundHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.SummonHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.ExplosionHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock.OnHitBlockHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.ConvertHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.FireHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.KnockbackHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.LightningStrikeHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.OnHitHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.StatusEffectHandler;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.BlockPredicateMatcher;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.DamagePercentagePredicate;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.EntityPredicateMatcher;
import com.sigmundgranaas.forgero.minecraft.common.match.predicate.WeatherPredicate;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.PredicateWriterFactory;

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
	}

	private void registerPredicateBuilders() {
		PredicateFactory.register(new StringModelBuilder());
		PredicateFactory.register(new StringIdentifierBuilder());
		PredicateFactory.register(new StringModelBuilder());
		PredicateFactory.register(new StringSlotBuilder());
		PredicateFactory.register(new StringTypeBuilder());
		PredicateFactory.register(new StringNameBuilder());
		PredicateFactory.register(StringSlotCategoryBuilder::new);
		PredicateFactory.register(DamagePercentagePredicate.DamagePercentagePredicateBuilder::new);
		PredicateFactory.register(EntityPredicateMatcher.EntityPredicateBuilder::new);
		PredicateFactory.register(BlockPredicateMatcher.BlockPredicateBuilder::new);
		PredicateFactory.register(WeatherPredicate.WeatherPredicateBuilder::new);
		PredicateFactory.register(CanMineFilter.CanMineFilterBuilder::new);

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
	}

	private void registerHandlerBuilders() {
		//On hit entity
		HandlerBuilderRegistry.register(OnHitHandler.KEY, StatusEffectHandler.TYPE, StatusEffectHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitHandler.KEY, ExplosionHandler.TYPE, ExplosionHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitHandler.KEY, LightningStrikeHandler.TYPE, LightningStrikeHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitHandler.KEY, FireHandler.TYPE, FireHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitHandler.KEY, ConvertHandler.TYPE, ConvertHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitHandler.KEY, KnockbackHandler.TYPE, KnockbackHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitHandler.KEY, FunctionExecuteHandler.TYPE, FunctionExecuteHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitHandler.KEY, SummonHandler.TYPE, SummonHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitHandler.KEY, MagneticHandler.TYPE, MagneticHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitHandler.KEY, ParticleHandler.TYPE, ParticleHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitHandler.KEY, SoundHandler.TYPE, SoundHandler.BUILDER);

		//On hit block
		HandlerBuilderRegistry.register(OnHitBlockHandler.KEY, FunctionExecuteHandler.TYPE, FunctionExecuteHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitBlockHandler.KEY, SummonHandler.TYPE, SummonHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitBlockHandler.KEY, ExplosionHandler.TYPE, ExplosionHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitBlockHandler.KEY, MagneticHandler.TYPE, MagneticHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitBlockHandler.KEY, FireHandler.TYPE, FireHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitBlockHandler.KEY, ParticleHandler.TYPE, ParticleHandler.BUILDER);
		HandlerBuilderRegistry.register(OnHitBlockHandler.KEY, SoundHandler.TYPE, SoundHandler.BUILDER);

		// On entity tick
		HandlerBuilderRegistry.register(EntityHandler.KEY, MagneticHandler.TYPE, MagneticHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityHandler.KEY, FunctionExecuteHandler.TYPE, FunctionExecuteHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityHandler.KEY, SummonHandler.TYPE, SummonHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityHandler.KEY, ParticleHandler.TYPE, ParticleHandler.BUILDER);
		HandlerBuilderRegistry.register(EntityHandler.KEY, SoundHandler.TYPE, SoundHandler.BUILDER);

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

		// Block filters
		// Soonish
		PredicateFactory.register(BowPullPredicate.BowPullPredicateBuilder::new);
	}

	private void soulLevelPropertyReloader() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
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
