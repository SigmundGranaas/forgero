package com.sigmundgranaas.forgero.common.tag.engine;

import com.sigmundgranaas.forgero.common.tags.engine.TagDefinition;
import com.sigmundgranaas.forgero.common.tags.engine.TagParser;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TagParserTest {

	private final TagParser parser = new TagParser();

	@Test
	void canParseParentAsString() {
		String json = """
        {
          "parent": "forgero:part"
        }
        """;
		TagDefinition def = parser.parse(json);
		assertNotNull(def.parents());
		assertEquals(Set.of("forgero:part"), def.parents());
	}

	@Test
	void canParseParentsAsArray() {
		String json = """
        {
          "parents": ["forgero:weapon_head", "forgero:blade"]
        }
        """;
		TagDefinition def = parser.parse(json);
		assertNotNull(def.parents());
		assertEquals(Set.of("forgero:weapon_head", "forgero:blade"), def.parents());
	}

	@Test
	void canParseEmptyOrMissingParents() {
		String jsonWithEmpty = """
        {
          "parents": []
        }
        """;
		TagDefinition def1 = parser.parse(jsonWithEmpty);
		assertNotNull(def1.parents());
		assertTrue(def1.parents().isEmpty());

		String jsonWithNull = """
        {
          "parents": null
        }
        """;
		TagDefinition def2 = parser.parse(jsonWithNull);
		assertNotNull(def2.parents());
		assertTrue(def2.parents().isEmpty());

		String emptyJson = "{}";
		TagDefinition def3 = parser.parse(emptyJson);
		assertNotNull(def3.parents());
		assertTrue(def3.parents().isEmpty());
	}
}
