package com.sigmundgranaas.forgero.core.condition.predicate;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class AllTagsMatchConditionTest extends ForgeroTest {

	private static final OpenIdentifier DURABLE_TAG = id("forgero:durable");

	@Test
	void conditionPassesWhenSelfHasAllTags() {
		TagResolver resolver = new TagGraph(Map.of());
		Component iron = part(IRON_ID).withTag(METAL_TAG).withTag(DURABLE_TAG).build();

		AllTagsMatchCondition condition = new AllTagsMatchCondition(
				id("forgero:self_has_all_tags"),
				List.of(METAL_TAG, DURABLE_TAG),
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertTrue(condition.test(context), "Condition should pass when self has all tags");
	}

	@Test
	void conditionFailsWhenSelfMissesOneTag() {
		TagResolver resolver = new TagGraph(Map.of());
		Component iron = material(IRON_ID, METAL_TAG);

		AllTagsMatchCondition condition = new AllTagsMatchCondition(
				id("forgero:self_has_all_tags"),
				List.of(METAL_TAG, DURABLE_TAG),
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertFalse(condition.test(context), "Condition should fail when self is missing one tag");
	}

	@Test
	void conditionPassesWhenRootHasAllTags() {
		TagResolver resolver = new TagGraph(Map.of());
		Component pickaxe = part(PICKAXE_ID).withTag(METAL_TAG).withTag(DURABLE_TAG).build();
		Component handle = material(HANDLE_ID, WOOD_TAG);

		AllTagsMatchCondition condition = new AllTagsMatchCondition(
				id("forgero:root_has_all_tags"),
				List.of(METAL_TAG, DURABLE_TAG),
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(handle, pickaxe);

		assertTrue(condition.test(context), "Condition should pass when root has all tags");
	}

	@Test
	void conditionFailsWhenRootMissesOneTag() {
		TagResolver resolver = new TagGraph(Map.of());
		Component pickaxe = part(PICKAXE_ID).withTag(METAL_TAG).build();
		Component handle = material(HANDLE_ID, WOOD_TAG);

		AllTagsMatchCondition condition = new AllTagsMatchCondition(
				id("forgero:root_has_all_tags"),
				List.of(METAL_TAG, DURABLE_TAG),
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(handle, pickaxe);

		assertFalse(condition.test(context), "Condition should fail when root is missing one tag");
	}

	@Test
	void conditionFailsWhenTagListIsEmpty() {
		TagResolver resolver = new TagGraph(Map.of());
		Component iron = material(IRON_ID, METAL_TAG);

		AllTagsMatchCondition condition = new AllTagsMatchCondition(
				id("forgero:self_has_all_tags"),
				List.of(),
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertFalse(condition.test(context), "Condition should fail when tag list is empty");
	}

	@Test
	void conditionFailsWhenResolverIsNull() {
		Component iron = material(IRON_ID, METAL_TAG);

		AllTagsMatchCondition condition = new AllTagsMatchCondition(
				id("forgero:self_has_all_tags"),
				List.of(METAL_TAG),
				() -> null
		);

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertFalse(condition.test(context), "Condition should fail when TagResolver is null");
	}

	@Test
	void hasCorrectType() {
		AllTagsMatchCondition condition = new AllTagsMatchCondition(
				id("forgero:self_has_all_tags"),
				List.of(METAL_TAG),
				() -> null
		);

		assertEquals(id("forgero:self_has_all_tags"), condition.type());
	}

	@Test
	void hasCorrectTags() {
		List<OpenIdentifier> tags = List.of(METAL_TAG, DURABLE_TAG);

		AllTagsMatchCondition condition = new AllTagsMatchCondition(
				id("forgero:self_has_all_tags"),
				tags,
				() -> null
		);

		assertEquals(tags, condition.tags());
	}
}
