package com.sigmundgranaas.forgero.minecraft.common.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.attribute.BaseAttribute;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.StatusEffectHandler;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

class StatusEffectHandlerTest implements Bootstrapped {
	private StatusEffectHandler handler;
	private StatusEffect effect;
	private Attribute level;
	private Attribute duration;

	@BeforeEach
	void setUp() {
		effect = StatusEffects.POISON;
		level = BaseAttribute.of(2, StatusEffectHandler.EFFECT_LEVEL_ATTRIBUTE_TYPE);
		duration = BaseAttribute.of(100, StatusEffectHandler.EFFECT_DURATION_TYPE);
		handler = new StatusEffectHandler(effect, level, duration, "minecraft:targeted_entity");
	}

	@Test
	void testCodecEncode() {
		JsonObject json = StatusEffectHandler.BUILDER.encodeStart(JsonOps.INSTANCE, handler)
				.result()
				.map(JsonElement::getAsJsonObject)
				.orElseThrow();

		assertEquals("minecraft:poison", json.get("effect").getAsString());
		assertEquals(2, json.get("level").getAsJsonObject().get("value").getAsInt());
		assertEquals(100, json.get("duration").getAsJsonObject().get("value").getAsInt());
		assertEquals("minecraft:targeted_entity", json.get("target").getAsString());
	}

	@Test
	void testCodecDecode() {
		JsonObject json = new JsonObject();
		json.addProperty("effect", "minecraft:poison");
		json.addProperty("level", 2);
		json.addProperty("duration", 100);
		json.addProperty("target", "minecraft:targeted_entity");

		StatusEffectHandler decodedHandler = StatusEffectHandler.BUILDER.parse(JsonOps.INSTANCE, json)
				.result()
				.orElseThrow();

		assertEquals(Registries.STATUS_EFFECT.get(new Identifier("minecraft:poison")), decodedHandler.effect());
		assertEquals(2, decodedHandler.level().compute().asInt());
		assertEquals(100, decodedHandler.duration().compute().asInt());
		assertEquals("minecraft:targeted_entity", decodedHandler.target());
	}

	@Test
	void testCodecDecodeDefaultValues() {
		JsonObject json = new JsonObject();
		json.addProperty("effect", "minecraft:poison");
		json.addProperty("target", "minecraft:targeted_entity");

		StatusEffectHandler decodedHandler = StatusEffectHandler.BUILDER.parse(JsonOps.INSTANCE, json)
				.result()
				.orElseThrow();

		assertEquals(Registries.STATUS_EFFECT.get(new Identifier("minecraft:poison")), decodedHandler.effect());
		assertEquals(1, decodedHandler.level().compute().asInt());
		assertEquals(20 * 30, decodedHandler.duration().compute().asInt());
		assertEquals("minecraft:targeted_entity", decodedHandler.target());
	}

	@Test
	void testInvalidTarget() {
		handler = new StatusEffectHandler(effect, level, duration, "invalid_target");
		// Specifying null as entity to avoid compiler warning due to method overloading
		Entity entity = null;
		//noinspection ConstantValue
		assertThrows(IllegalArgumentException.class, () -> handler.onHit( null, null, entity));
	}

	@Test
	void testNullEffect() {
		assertThrows(NullPointerException.class, () -> new StatusEffectHandler(null, level, duration, "minecraft:targeted_entity"));
	}

	@Test
	void testNullTarget() {
		assertThrows(NullPointerException.class, () -> new StatusEffectHandler(effect, level, duration, null));
	}
}
