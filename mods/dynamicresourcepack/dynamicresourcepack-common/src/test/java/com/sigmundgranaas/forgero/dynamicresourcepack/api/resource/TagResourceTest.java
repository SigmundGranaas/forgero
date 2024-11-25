package com.sigmundgranaas.forgero.dynamicresourcepack.api.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import net.minecraft.util.Identifier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TagResourceTest {
	@Test
	void testFactoryMethods() {
		TagResource basic = TagResource.create();
		assertFalse(basic.shouldReplace());

		TagResource replacing = TagResource.createReplacingTag();
		assertTrue(replacing.shouldReplace());

		TagResource withEntries = TagResource.of(
				new Identifier("minecraft:stone"),
				new Identifier("minecraft:dirt")
		);
		assertEquals(2, withEntries.getEntries().size());
	}

	@Test
	void testTagValidation() {
		TagResource tag = TagResource.create();

		assertThrows(IllegalArgumentException.class, () ->
				tag.entry(new Identifier("minecraft:#stone")));

		assertThrows(IllegalArgumentException.class, () ->
				tag.tag(new Identifier("minecraft:stone")));

		// These should work fine
		tag.tag(new Identifier("minecraft:#stone"));
		tag.entry(new Identifier("minecraft:stone"));
	}

	@Test
	void testRoundTripSerialization() {
		TagResource original = TagResource.createReplacingTag();
		original.entry(new Identifier("minecraft:stone"));
		original.optionalEntry(new Identifier("minecraft:dirt"));
		original.tag(new Identifier("minecraft:#blocks"));
		original.optionalTag(new Identifier("minecraft:#decorative"));

		// Encode to JSON
		JsonElement encoded = TagResource.CODEC.encode(original, JsonOps.INSTANCE, JsonOps.INSTANCE.empty())
				.result().orElseThrow();

		// Decode back
		TagResource decoded = TagResource.CODEC.decode(JsonOps.INSTANCE, encoded)
				.result().orElseThrow().getFirst();

		// Verify
		assertEquals(original.shouldReplace(), decoded.shouldReplace());
		assertEquals(original.getEntries().size(), decoded.getEntries().size());

		for (int i = 0; i < original.getEntries().size(); i++) {
			assertEquals(
					original.getEntries().get(i).id(),
					decoded.getEntries().get(i).id()
			);
			assertEquals(
					original.getEntries().get(i).required(),
					decoded.getEntries().get(i).required()
			);
		}
	}

	@Test
	void testJsonFormat() {
		TagResource tag = TagResource.createReplacingTag();
		tag.entry(new Identifier("minecraft:stone"));
		tag.optionalTag(new Identifier("minecraft:#decorative"));

		JsonElement json = TagResource.CODEC.encode(tag, JsonOps.INSTANCE, JsonOps.INSTANCE.empty())
				.result().orElseThrow();

		String expected = """
            {
              "replace": true,
              "values": [
                {
                  "id": "minecraft:stone",
                  "required": true
                },
                {
                  "id": "minecraft:#decorative",
                  "required": false
                }
              ]
            }""";

		JsonElement expectedJson = JsonParser.parseString(expected);
		assertEquals(expectedJson, json);
	}
}
