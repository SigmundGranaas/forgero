// Test file: com.sigmundgranaas.forgero.data.mapper.impl.AttributeCodecTest.java

package com.sigmundgranaas.forgero.data.mapper.impl;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult; // <-- ADD THIS IMPORT
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttributeComponent;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.core.property.condition.StaticConditions;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.loading.api.data.condition.ConditionData;
import com.sigmundgranaas.forgero.data.loading.api.data.condition.TagMatchPredicateData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionMapper;
import com.sigmundgranaas.forgero.data.loading.impl.codec.OperatorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class AttributeCodecTest {

	private AttributeCodec codec;
	private ConditionMapper conditionMapper;
	private OperatorMapper operatorMapper;

	@BeforeEach
	void setUp() {
		conditionMapper = new ConditionMapper(idFactory);
		operatorMapper = new OperatorMapper();
		codec = new AttributeCodec(conditionMapper, operatorMapper);
	}

	@Nested
	@DisplayName("Mapping from AttributeData to Property")
	class MappingToProperty {
		@Test
		void buildSimpleAttribute() {
			ComputationData computation = new ComputationData(10, AttributeCodecs.ADDITION_OPERATOR, AttributeCodecs.BASE_ORDER);
			AttributeData data = new AttributeDataImpl(DURABILITY_IDENTIFIER, DURABILITY_IDENTIFIER, computation, null, null);

			List<Property> properties = codec.build(List.of(data));

			assertEquals(1, properties.size());
			assertTrue(properties.get(0) instanceof SimpleAttribute);
			SimpleAttribute attribute = (SimpleAttribute) properties.get(0);
			assertEquals(10, attribute.value());
			assertEquals(DURABILITY_IDENTIFIER, attribute.type());
		}

		@Test
		void buildCompositeAttributeComponent() {
			ComputationData computation = new ComputationData(1.2f, AttributeCodecs.MULTIPLICATION_OPERATOR, AttributeCodecs.MIDDLE_ORDER);
			AttributeData data = new AttributeDataImpl(
					id("iron-pickaxe_head-durability"),
					DURABILITY_IDENTIFIER,
					computation,
					null,
					PICKAXE_HEAD_ID
			);

			List<Property> properties = codec.build(List.of(data));

			assertEquals(1, properties.size());
			assertTrue(properties.get(0) instanceof CompositeAttributeComponent);
			CompositeAttributeComponent attribute = (CompositeAttributeComponent) properties.get(0);
			assertEquals(1.2f, attribute.value());
			assertEquals(DURABILITY_IDENTIFIER, attribute.type());
			assertEquals(PICKAXE_HEAD_ID, attribute.compositeKey());
		}
	}

	@Nested
	@DisplayName("Mapping from Property to AttributeData")
	class MappingToData {
		@Test
		void fromSimpleAttribute() {
			SimpleAttribute attribute = new SimpleAttribute(ATTACK_DAMAGE_IDENTIFIER, 5f, Condition.ALWAYS_TRUE);

			AttributeData data = codec.toData(attribute);

			assertNotNull(data);
			assertEquals(ATTACK_DAMAGE_IDENTIFIER, data.id());
			assertEquals(ATTACK_DAMAGE_IDENTIFIER, data.type());
			assertEquals(5f, data.computation().value());
			assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.computation().operator());
			assertNull(data.condition(), "ALWAYS_TRUE condition should serialize to null/absent");
			assertNull(data.composite());
		}

		@Test
		void fromCompositeAttributeComponent() {
			CompositeAttributeComponent attribute = new CompositeAttributeComponent(DURABILITY_IDENTIFIER, 0.1f, PICKAXE_HEAD_ID);

			AttributeData data = codec.toData(attribute);

			assertNotNull(data);
			assertEquals(DURABILITY_IDENTIFIER, data.id());
			assertEquals(DURABILITY_IDENTIFIER, data.type());
			assertEquals(0.1f, data.computation().value());
			assertEquals(PICKAXE_HEAD_ID, data.composite());
		}

		@Test
		void returnsNullForWrongPropertyType() {
			Property notAnAttribute = new Property() {
			};
			AttributeData data = codec.toData(notAnAttribute);
			assertNull(data);
		}
	}


	@Test
	@DisplayName("SimpleAttribute can be round-tripped through Codec and AttributeData")
	void testAttributeRoundTripSerialization() {
		// 1. Arrange: Create an original SimpleAttribute with a condition
		Condition originalCondition = new Condition(List.of(StaticConditions.selfHasTag("test_tag")), Collections.emptyList(),
				new ConditionData(List.of(new TagMatchPredicateData(id("forgero:self_has_tag"), id("forgero:test_tag"))))
		);
		SimpleAttribute originalAttribute = new SimpleAttribute(ATTACK_DAMAGE_IDENTIFIER, 15.5f, originalCondition);

		// 2. Act: Forward mapping (Property -> Data)
		AttributeData dataFromProperty = codec.toData(originalAttribute);

		// 3. Act: Serialize Data to JSON
		Codec<List<AttributeData>> listCodec = codec.getCodec();

		DataResult<JsonElement> encodeResult = listCodec.encodeStart(JsonOps.INSTANCE, List.of(dataFromProperty));

		if (encodeResult.error().isPresent()) {
			String errorMessage = encodeResult.error().orElseThrow().message();
			System.err.println("Actual Serialization Error: " + errorMessage);
			fail("Serialization failed: " + errorMessage);
		}
		JsonElement serializedElement = encodeResult.getOrThrow(false, msg -> new RuntimeException("Should not happen if isError() is handled: " + msg));

		DataResult<List<AttributeData>> parse = listCodec.parse(JsonOps.INSTANCE, serializedElement);

		if(parse.error().isPresent()) {
			String errorMessage = encodeResult.error().orElseThrow().message();
			System.err.println("Actual Serialization Error: " + errorMessage);
			fail("Serialization failed: " + errorMessage);
		}
		var deserializedData = parse.getOrThrow(false, msg -> new RuntimeException("Should not happen if isError() is handled: " + msg));

		// 5. Act: Backward mapping (Data -> Property)
		List<Property> finalProperties = codec.build(deserializedData);
		Property finalProperty = finalProperties.get(0);

		// 6. Assert
		assertEquals(1, finalProperties.size());
		assertInstanceOf(SimpleAttribute.class, finalProperty);
		SimpleAttribute finalAttribute = (SimpleAttribute) finalProperty;

		assertEquals(originalAttribute.type(), finalAttribute.type(), "Types should match after round trip");
		assertEquals(originalAttribute.value(), finalAttribute.value(), "Values should match after round trip");
		assertEquals(originalAttribute.operator().getClass(), finalAttribute.operator().getClass(), "Operators should match after round trip");

		assertNotNull(originalAttribute.condition().orElse(null), "Original attribute should have a condition");
		assertNotNull(finalAttribute.condition().orElse(null), "Final attribute should have a condition");
		// Check that the source data of the condition matches
		assertEquals(originalAttribute.condition().get().sourceData(), finalAttribute.condition().get().sourceData(), "Condition source data should match after round trip");
	}
}
