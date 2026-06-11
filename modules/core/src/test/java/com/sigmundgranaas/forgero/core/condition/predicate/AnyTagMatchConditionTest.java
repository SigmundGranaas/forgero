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

class AnyTagMatchConditionTest extends ForgeroTest {

	@Test
	void conditionPassesWhenSelfHasAnyTag() {
		TagResolver resolver = new TagGraph(Map.of());
		Component iron = material(IRON_ID, METAL_TAG);

		AnyTagMatchCondition condition = new AnyTagMatchCondition(
				id("forgero:self_has_any_tag"),
				List.of(METAL_TAG, WOOD_TAG, GEM_TAG),
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertTrue(condition.test(context), "Condition should pass when self has at least one tag");
	}

	@Test
	void conditionFailsWhenSelfHasNoneOfTheTags() {
		TagResolver resolver = new TagGraph(Map.of());
		Component iron = material(IRON_ID, METAL_TAG);

		AnyTagMatchCondition condition = new AnyTagMatchCondition(
				id("forgero:self_has_any_tag"),
				List.of(WOOD_TAG, GEM_TAG),
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertFalse(condition.test(context), "Condition should fail when self has none of the tags");
	}

	@Test
	void conditionPassesWhenRootHasAnyTag() {
		TagResolver resolver = new TagGraph(Map.of());
		Component pickaxe = part(PICKAXE_ID).withTag(METAL_TAG).build();
		Component handle = material(HANDLE_ID, WOOD_TAG);

		AnyTagMatchCondition condition = new AnyTagMatchCondition(
				id("forgero:root_has_any_tag"),
				List.of(METAL_TAG, GEM_TAG),
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(handle, pickaxe);

		assertTrue(condition.test(context), "Condition should pass when root has at least one tag");
	}

	@Test
	void conditionFailsWhenRootHasNoneOfTheTags() {
		TagResolver resolver = new TagGraph(Map.of());
		Component pickaxe = part(PICKAXE_ID).withTag(METAL_TAG).build();
		Component handle = material(HANDLE_ID, WOOD_TAG);

		AnyTagMatchCondition condition = new AnyTagMatchCondition(
				id("forgero:root_has_any_tag"),
				List.of(WOOD_TAG, GEM_TAG),
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(handle, pickaxe);

		assertFalse(condition.test(context), "Condition should fail when root has none of the tags");
	}

	@Test
	void conditionFailsWhenTagListIsEmpty() {
		TagResolver resolver = new TagGraph(Map.of());
		Component iron = material(IRON_ID, METAL_TAG);

		AnyTagMatchCondition condition = new AnyTagMatchCondition(
				id("forgero:self_has_any_tag"),
				List.of(),
				() -> resolver
		);

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertFalse(condition.test(context), "Condition should fail when tag list is empty");
	}

	@Test
	void conditionFailsWhenResolverIsNull() {
		Component iron = material(IRON_ID, METAL_TAG);

		AnyTagMatchCondition condition = new AnyTagMatchCondition(
				id("forgero:self_has_any_tag"),
				List.of(METAL_TAG),
				() -> null
		);

		ResolutionContext context = new ResolutionContext(iron, iron);

		assertFalse(condition.test(context), "Condition should fail when TagResolver is null");
	}

	@Test
	void hasCorrectType() {
		AnyTagMatchCondition condition = new AnyTagMatchCondition(
				id("forgero:self_has_any_tag"),
				List.of(METAL_TAG),
				() -> null
		);

		assertEquals(id("forgero:self_has_any_tag"), condition.type());
	}

	@Test
	void hasCorrectTags() {
		List<OpenIdentifier> tags = List.of(METAL_TAG, WOOD_TAG);

		AnyTagMatchCondition condition = new AnyTagMatchCondition(
				id("forgero:self_has_any_tag"),
				tags,
				() -> null
		);

		assertEquals(tags, condition.tags());
	}
}
