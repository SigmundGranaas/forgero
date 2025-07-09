package com.sigmundgranaas.forgero.data.codec;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.condition.*;
import com.sigmundgranaas.forgero.data.loading.api.data.loader.ConditionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sigmundgranaas.forgero.data.Utils.id;
import static org.junit.jupiter.api.Assertions.*;

class ConditionMapperTest extends ForgeroTest {

	private ConditionMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new ConditionMapper(idFactory);
	}

	@Test
	void testMapNullConditionData() {
		Condition condition = mapper.apply(null);
		assertSame(Condition.ALWAYS_TRUE, condition);
	}

	@Test
	void testMapEmptyConditionData() {
		ConditionData data = new ConditionData(List.of());
		Condition condition = mapper.apply(data);
		assertSame(Condition.ALWAYS_TRUE, condition);
	}

	@Test
	void testMapSingleStaticPredicate() {
		TagMatchPredicateData predicateData = new TagMatchPredicateData(id("forgero:self_has_tag"), id("forgero:metal"));
		ConditionData data = new ConditionData(List.of(predicateData));

		Condition condition = mapper.apply(data);

		assertNotSame(Condition.ALWAYS_TRUE, condition);
		assertEquals(1, condition.staticConditions().size());
		assertTrue(condition.dynamicConditions().isEmpty());
	}

	@Test
	void testMapMultipleStaticPredicates() {
		TagMatchPredicateData predicate1 = new TagMatchPredicateData(id("forgero:self_has_tag"), id("forgero:metal"));
		TagMatchPredicateData predicate2 = new TagMatchPredicateData(id("forgero:root_has_tag"), id("forgero:pickaxe"));
		ConditionData data = new ConditionData(List.of(predicate1, predicate2));

		Condition condition = mapper.apply(data);

		assertEquals(2, condition.staticConditions().size());
		assertTrue(condition.dynamicConditions().isEmpty());
	}

	@Test
	void testMapNestedAndPredicate() {
		TagMatchPredicateData child1 = new TagMatchPredicateData(id("forgero:self_has_tag"), id("forgero:metal"));
		TagMatchPredicateData child2 = new TagMatchPredicateData(id("forgero:root_has_tag"), id("forgero:pickaxe"));
		AndPredicateData andPredicate = new AndPredicateData(id("forgero:and"), List.of(child1, child2));
		ConditionData data = new ConditionData(List.of(andPredicate));

		Condition condition = mapper.apply(data);
		assertEquals(1, condition.staticConditions().size());
		// The internal logic of the AND condition would need a mock ResolutionContext to test fully,
		// but we can verify that a single StaticCondition object was created.
	}

	@Test
	void testIgnoresDynamicPredicatesForNow() {
		// Example of a future dynamic predicate
		// For now, we simulate it with a predicate that doesn't map to a static one.
		PredicateData dynamicPredicate = () -> id("forgero:on_fire");
		ConditionData data = new ConditionData(List.of(dynamicPredicate));

		Condition condition = mapper.apply(data);

		assertTrue(condition.staticConditions().isEmpty());
		assertTrue(condition.dynamicConditions().isEmpty(), "No dynamic mappers are implemented, so list should be empty");
	}
}
