package com.sigmundgranaas.forgero.common.tooltip;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TooltipDescriptorTest {

	private Codec<TooltipDescriptor> codec;

	@BeforeEach
	void setUp() {
		// Use a simple codec that always decodes to ALWAYS_TRUE condition
		Codec<Condition> conditionCodec = Codec.unit(Condition.ALWAYS_TRUE);
		codec = TooltipDescriptor.codec(conditionCodec);
	}

	@Test
	void decodeMinimalDescriptor() {
		String json = """
			{
			  "section": "forgero:description",
			  "template": "tooltip.forgero.iron.description"
			}
			""";

		DataResult<TooltipDescriptor> result = codec.decode(JsonOps.INSTANCE, JsonParser.parseString(json))
				.map(Pair::getFirst);

		assertTrue(result.result().isPresent(), "Failed to decode: " + result.error());
		TooltipDescriptor descriptor = result.result().get();

		assertEquals(OpenIdentifier.parse("forgero:description"), descriptor.section());
		assertEquals("tooltip.forgero.iron.description", descriptor.template());
		assertEquals(TooltipDescriptor.DisplayFormat.TEXT, descriptor.format());
		assertEquals(0, descriptor.priority());
		assertNull(descriptor.target());
		assertTrue(descriptor.metadata().isEmpty());
	}

	@Test
	void decodeFullDescriptor() {
		String json = """
			{
			  "section": "forgero:notes",
			  "target": "forgero:fire_aspect",
			  "template": "tooltip.forgero.fire_aspect.note",
			  "format": "NUMBER",
			  "priority": 10,
			  "metadata": {
			    "args": "duration,damage"
			  }
			}
			""";

		DataResult<TooltipDescriptor> result = codec.decode(JsonOps.INSTANCE, JsonParser.parseString(json))
				.map(Pair::getFirst);

		assertTrue(result.result().isPresent(), "Failed to decode: " + result.error());
		TooltipDescriptor descriptor = result.result().get();

		assertEquals(OpenIdentifier.parse("forgero:notes"), descriptor.section());
		assertEquals(OpenIdentifier.parse("forgero:fire_aspect"), descriptor.target());
		assertEquals("tooltip.forgero.fire_aspect.note", descriptor.template());
		assertEquals(TooltipDescriptor.DisplayFormat.NUMBER, descriptor.format());
		assertEquals(10, descriptor.priority());
		assertEquals("duration,damage", descriptor.metadata().get("args"));
	}

	@Test
	void encodeDescriptorRoundTrips() {
		TooltipDescriptor original = new TooltipDescriptor(
				OpenIdentifier.parse("forgero:attributes"),
				OpenIdentifier.parse("forgero:attack_damage"),
				"tooltip.forgero.attack_damage",
				TooltipDescriptor.DisplayFormat.COMPARISON,
				5,
				Map.of("show_baseline", "true"),
				null
		);

		DataResult<JsonElement> encodeResult = codec.encodeStart(JsonOps.INSTANCE, original);
		assertTrue(encodeResult.result().isPresent(), "Failed to encode: " + encodeResult.error());

		DataResult<TooltipDescriptor> decodeResult = codec.decode(JsonOps.INSTANCE, encodeResult.result().get())
				.map(Pair::getFirst);
		assertTrue(decodeResult.result().isPresent(), "Failed to decode: " + decodeResult.error());

		TooltipDescriptor decoded = decodeResult.result().get();
		assertEquals(original.section(), decoded.section());
		assertEquals(original.target(), decoded.target());
		assertEquals(original.template(), decoded.template());
		assertEquals(original.format(), decoded.format());
		assertEquals(original.priority(), decoded.priority());
		assertEquals(original.metadata(), decoded.metadata());
	}

	@Test
	void allDisplayFormatsDeserialize() {
		for (TooltipDescriptor.DisplayFormat format : TooltipDescriptor.DisplayFormat.values()) {
			String json = String.format("""
				{
				  "section": "forgero:test",
				  "template": "test.key",
				  "format": "%s"
				}
				""", format.name());

			DataResult<TooltipDescriptor> result = codec.decode(JsonOps.INSTANCE, JsonParser.parseString(json))
					.map(Pair::getFirst);

			assertTrue(result.result().isPresent(),
					"Failed to decode format " + format + ": " + result.error());
			assertEquals(format, result.result().get().format());
		}
	}

	@Test
	void getTranslationArgNamesParsesList() {
		TooltipDescriptor descriptor = new TooltipDescriptor(
				OpenIdentifier.parse("forgero:notes"),
				null,
				"test.key",
				TooltipDescriptor.DisplayFormat.TEXT,
				0,
				Map.of("args", "damage,speed,durability"),
				null
		);

		List<String> args = descriptor.getTranslationArgNames();

		assertEquals(3, args.size());
		assertEquals("damage", args.get(0));
		assertEquals("speed", args.get(1));
		assertEquals("durability", args.get(2));
	}

	@Test
	void getTranslationArgNamesReturnsEmptyForNoArgs() {
		TooltipDescriptor descriptor = new TooltipDescriptor(
				OpenIdentifier.parse("forgero:description"),
				"test.key"
		);

		List<String> args = descriptor.getTranslationArgNames();

		assertTrue(args.isEmpty());
	}

	@Test
	void hasTargetReturnsCorrectly() {
		TooltipDescriptor withTarget = new TooltipDescriptor(
				OpenIdentifier.parse("forgero:notes"),
				OpenIdentifier.parse("forgero:sharpness"),
				"test.key",
				TooltipDescriptor.DisplayFormat.TEXT
		);

		TooltipDescriptor withoutTarget = new TooltipDescriptor(
				OpenIdentifier.parse("forgero:description"),
				"test.key"
		);

		assertTrue(withTarget.hasTarget());
		assertFalse(withoutTarget.hasTarget());
	}

	@Test
	void getMetadataReturnsValue() {
		TooltipDescriptor descriptor = new TooltipDescriptor(
				OpenIdentifier.parse("forgero:attributes"),
				null,
				"test.key",
				TooltipDescriptor.DisplayFormat.TEXT,
				0,
				Map.of("custom_key", "custom_value"),
				null
		);

		assertTrue(descriptor.getMetadata("custom_key").isPresent());
		assertEquals("custom_value", descriptor.getMetadata("custom_key").get());
		assertFalse(descriptor.getMetadata("nonexistent").isPresent());
	}
}
