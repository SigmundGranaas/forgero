package com.sigmundgranaas.forgero.core.condition.predicate;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class TagMatchConditionTest extends ForgeroTest {

	@Test
	void selfHasTagPassesWhenComponentHasTag() {
		TagResolver resolver = new TagGraph(Map.of());
		Component iron = material(IRON_ID, METAL_TAG);

		TagMatchCondition condition = new TagMatchCondition(
				id("forgero:self_has_tag"),
				METAL_TAG,
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertTrue(condition.test(context), "Condition should pass when self has the tag");
	}

	@Test
	void selfHasTagFailsWhenComponentLacksTag() {
		TagResolver resolver = new TagGraph(Map.of());
		Component oak = material(OAK_ID, WOOD_TAG);

		TagMatchCondition condition = new TagMatchCondition(
				id("forgero:self_has_tag"),
				METAL_TAG,
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(oak, oak);

		assertFalse(condition.test(context), "Condition should fail when self lacks the tag");
	}

	@Test
	void rootHasTagPassesWhenRootHasTag() {
		TagResolver resolver = new TagGraph(Map.of());
		Component pickaxe = part(PICKAXE_ID).withTag(METAL_TAG).build();
		Component handle = material(HANDLE_ID, WOOD_TAG);

		TagMatchCondition condition = new TagMatchCondition(
				id("forgero:root_has_tag"),
				METAL_TAG,
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(handle, pickaxe);

		assertTrue(condition.test(context), "Condition should pass when root has the tag");
	}

	@Test
	void rootHasTagFailsWhenRootLacksTag() {
		TagResolver resolver = new TagGraph(Map.of());
		Component pickaxe = part(PICKAXE_ID).withTag(WOOD_TAG).build();
		Component handle = material(HANDLE_ID, WOOD_TAG);

		TagMatchCondition condition = new TagMatchCondition(
				id("forgero:root_has_tag"),
				METAL_TAG,
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(handle, pickaxe);

		assertFalse(condition.test(context), "Condition should fail when root lacks the tag");
	}

	@Test
	void failsWhenTagResolverIsNull() {
		Component iron = material(IRON_ID, METAL_TAG);

		TagMatchCondition condition = new TagMatchCondition(
				id("forgero:self_has_tag"),
				METAL_TAG,
				() -> null
		);

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertFalse(condition.test(context), "Condition should fail when TagResolver is null");
	}

	@Test
	void failsForUnrecognizedType() {
		TagResolver resolver = new TagGraph(Map.of());
		Component iron = material(IRON_ID, METAL_TAG);

		TagMatchCondition condition = new TagMatchCondition(
				id("forgero:unknown_type"),
				METAL_TAG,
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertFalse(condition.test(context), "Condition should fail for unrecognized type");
	}

	@Test
	void hasCorrectType() {
		TagMatchCondition condition = new TagMatchCondition(
				id("forgero:self_has_tag"),
				METAL_TAG,
				() -> null
		);

		assertEquals(id("forgero:self_has_tag"), condition.type());
	}

	@Test
	void hasCorrectTag() {
		TagMatchCondition condition = new TagMatchCondition(
				id("forgero:self_has_tag"),
				METAL_TAG,
				() -> null
		);

		assertEquals(METAL_TAG, condition.tag());
	}
}
