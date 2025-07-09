package com.sigmundgranaas.forgero.property;

import com.mojang.serialization.Codec;
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
import com.sigmundgranaas.forgero.data.mapper.impl.AttributeCodec;
import com.sigmundgranaas.forgero.testutils.TestIdentifiers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.PICKAXE_HEAD_IDENTIFIER;
import static org.junit.jupiter.api.Assertions.*;

class AttributeCodecTest {

	private AttributeCodec codec;
	private ConditionMapper conditionMapper;
	private OperatorMapper operatorMapper;

	@BeforeEach
	void setUp() {
		conditionMapper = new ConditionMapper(TestIdentifiers.identifierFactory());
		operatorMapper = new OperatorMapper();
		codec = new AttributeCodec(conditionMapper, operatorMapper);
	}

	@Test
	void buildSimpleAttribute() {
		ComputationData computation = new ComputationData(10, AttributeCodecs.ADDITION_OPERATOR, AttributeCodecs.BASE_ORDER);
		AttributeData data = new AttributeDataImpl(TestIdentifiers.DURABILITY_IDENTIFIER, TestIdentifiers.DURABILITY_IDENTIFIER, computation, null, null);

		List<Property> properties = codec.build(List.of(data));

		assertEquals(1, properties.size());
		assertTrue(properties.get(0) instanceof SimpleAttribute);
		SimpleAttribute attribute = (SimpleAttribute) properties.get(0);
		assertEquals(10, attribute.value());
		assertEquals(TestIdentifiers.DURABILITY_IDENTIFIER, attribute.type());
	}

	@Test
	void buildCompositeAttributeComponent() {
		ComputationData computation = new ComputationData(1.2f, AttributeCodecs.MULTIPLICATION_OPERATOR, AttributeCodecs.MIDDLE_ORDER);
		AttributeData data = new AttributeDataImpl(
				TestIdentifiers.IRON_PICKAXE_HEAD_DURABILITY,
				TestIdentifiers.DURABILITY_IDENTIFIER,
				computation,
				null,
				PICKAXE_HEAD_IDENTIFIER
		);

		List<Property> properties = codec.build(List.of(data));

		assertEquals(1, properties.size());
		assertTrue(properties.get(0) instanceof CompositeAttributeComponent);
		CompositeAttributeComponent attribute = (CompositeAttributeComponent) properties.get(0);
		assertEquals(1.2f, attribute.value());
		assertEquals(TestIdentifiers.DURABILITY_IDENTIFIER, attribute.type());
		assertEquals(PICKAXE_HEAD_IDENTIFIER, attribute.compositeKey());
	}

	@Test
	void toDataFromSimpleAttribute() {
		SimpleAttribute attribute = new SimpleAttribute(TestIdentifiers.ATTACK_DAMAGE_IDENTIFIER, 5f, Condition.ALWAYS_TRUE);

		AttributeData data = codec.toData(attribute);

		assertNotNull(data);
		assertEquals(TestIdentifiers.ATTACK_DAMAGE_IDENTIFIER, data.id());
		assertEquals(TestIdentifiers.ATTACK_DAMAGE_IDENTIFIER, data.type());
		assertEquals(5f, data.computation().value());
		assertEquals(AttributeCodecs.ADDITION_OPERATOR, data.computation().operator());
		assertNull(data.condition()); // ALWAYS_TRUE condition serializes to null/absent
		assertNull(data.composite());
	}

	@Test
	void toDataFromCompositeAttributeComponent() {
		CompositeAttributeComponent attribute = new CompositeAttributeComponent(TestIdentifiers.DURABILITY_IDENTIFIER, 0.1f, PICKAXE_HEAD_IDENTIFIER);

		AttributeData data = codec.toData(attribute);

		assertNotNull(data);
		assertEquals(TestIdentifiers.DURABILITY_IDENTIFIER, data.id());
		assertEquals(TestIdentifiers.DURABILITY_IDENTIFIER, data.type());
		assertEquals(0.1f, data.computation().value());
		assertEquals(PICKAXE_HEAD_IDENTIFIER, data.composite());
	}

	@Test
	void toDataReturnsNullForWrongPropertyType() {
		Property notAnAttribute = new Property() {};
		AttributeData data = codec.toData(notAnAttribute);
		assertNull(data);
	}

	@Test
	@DisplayName("SimpleAttribute can be round-tripped through Codec and AttributeData")
	void testAttributeRoundTripSerialization() {
		// 1. Programmatically create an original SimpleAttribute with a condition
		Condition originalCondition = new Condition(List.of(StaticConditions.selfHasTag("test_tag")), Collections.emptyList(),
				new ConditionData(List.of(new TagMatchPredicateData(TestIdentifiers.identifierFactory().of("forgero:self_has_tag"), TestIdentifiers.identifierFactory().of("forgero:test_tag"))))
		);
		SimpleAttribute originalAttribute = new SimpleAttribute(
				TestIdentifiers.ATTACK_DAMAGE_IDENTIFIER,
				15.5f,
				originalCondition
		);

		// 2. Convert Property to PropertyData (forward mapping)
		AttributeData originalData = codec.toData(originalAttribute);
		assertNotNull(originalData, "Attribute should convert to AttributeData");
		assertEquals(originalAttribute.type(), originalData.type());
		assertEquals(originalAttribute.value(), originalData.computation().value());
		assertEquals(operatorMapper.toString(originalAttribute.operator()), originalData.computation().operator());
		assertNotNull(originalData.condition(), "Condition data should be present");

		// 3. Serialize PropertyData to JsonElement (as a list)
		Codec<List<AttributeData>> attributeListCodec = codec.getCodec();
		var serializedElement = attributeListCodec.encodeStart(JsonOps.INSTANCE, List.of(originalData))
				.resultOrPartial(error -> { throw new RuntimeException("Serialization error: " + error); })
				.orElseThrow();

		assertNotNull(serializedElement, "Serialized JSON should not be null");
		assertTrue(serializedElement.isJsonArray(), "Serialized element should be a JSON array");
		assertEquals(1, serializedElement.getAsJsonArray().size());
		var jsonObject = serializedElement.getAsJsonArray().get(0).getAsJsonObject();

		assertEquals(originalAttribute.type().toString(), jsonObject.get("type").getAsString());
		assertEquals(originalAttribute.value(), jsonObject.get("computation").getAsJsonObject().get("value").getAsFloat());
		assertTrue(jsonObject.has("condition"), "Condition should be serialized");


		// 4. Deserialize JsonElement back to PropertyData (list of data)
		List<AttributeData> deserializedDataList = attributeListCodec.parse(JsonOps.INSTANCE, serializedElement)
				.resultOrPartial(error -> { throw new RuntimeException("Deserialization error: " + error); })
				.orElseThrow();
		assertEquals(1, deserializedDataList.size(), "Deserialized list should contain one item");
		AttributeData deserializedData = deserializedDataList.get(0);

		assertNotNull(deserializedData, "Deserialized data should not be null");
		assertEquals(originalData.type(), deserializedData.type());
		assertEquals(originalData.computation().value(), deserializedData.computation().value());
		assertEquals(originalData.computation().operator(), deserializedData.computation().operator());
		assertNotNull(deserializedData.condition(), "Deserialized condition data should be present");
		assertEquals(originalData.condition().predicates().get(0).type(), deserializedData.condition().predicates().get(0).type());


		// 5. Convert PropertyData back to Property (backward mapping)
		List<Property> roundTrippedProperties = codec.build(deserializedDataList);
		assertEquals(1, roundTrippedProperties.size(), "Round-tripped properties list should contain one item");
		Property roundTrippedProperty = roundTrippedProperties.get(0);

		assertTrue(roundTrippedProperty instanceof SimpleAttribute, "Round-tripped property should be SimpleAttribute");
		SimpleAttribute roundTrippedAttribute = (SimpleAttribute) roundTrippedProperty;

		// 6. Assert that original and round-tripped attributes are equivalent
		assertEquals(originalAttribute.type(), roundTrippedAttribute.type(), "Types should match after round trip");
		assertEquals(originalAttribute.value(), roundTrippedAttribute.value(), "Values should match after round trip");
		assertEquals(originalAttribute.operator().getClass(), roundTrippedAttribute.operator().getClass(), "Operators should match after round trip");
		// Compare conditions via their source data
		assertNotNull(originalAttribute.condition(), "Original attribute should have a condition");
		assertNotNull(roundTrippedAttribute.condition(), "Round-tripped attribute should have a condition");
		assertEquals(originalAttribute.condition().get().sourceData(), roundTrippedAttribute.condition().get().sourceData(), "Condition source data should match after round trip");
	}
}
