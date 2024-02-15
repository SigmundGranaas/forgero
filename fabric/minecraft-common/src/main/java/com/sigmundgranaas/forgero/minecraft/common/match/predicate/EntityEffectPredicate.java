package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Builder;
import lombok.Data;

import net.minecraft.registry.Registries;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.predicate.NumberRange;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

@Data
@Builder
public class EntityEffectPredicate {
	public static final EntityEffectPredicate EMPTY = EntityEffectPredicate.builder().effects(Collections.emptyMap()).build();
	private final Map<StatusEffect, EffectData> effects;

	public static EntityEffectPredicate create() {
		return new EntityEffectPredicate(Maps.newLinkedHashMap());
	}

	public static EntityEffectPredicate fromJson(@Nullable JsonElement json) {
		if (json == null || json.isJsonNull()) {
			return EMPTY;
		}

		JsonObject jsonObject = JsonHelper.asObject(json, "effects");
		Map<StatusEffect, EffectData> effectMap = buildEffectMapFromJson(jsonObject);

		return new EntityEffectPredicate(effectMap);
	}

	private static Map<StatusEffect, EffectData> buildEffectMapFromJson(JsonObject jsonObject) {
		Map<StatusEffect, EffectData> map = new LinkedHashMap<>();

		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			Identifier identifier = new Identifier(entry.getKey());
			StatusEffect statusEffect = getStatusEffect(identifier);
			EffectData effectData = EffectData.fromJson(JsonHelper.asObject(entry.getValue(), entry.getKey()));

			map.put(statusEffect, effectData);
		}

		return map;
	}

	private static StatusEffect getStatusEffect(Identifier identifier) {
		return Registries.STATUS_EFFECT.getOrEmpty(identifier)
				.orElseThrow(() -> new JsonSyntaxException("Unknown effect '" + identifier + "'"));
	}

	public boolean test(Entity entity) {
		if (this == EMPTY) return true;
		return entity instanceof LivingEntity && test(((LivingEntity) entity).getActiveStatusEffects());
	}

	public boolean test(LivingEntity livingEntity) {
		return this == EMPTY || test(livingEntity.getActiveStatusEffects());
	}

	public boolean test(Map<StatusEffect, StatusEffectInstance> effects) {
		if (this == EMPTY) return true;

		for (Map.Entry<StatusEffect, EffectData> entry : this.effects.entrySet()) {
			StatusEffectInstance statusEffectInstance = effects.get(entry.getKey());
			if (!entry.getValue().test(statusEffectInstance)) {
				return false;
			}
		}
		return true;
	}

	@Data
	@Builder
	public static class EffectData {
		private final NumberRange.IntRange amplifier;
		private final NumberRange.IntRange duration;
		@Nullable
		private final Boolean ambient;
		@Nullable
		private final Boolean visible;


		public EffectData(NumberRange.IntRange amplifier, NumberRange.IntRange duration, @Nullable Boolean ambient, @Nullable Boolean visible) {
			this.amplifier = amplifier;
			this.duration = duration;
			this.ambient = ambient;
			this.visible = visible;
		}

		public static EffectData fromJson(JsonObject json) {
			NumberRange.IntRange intRange = NumberRange.IntRange.fromJson(json.get("amplifier"));
			NumberRange.IntRange intRange2 = NumberRange.IntRange.fromJson(json.get("duration"));
			Boolean boolean_ = json.has("ambient") ? JsonHelper.getBoolean(json, "ambient") : null;
			Boolean boolean2 = json.has("visible") ? JsonHelper.getBoolean(json, "visible") : null;
			return new EffectData(intRange, intRange2, boolean_, boolean2);
		}

		public boolean test(@Nullable StatusEffectInstance statusEffectInstance) {
			if (statusEffectInstance == null) {
				return false;
			} else if (!this.amplifier.test(statusEffectInstance.getAmplifier())) {
				return false;
			} else if (!this.duration.test(statusEffectInstance.getDuration())) {
				return false;
			} else if (this.ambient != null && this.ambient != statusEffectInstance.isAmbient()) {
				return false;
			} else {
				return this.visible == null || this.visible == statusEffectInstance.shouldShowParticles();
			}
		}
	}
}
