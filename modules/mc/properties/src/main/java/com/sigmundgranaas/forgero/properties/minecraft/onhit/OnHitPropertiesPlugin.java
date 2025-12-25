package com.sigmundgranaas.forgero.properties.minecraft.onhit;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.effects.EffectCodecRegistry;
import com.sigmundgranaas.forgero.effects.entity.ConvertHandler;
import com.sigmundgranaas.forgero.effects.entity.DisarmHandler;
import com.sigmundgranaas.forgero.effects.entity.ExplosionHandler;
import com.sigmundgranaas.forgero.effects.entity.FireHandler;
import com.sigmundgranaas.forgero.effects.entity.FreezeHandler;
import com.sigmundgranaas.forgero.effects.entity.FunctionExecuteHandler;
import com.sigmundgranaas.forgero.effects.entity.KnockbackHandler;
import com.sigmundgranaas.forgero.effects.entity.LifeStealHandler;
import com.sigmundgranaas.forgero.effects.entity.LightningHandler;
import com.sigmundgranaas.forgero.effects.entity.MagnetHandler;
import com.sigmundgranaas.forgero.effects.entity.ModifyBlockHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.effects.entity.ParticleHandler;
import com.sigmundgranaas.forgero.effects.entity.SoundHandler;
import com.sigmundgranaas.forgero.effects.entity.SpawnEntityHandler;
import com.sigmundgranaas.forgero.effects.entity.StatusEffectHandler;
import com.sigmundgranaas.forgero.effects.entity.TeleportHandler;
import com.sigmundgranaas.forgero.effects.entity.VelocityHandler;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.*;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EnvironmentFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityStateFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.AreaOfEffectSelector;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.ChainSelector;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.ConeSelector;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.EntitySelector;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.SingleTargetSelector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OnHitPropertiesPlugin implements DataPlugin {

	private static final Map<String, Codec<? extends OnHitEffect>> EFFECTS = new ConcurrentHashMap<>();
	private static final Map<String, Codec<? extends EntitySelector>> SELECTORS = new ConcurrentHashMap<>();
	private static final Map<String, Codec<? extends EntityFilter>> FILTERS = new ConcurrentHashMap<>();


	static {
		// EntityEffect Handlers (target only)
		registerEffect(FireHandler.TYPE, FireHandler.CODEC);
		registerEffect(LightningHandler.TYPE, LightningHandler.CODEC);
		registerEffect(StatusEffectHandler.TYPE, StatusEffectHandler.CODEC);
		registerEffect(FreezeHandler.TYPE, FreezeHandler.CODEC);

		// ContextualEffect Handlers (source and target)
		registerEffect(LifeStealHandler.TYPE, LifeStealHandler.CODEC);
		registerEffect(KnockbackHandler.TYPE, KnockbackHandler.CODEC);
		registerEffect(ExplosionHandler.TYPE, ExplosionHandler.CODEC);
		registerEffect(ConvertHandler.TYPE, ConvertHandler.CODEC);
		registerEffect(DisarmHandler.TYPE, DisarmHandler.CODEC);
		registerEffect(SoundHandler.TYPE, SoundHandler.CODEC);
		registerEffect(ParticleHandler.TYPE, ParticleHandler.CODEC);
		registerEffect(VelocityHandler.TYPE, VelocityHandler.CODEC);
		registerEffect(MagnetHandler.TYPE, MagnetHandler.CODEC);
		registerEffect(SpawnEntityHandler.TYPE, SpawnEntityHandler.CODEC);
		registerEffect(ModifyBlockHandler.TYPE, ModifyBlockHandler.CODEC);
		registerEffect(TeleportHandler.TYPE, TeleportHandler.CODEC);
		registerEffect(FunctionExecuteHandler.TYPE, FunctionExecuteHandler.CODEC);

		// Selectors
		registerSelector(SingleTargetSelector.TYPE, SingleTargetSelector.CODEC);
		registerSelector(AreaOfEffectSelector.TYPE, AreaOfEffectSelector.CODEC);
		registerSelector(ConeSelector.TYPE, ConeSelector.CODEC);
		registerSelector(ChainSelector.TYPE, ChainSelector.CODEC);

		// Basic Filters
		registerFilter(IsAliveFilter.TYPE, IsAliveFilter.CODEC);
		registerFilter(IsHostileFilter.TYPE, IsHostileFilter.CODEC);
		registerFilter(IsTeammateFilter.TYPE, IsTeammateFilter.CODEC);
		registerFilter(HasTagFilter.TYPE, HasTagFilter.CODEC);
		registerFilter(EntityTypeFilter.TYPE, EntityTypeFilter.CODEC);
		registerFilter(IsPlayerFilter.TYPE, IsPlayerFilter.CODEC);

		// State-based Filters
		registerFilter(IsBurningFilter.TYPE, IsBurningFilter.CODEC);
		registerFilter(IsInWaterFilter.TYPE, IsInWaterFilter.CODEC);
		registerFilter(HasEffectFilter.TYPE, HasEffectFilter.CODEC);

		// Advanced Filters
		registerFilter(HealthThresholdFilter.TYPE, HealthThresholdFilter.CODEC);
		registerFilter(DistanceFilter.TYPE, DistanceFilter.CODEC);
		registerFilter(RandomChanceFilter.TYPE, RandomChanceFilter.CODEC);

		// Composite Filters
		registerFilter(AndFilter.TYPE, AndFilter.CODEC);
		registerFilter(OrFilter.TYPE, OrFilter.CODEC);
		registerFilter(NotFilter.TYPE, NotFilter.CODEC);

		// New Filters - Phase 4
		registerFilter(EnvironmentFilter.TYPE, EnvironmentFilter.CODEC);
		registerFilter(EntityStateFilter.TYPE, EntityStateFilter.CODEC);
	}

	public static void registerEffect(String type, Codec<? extends OnHitEffect> codec) {
		EFFECTS.put(type, codec);
		EffectCodecRegistry.registerOnHitEffect(type, codec);
	}

	/**
	 * @deprecated Use {@link EffectCodecRegistry#getOnHitEffectCodec(String)} instead
	 */
	@Deprecated
	public static Codec<? extends OnHitEffect> getEffectCodec(String type) {
		return EFFECTS.get(type);
	}

	public static void registerSelector(String type, Codec<? extends EntitySelector> codec) {
		SELECTORS.put(type, codec);
	}

	public static Codec<? extends EntitySelector> getSelectorCodec(String type) {
		return SELECTORS.get(type);
	}

	public static void registerFilter(String type, Codec<? extends EntityFilter> codec) {
		FILTERS.put(type, codec);
	}

	public static Codec<? extends EntityFilter> getFilterCodec(String type) {
		return FILTERS.get(type);
	}

	@Override
	public void register(PluginRegistrationContext context) {
		context.registerPropertyCodec(
				OnHitProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(OnHitProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public String getId() {
		return "forgero:on-hit-properties";
	}
}
