package com.sigmundgranaas.forgero.data.processor;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.data.definition.NormalizedState;
import com.sigmundgranaas.forgero.core.data.definition.RawDefinition;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.v3.codec.CodecConstants;
import com.sigmundgranaas.forgero.data.v3.dto.MaterialData;
import com.sigmundgranaas.forgero.data.v3.dto.ShapeData;
import com.sigmundgranaas.forgero.data.v3.dto.StaticPartData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.VeinMiningFeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.VeinMiningSelectorData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateSlotData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateStructureSlotData;
import com.sigmundgranaas.forgero.data.v3.dto.template.UpgradeSlotData;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DataProcessorTest extends ForgeroTest {

	private DataProcessor processor;

	@BeforeEach
	void setUp() {
		processor = new DataProcessorImpl();
	}

	// region Test Data Factories
	private RawDefinition createRawMaterial(String idPath, String name, @Nullable List<String> include, @Nullable List<String> tags, @Nullable List<AttributeData> attributes, @Nullable List<FeatureData> features) {
		MaterialData dto = new MaterialData(id("forgero:material"), name,
				include == null ? null : include.stream().map(this::id).toList(),
				tags == null ? null : tags.stream().map(this::id).toList(),
				attributes, features);
		return new RawDefinition(id(idPath), dto);
	}

	private RawDefinition createEquipmentTemplate(String idPath, String name, @Nullable List<String> include, @Nullable List<String> tags, String idPattern, Map<String, EquipmentTemplateSlotData> slots) {
		EquipmentTemplateData dto = new EquipmentTemplateData(
				id("forgero:equipment_template"), name,
				include == null ? null : include.stream().map(this::id).toList(),
				tags == null ? null : tags.stream().map(this::id).toList(),
				new EquipmentTemplateStructureData(idPattern, slots),
				null, null, null
		);
		return new RawDefinition(id(idPath), dto);
	}

	private RawDefinition createRawStaticPart(String idPath, String name, @Nullable List<String> include, @Nullable List<String> tags, @Nullable List<AttributeData> attributes, @Nullable List<FeatureData> features, @Nullable List<UpgradeSlotData> upgrades) {
		StaticPartData dto = new StaticPartData(id("forgero:static_part"), name,
				include == null ? null : include.stream().map(this::id).toList(),
				tags == null ? null : tags.stream().map(this::id).toList(),
				attributes, upgrades, features);
		return new RawDefinition(id(idPath), dto);
	}

	private AttributeData createAttribute(String id, String type, float value) {
		return new AttributeDataImpl(id(id), id(type), new ComputationData(value, "forgero:addition", "forgero:base"), null, null);
	}

	private FeatureData createVeinMiningFeature(String type, int radius, String selectorTag) {
		return new VeinMiningFeatureData(id(type), "Test Feature", "Desc", new VeinMiningSelectorData(id("forgero:radius"), radius, id(selectorTag)), null);
	}
	// endregion

	@Test
	void testNormalize_NoIncludes() {
		AttributeData attr = createAttribute("attr1", "damage", 10f);
		FeatureData feature = createVeinMiningFeature("forgero:vein_mining", 1, "forgero:tag");

		RawDefinition raw = createRawMaterial("test:data1", "Data 1", null, List.of("tag1"), List.of(attr), List.of(feature));
		Map<OpenIdentifier, RawDefinition> rawData = Map.of(raw.id(), raw);

		NormalizedState state = processor.normalize(rawData);

		assertEquals(1, state.materials().size());
		assertTrue(state.materials().containsKey(raw.id()));

		var result = state.materials().get(raw.id());
		assertNotNull(result);
		assertEquals("Data 1", result.name());
		assertTrue(result.tags().contains(id("tag1")));
		assertEquals(1, result.attributes().size());
		assertEquals(1, result.features().size());
	}

	@Test
	void testNormalize_SingleInclude_NoConflict() {
		AttributeData includedAttr = createAttribute("inc_attr1", "durability", 50f);
		FeatureData includedFeature = createVeinMiningFeature("forgero:vein_mining_inc", 1, "inc_tag_selector");
		RawDefinition includedData = createRawMaterial("test:included", "Included", null, List.of("inc_tag"), List.of(includedAttr), List.of(includedFeature));

		AttributeData baseAttr = createAttribute("base_attr1", "speed", 5f);
		RawDefinition baseData = createRawMaterial("test:base", "Base", List.of("test:included"), List.of("base_tag"), List.of(baseAttr), null);

		Map<OpenIdentifier, RawDefinition> rawData = Map.of(includedData.id(), includedData, baseData.id(), baseData);
		NormalizedState state = processor.normalize(rawData);

		assertTrue(state.materials().containsKey(baseData.id()));
		var result = state.materials().get(baseData.id());

		assertEquals(2, result.tags().size());
		assertTrue(result.tags().containsAll(List.of(id("inc_tag"), id("base_tag"))));

		assertEquals(2, result.attributes().size());
		var attributeIds = result.attributes().stream().map(AttributeData::id).collect(Collectors.toSet());
		assertTrue(attributeIds.containsAll(List.of(id("inc_attr1"), id("base_attr1"))));

		assertEquals(1, result.features().size());
		assertEquals(id("forgero:vein_mining_inc"), result.features().get(0).type());
	}

	@Test
	void testNormalize_SingleInclude_AttributeConflict() {
		AttributeData includedAttr = createAttribute("shared_attr", "damage", 100f);
		RawDefinition includedData = createRawMaterial("test:included", "Included", null, null, List.of(includedAttr), null);

		AttributeData baseAttr = createAttribute("shared_attr", "damage", 200f);
		RawDefinition baseData = createRawMaterial("test:base", "Base", List.of("test:included"), null, List.of(baseAttr), null);

		Map<OpenIdentifier, RawDefinition> rawData = Map.of(includedData.id(), includedData, baseData.id(), baseData);
		NormalizedState state = processor.normalize(rawData);
		var result = state.materials().get(baseData.id());

		assertEquals(1, result.attributes().size());
		AttributeData finalAttr = result.attributes().get(0);
		assertEquals(id("shared_attr"), finalAttr.id());
		assertEquals(200f, finalAttr.computation().value(), "Base attribute should override included one.");
	}

	@Test
	void testNormalize_CyclicInclude_Direct() {
		RawDefinition A = createRawMaterial("test:a", "A", List.of("test:b"), null, null, null);
		RawDefinition B = createRawMaterial("test:b", "B", List.of("test:a"), null, null, null);

		Map<OpenIdentifier, RawDefinition> rawData = Map.of(A.id(), A, B.id(), B);

		IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> processor.normalize(rawData));
		assertTrue(thrown.getMessage().contains("Cyclic include dependency detected"), "Exception message should report cyclic dependency.");
	}

	@Test
	void testNormalize_EquipmentTemplateWithIdPatternAndSlots() {
		RawDefinition pickaxeTemplateRaw = createEquipmentTemplate(
				"test:pickaxe_template", "Pickaxe",
				null, List.of("tool", "pickaxe"),
				"forgero:{head.material.name}-pickaxe",
				Map.of(
						"head", new EquipmentTemplateSlotData(id("forgero:pickaxe_head_type"), id("forgero:default-pickaxe_head"), null),
						"handle", new EquipmentTemplateSlotData(id("forgero:handle_type"), null, id("forgero:static_oak_handle"))
				)
		);

		Map<OpenIdentifier, RawDefinition> rawData = Map.of(pickaxeTemplateRaw.id(), pickaxeTemplateRaw);
		NormalizedState state = processor.normalize(rawData);

		assertEquals(1, state.equipmentTemplates().size());
		var normalizedTemplate = state.equipmentTemplates().get(pickaxeTemplateRaw.id());
		assertNotNull(normalizedTemplate);
		assertEquals("Pickaxe", normalizedTemplate.name());
		assertTrue(normalizedTemplate.tags().containsAll(List.of(id("tool"), id("pickaxe"))));
		assertEquals("forgero:{head.material.name}-pickaxe", normalizedTemplate.structure().id());
		assertNotNull(normalizedTemplate.structure().slots());
		assertTrue(normalizedTemplate.structure().slots().containsKey("head"));
		assertTrue(normalizedTemplate.structure().slots().containsKey("handle"));
	}

	@Test
	void testNormalize_StaticPartWithUpgrades() {
		List<UpgradeSlotData> upgrades = List.of(new UpgradeSlotData(id("slot1"), id("slot_type"), null, null, null));
		List<AttributeData> attributes = List.of(createAttribute("handle-durability", "durability", 100f));
		RawDefinition rawStaticPart = createRawStaticPart("test:static_handle", "Static Handle", null, List.of("handle_type"), attributes, null, upgrades);

		Map<OpenIdentifier, RawDefinition> rawData = Map.of(rawStaticPart.id(), rawStaticPart);
		NormalizedState state = processor.normalize(rawData);

		assertEquals(1, state.staticParts().size());
		var normalizedStaticPart = state.staticParts().get(rawStaticPart.id());
		assertNotNull(normalizedStaticPart);
		assertEquals("Static Handle", normalizedStaticPart.name());
		assertNotNull(normalizedStaticPart.upgrades());
		assertEquals(1, normalizedStaticPart.upgrades().size());
	}
	private OpenIdentifier id(String id) {
		return CodecConstants.IDENTIFIER_FACTORY.of(id);
	}
}
