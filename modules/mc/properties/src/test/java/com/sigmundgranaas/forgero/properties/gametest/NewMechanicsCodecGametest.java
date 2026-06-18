package com.sigmundgranaas.forgero.properties.gametest;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.effects.entity.FireHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.effects.mark.ClearMarkHandler;
import com.sigmundgranaas.forgero.effects.mark.MarkHandler;
import com.sigmundgranaas.forgero.effects.zone.CreateZoneHandler;
import com.sigmundgranaas.forgero.effects.zone.OwnerMode;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.FacingAwayFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.HasMarkFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.IsAirborneFilter;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.LineOfSightFilter;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

/**
 * Integration: the new effects/filters are registered in the dispatch codecs and parse from JSON
 * exactly as content authors would write them. Construction-level tests elsewhere bypass the
 * codecs; these exercise the {@code "type"} dispatch + field parsing end-to-end.
 */
public class NewMechanicsCodecGametest {

	private static OnHitEffect effect(String json) {
		JsonElement element = JsonParser.parseString(json);
		return OnHitEffect.CODEC.parse(JsonOps.INSTANCE, element).result().orElseThrow();
	}

	private static EntityFilter filter(String json) {
		JsonElement element = JsonParser.parseString(json);
		return EntityFilter.CODEC.parse(JsonOps.INSTANCE, element).result().orElseThrow();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testMarkEffectsParse(TestContext context) {
		OnHitEffect mark = effect("{\"type\":\"forgero:mark\",\"mark\":\"forgero:hexed\",\"duration\":100}");
		context.assertTrue(mark instanceof MarkHandler, "Should parse to MarkHandler");
		MarkHandler markHandler = (MarkHandler) mark;
		context.assertTrue(markHandler.mark().equals(new Identifier("forgero", "hexed")), "mark id should parse");
		context.assertTrue(markHandler.duration() == 100, "duration should parse");

		OnHitEffect clear = effect("{\"type\":\"forgero:clear_mark\",\"mark\":\"forgero:hexed\"}");
		context.assertTrue(clear instanceof ClearMarkHandler, "Should parse to ClearMarkHandler");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testCreateZoneEffectParsesWithNestedEffects(TestContext context) {
		OnHitEffect zone = effect("{\"type\":\"forgero:create_zone\",\"radius\":3,\"duration\":40,\"interval\":10,"
				+ "\"owner_mode\":\"target_as_source\","
				+ "\"effects\":[{\"type\":\"forgero:fire\",\"duration\":2}]}");
		context.assertTrue(zone instanceof CreateZoneHandler, "Should parse to CreateZoneHandler");
		CreateZoneHandler handler = (CreateZoneHandler) zone;
		context.assertTrue(handler.radius() == 3 && handler.duration() == 40 && handler.interval() == 10,
				"Zone scalar fields should parse");
		context.assertTrue(handler.ownerMode() == OwnerMode.TARGET_AS_SOURCE, "owner_mode enum should parse");
		context.assertTrue(handler.effects().size() == 1 && handler.effects().get(0) instanceof FireHandler,
				"Nested effect should dispatch-parse to FireHandler");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
	public void testPositionalFiltersParse(TestContext context) {
		context.assertTrue(filter("{\"type\":\"forgero:has_mark\",\"mark\":\"forgero:hexed\"}") instanceof HasMarkFilter,
				"has_mark should parse");
		context.assertTrue(filter("{\"type\":\"forgero:is_airborne\"}") instanceof IsAirborneFilter,
				"is_airborne should parse");
		context.assertTrue(filter("{\"type\":\"forgero:line_of_sight\"}") instanceof LineOfSightFilter,
				"line_of_sight should parse");

		EntityFilter facing = filter("{\"type\":\"forgero:facing_away\",\"min_angle\":120}");
		context.assertTrue(facing instanceof FacingAwayFilter && ((FacingAwayFilter) facing).minAngle() == 120.0f,
				"facing_away should parse with min_angle");
		context.complete();
	}
}
