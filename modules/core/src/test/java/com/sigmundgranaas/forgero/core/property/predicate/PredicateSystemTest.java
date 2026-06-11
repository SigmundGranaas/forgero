package com.sigmundgranaas.forgero.core.property.predicate;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.PrecomputedAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ComponentTraversal;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sigmundgranaas.forgero.data.Utils.id;
import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.attributeEngine;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.ATTACK_DAMAGE_IDENTIFIER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Test to demonstrate the extensibility of the predicate system.
 * This test simulates a "Minecraft" module plugging in its own custom predicate.
 *
 * <p>Core never evaluates dynamic predicates: it parses them from JSON and carries them
 * through compilation as data. Evaluation happens in the game layer (RuntimeConditions in
 * the mc common module), where the platform also defines its runtime context keys.
 */
public class PredicateSystemTest extends ForgeroTest {

	// =============================================================================================
	// Step 1: Define the custom predicate record with its own Codec
	// Location: (Simulated) fabric/minecraft-common/src/main/java/.../IsSneakingCondition.java
	// The game layer would additionally implement the game-side evaluation interface
	// (EvaluableCondition); to core this is opaque data with a type and a codec.
	// =============================================================================================

	public record IsSneakingCondition(OpenIdentifier type, boolean value) implements DynamicCondition {
		public static final Codec<IsSneakingCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.xmap(OpenIdentifier::parse, OpenIdentifier::toString).fieldOf("type").forGetter(IsSneakingCondition::type),
				Codec.BOOL.fieldOf("value").forGetter(IsSneakingCondition::value)
		).apply(instance, IsSneakingCondition::new));
	}

	// =============================================================================================
	// Test Setup and Execution
	// =============================================================================================

	private AttributeEngine engine;
	private Component componentWithPlatformPredicate;

	@BeforeEach
	void setUp() {
		// Step 1: Create maps of all known predicate codecs
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		// Step 2: Register our custom predicate codec
		dynamicCodecs.put("minecraft:is_sneaking", IsSneakingCondition.CODEC);

		// Step 3: Create the master ConditionCodec with all registered predicates
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);

		this.engine = attributeEngine();

		// Step 4: Create a condition object that uses the custom predicate by parsing JSON
		String conditionJson = """
				{
				    "type": "minecraft:is_sneaking",
				    "value": true
				}
				""";

		Condition customCondition = conditionCodec.parse(JsonOps.INSTANCE, JsonParser.parseString(conditionJson))
				.getOrThrow(false, System.err::println);

		// Step 5: Create an Attribute that uses this condition
		Attribute attributeWithCustomCondition = new SimpleAttribute(
				ATTACK_DAMAGE_IDENTIFIER,
				10.0f,
				customCondition
		);

		// Step 6: Create a component holding this attribute
		this.componentWithPlatformPredicate = new StaticComponent(
				id("test:test_item"),
				Collections.emptySet(),
				Map.of(Attribute.KEY.key(), List.of(attributeWithCustomCondition))
		);
	}

	@Test
	void testPlatformPredicatePlugin() {
		// The compiled value never includes dynamic-conditional attributes - core does not
		// evaluate the custom predicate, regardless of any runtime state.
		AttributeQueryResult compiled = engine.resolve(componentWithPlatformPredicate);

		float compiledDamage = compiled.getValue(ATTACK_DAMAGE_IDENTIFIER);
		assertEquals(0.0f, compiledDamage, "Dynamic-conditional attribute must not contribute to the compiled value.");

		// The parsed predicate is carried through compilation as data for the game layer.
		PrecomputedAttribute precomputed = engine
				.bake(ComponentTraversal.traverse(componentWithPlatformPredicate).stream())
				.get(ATTACK_DAMAGE_IDENTIFIER);

		assertEquals(1, precomputed.conditionalAttributes().size(),
				"The dynamic-conditional attribute should be carried as data.");

		Attribute carried = precomputed.conditionalAttributes().get(0);
		List<DynamicCondition> dynamicConditions = carried.condition().orElseThrow().dynamicConditions();
		assertEquals(1, dynamicConditions.size(), "The parsed predicate should be attached to the carried attribute.");

		IsSneakingCondition parsed = assertInstanceOf(IsSneakingCondition.class, dynamicConditions.get(0),
				"The codec-registered predicate type should round-trip through parsing and compilation.");
		assertEquals(true, parsed.value(), "Predicate data should be preserved.");
		assertEquals(OpenIdentifier.parse("minecraft:is_sneaking"), parsed.type(), "Predicate type should be preserved.");
	}
}
