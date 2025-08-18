package com.sigmundgranaas.forgero.core.property.predicate;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeCodec;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.core.property.context.Key;
import com.sigmundgranaas.forgero.core.property.engine.ResolverEngine;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.condition.StaticCondition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.sigmundgranaas.forgero.data.Utils.id;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.ATTACK_DAMAGE_IDENTIFIER;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test to demonstrate the extensibility of the predicate system.
 * This test simulates a "Minecraft" module plugging in its own custom predicate.
 */
public class PredicateSystemTest extends ForgeroTest {

	// =============================================================================================
	// Step 1: Define the custom predicate record with its own Codec
	// Location: (Simulated) fabric/minecraft-common/src/main/java/.../IsSneakingCondition.java
	// =============================================================================================

	public record IsSneakingCondition(OpenIdentifier type, boolean value) implements DynamicCondition {
		public static final Codec<IsSneakingCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.xmap(OpenIdentifier::new, OpenIdentifier::toString).fieldOf("type").forGetter(IsSneakingCondition::type),
				Codec.BOOL.fieldOf("value").forGetter(IsSneakingCondition::value)
		).apply(instance, IsSneakingCondition::new));

		@Override
		public boolean test(DynamicContext context) {
			boolean isSneaking = context
					.get(MinecraftContextKeys.ENTITY_FLAGS)
					.map(flags -> flags.contains(id("minecraft:is_sneaking")))
					.orElse(false);
			return isSneaking == value;
		}
	}

	/**
	 * Context key used by the platform to pass data into the DynamicContext.
	 */
	public static class MinecraftContextKeys {
		public static final Key<Set<OpenIdentifier>> ENTITY_FLAGS = new Key<>(id("minecraft:entity_flags"));
	}

	// =============================================================================================
	// Test Setup and Execution
	// =============================================================================================

	private ResolverEngine resolver;
	private Component componentWithPlatformPredicate;

	@BeforeEach
	void setUp() {
		// Step 1: Create maps of all known predicate codecs
		Map<String, Codec<? extends StaticCondition>> staticCodecs = new HashMap<>();
		staticCodecs.put("forgero:self_has_tag", TagMatchCondition.CODEC);
		staticCodecs.put("forgero:root_has_tag", TagMatchCondition.CODEC);

		Map<String, Codec<? extends DynamicCondition>> dynamicCodecs = new HashMap<>();
		// Step 2: Register our custom predicate codec
		dynamicCodecs.put("minecraft:is_sneaking", IsSneakingCondition.CODEC);

		// Step 3: Create the master ConditionCodec with all registered predicates
		ConditionCodec conditionCodec = new ConditionCodec(staticCodecs, dynamicCodecs);

		// Step 4: Create services that depend on the ConditionCodec
		Codec<AttributeData> attributeDataCodec = AttributeCodecs.create(conditionCodec);
		var attributePropertyCodec = new AttributeCodec(conditionCodec);

		this.resolver = new ResolverEngine(); // The resolver will use the registered DataTypeEngines.

		// Step 5: Create a component with a property that uses the custom predicate by parsing JSON
		String jsonProperty = """
				{
				    "id": "test_attribute",
				    "type": "forgero:attack_damage",
				    "computation": 10,
				    "condition": {
				        "type": "minecraft:is_sneaking",
				        "value": true
				    }
				}
				""";

		Attribute data = attributePropertyCodec.parse(JsonOps.INSTANCE, JsonParser.parseString(jsonProperty)).getOrThrow(false, msg -> {});
		this.componentWithPlatformPredicate = new StaticComponent(id("test:test_item"), Collections.emptySet(), new HashMap<>());
	}

	@Test
	void testPlatformPredicatePlugin() {
		// Test Case 1: Context matches the predicate (entity is sneaking)
		DynamicContext contextWhenSneaking = new DynamicContext.Builder()
				.put(MinecraftContextKeys.ENTITY_FLAGS, Set.of(id("minecraft:is_sneaking")))
				.build();

		AttributeQueryResult resultWhenSneaking = resolver.resolve(componentWithPlatformPredicate, new AttributeEngine(), contextWhenSneaking);

		float damageWhenSneaking = resultWhenSneaking.getValue(ATTACK_DAMAGE_IDENTIFIER);
		assertEquals(10.0f, damageWhenSneaking, "Attribute should be applied when the custom predicate is met.");

		// Test Case 2: Context does not match the predicate (entity is not sneaking)
		DynamicContext contextWhenNotSneaking = new DynamicContext.Builder()
				.put(MinecraftContextKeys.ENTITY_FLAGS, Collections.emptySet())
				.build();

		AttributeQueryResult resultWhenNotSneaking = resolver.resolve(componentWithPlatformPredicate,  new AttributeEngine(), contextWhenNotSneaking);

		float damageWhenNotSneaking = resultWhenNotSneaking.getValue(ATTACK_DAMAGE_IDENTIFIER);
		assertEquals(0.0f, damageWhenNotSneaking, "Attribute should NOT be applied when the custom predicate is not met.");

		// Test Case 3: Context is missing the required data
		DynamicContext emptyContext = DynamicContext.empty();

		AttributeQueryResult resultWithEmptyContext = resolver.resolve(componentWithPlatformPredicate, new AttributeEngine(), emptyContext);

		float damageWithEmptyContext = resultWithEmptyContext.getValue(ATTACK_DAMAGE_IDENTIFIER);
		assertEquals(0.0f, damageWithEmptyContext, "Attribute should NOT be applied when context is missing required keys.");
	}
}
