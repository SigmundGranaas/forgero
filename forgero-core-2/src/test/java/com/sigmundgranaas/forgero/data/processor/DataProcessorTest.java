package com.sigmundgranaas.forgero.data.processor;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.data.definition.NormalizedState;
import com.sigmundgranaas.forgero.core.data.definition.RawDefinition;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.v3.dto.MaterialData;
import com.sigmundgranaas.forgero.data.v3.dto.ShapeData;
import com.sigmundgranaas.forgero.data.v3.dto.StaticPartData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.EquipmentTemplateSlotData;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateData;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateStructureData;
import com.sigmundgranaas.forgero.data.v3.dto.template.PartTemplateStructureSlotData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.v3.dto.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.FeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.VeinMiningFeatureData;
import com.sigmundgranaas.forgero.data.v3.dto.feature.VeinMiningSelectorData;
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

	private RawDefinition createRawMaterial(String idPath, String name, @Nullable List<String> include, @Nullable List<String> tags, @Nullable List<AttributeData> attributes, @Nullable List<FeatureData> features) {
		MaterialData dto = new MaterialData(idFactory.of("forgero:material"), name,
				include == null ? null : include.stream().map(idFactory::of).toList(),
				tags == null ? null : tags.stream().map(idFactory::of).toList(),
				attributes, features);
		return new RawDefinition(idFactory.of(idPath), dto);
	}

	private RawDefinition createRawShape(String idPath, String name, @Nullable List<String> include, @Nullable List<String> tags, @Nullable List<AttributeData> attributes, @Nullable List<FeatureData> features) {
		ShapeData dto = new ShapeData(idFactory.of("forgero:shape"), name,
				include == null ? null : include.stream().map(idFactory::of).toList(),
				tags == null ? null : tags.stream().map(idFactory::of).toList(),
				attributes, features);
		return new RawDefinition(idFactory.of(idPath), dto);
	}

	private RawDefinition createRawPartTemplate(String idPath, String name, @Nullable List<String> include, @Nullable List<String> tags, String idPattern, Map<String, PartTemplateStructureSlotData> slots) {
		PartTemplateData dto = new PartTemplateData(
				idFactory.of("forgero:part_template"), name,
				include == null ? null : include.stream().map(idFactory::of).toList(),
				tags == null ? null : tags.stream().map(idFactory::of).toList(),
				new PartTemplateStructureData(
						idPattern,
						slots
				),
				null, null, null
		);
		return new RawDefinition(idFactory.of(idPath), dto);
	}

	private RawDefinition createRawEquipmentTemplate(String idPath, String name, @Nullable List<String> include, @Nullable List<String> tags, String idPattern, Map<String, EquipmentTemplateSlotData> slots) {
		EquipmentTemplateData dto = new EquipmentTemplateData(
				idFactory.of("forgero:equipment_template"), name,
				include == null ? null : include.stream().map(idFactory::of).toList(),
				tags == null ? null : tags.stream().map(idFactory::of).toList(),
				new EquipmentTemplateStructureData(idPattern, slots),
				null, null, null
		);
		return new RawDefinition(idFactory.of(idPath), dto);
	}

	private RawDefinition createRawStaticPart(String idPath, String name, @Nullable List<String> include, @Nullable List<String> tags, @Nullable List<AttributeData> attributes, @Nullable List<FeatureData> features) {
		StaticPartData dto = new StaticPartData(idFactory.of("forgero:static_part"), name,
				include == null ? null : include.stream().map(idFactory::of).toList(),
				tags == null ? null : tags.stream().map(idFactory::of).toList(),
				attributes, null, features);
		return new RawDefinition(idFactory.of(idPath), dto);
	}

	@BeforeEach
	void setUp() {
		processor = new DataProcessorImpl();
	}

	@Test
	void testNormalize_NoIncludes() {
		AttributeData attr = new AttributeDataImpl(idFactory.of("attr1"), idFactory.of("damage"), new ComputationData(10f, "forgero:addition", "forgero:base"), null, null);
		FeatureData feature = new VeinMiningFeatureData(idFactory.of("forgero:vein_mining"), "F1", "Desc1", new VeinMiningSelectorData(idFactory.of("forgero:radius"), 1, idFactory.of("tag")), null);

		RawDefinition raw = createRawMaterial(
				"test:data1", "Data 1",
				null, List.of("tag1"),
				List.of(attr),
				List.of(feature)
		);
		Map<OpenIdentifier, RawDefinition> rawData = Map.of(raw.id(), raw);

		NormalizedState state = processor.normalize(rawData);

		assertEquals(1, state.materials().size());
		assertTrue(state.materials().containsKey(raw.id()));

		var result = state.materials().get(raw.id());
		assertNotNull(result);
		assertEquals("Data 1", result.name());
		assertEquals(1, result.tags().size());
		assertTrue(result.tags().contains(idFactory.of("tag1")));
		assertEquals(1, result.attributes().size());
		assertEquals(1, result.features().size());
	}

	@Test
	void testNormalize_SingleInclude_NoConflict() {
		AttributeData includedAttr = new AttributeDataImpl(idFactory.of("inc_attr1"), idFactory.of("durability"), new ComputationData(50f, "forgero:addition", "forgero:base"), null, null);
		FeatureData includedFeature = new VeinMiningFeatureData(idFactory.of("forgero:vein_mining_inc"), "IncF1", "IncDesc1", new VeinMiningSelectorData(idFactory.of("forgero:radius"), 1, idFactory.of("inc_tag_selector")), null);
		RawDefinition includedData = createRawMaterial(
				"test:included", "Included",
				null, List.of("inc_tag"), List.of(includedAttr), List.of(includedFeature)
		);

		AttributeData baseAttr = new AttributeDataImpl(idFactory.of("base_attr1"), idFactory.of("speed"), new ComputationData(5f, "forgero:addition", "forgero:base"), null, null);
		RawDefinition baseData = createRawMaterial(
				"test:base", "Base",
				List.of("test:included"), List.of("base_tag"), List.of(baseAttr), null
		);

		Map<OpenIdentifier, RawDefinition> rawData = Map.of(
				includedData.id(), includedData,
				baseData.id(), baseData
		);

		NormalizedState state = processor.normalize(rawData);

		assertTrue(state.materials().containsKey(baseData.id()));
		var result = state.materials().get(baseData.id());

		assertEquals(2, result.tags().size());
		assertTrue(result.tags().contains(idFactory.of("inc_tag")));
		assertTrue(result.tags().contains(idFactory.of("base_tag")));

		assertEquals(2, result.attributes().size());
		assertTrue(result.attributes().stream().anyMatch(a -> a.id().equals(idFactory.of("inc_attr1"))));
		assertTrue(result.attributes().stream().anyMatch(a -> a.id().equals(idFactory.of("base_attr1"))));

		assertEquals(1, result.features().size());
		assertTrue(result.features().stream().anyMatch(f -> f.type().equals(idFactory.of("forgero:vein_mining_inc"))));
	}

	@Test
	void testNormalize_SingleInclude_AttributeConflict() {
		AttributeData includedAttr = new AttributeDataImpl(idFactory.of("shared_attr"), idFactory.of("damage"), new ComputationData(100f, "forgero:addition", "forgero:base"), null, null);
		RawDefinition includedData = createRawMaterial("test:included", "Included", null, null, List.of(includedAttr), null);

		AttributeData baseAttr = new AttributeDataImpl(idFactory.of("shared_attr"), idFactory.of("damage"), new ComputationData(200f, "forgero:addition", "forgero:base"), null, null);
		RawDefinition baseData = createRawMaterial("test:base", "Base", List.of("test:included"), null, List.of(baseAttr), null);

		Map<OpenIdentifier, RawDefinition> rawData = Map.of(
				includedData.id(), includedData,
				baseData.id(), baseData
		);

		NormalizedState state = processor.normalize(rawData);
		var result = state.materials().get(baseData.id());

		assertEquals(1, result.attributes().size());
		AttributeData finalAttr = result.attributes().get(0);
		assertEquals(idFactory.of("shared_attr"), finalAttr.id());
		assertEquals(200f, finalAttr.computation().value(), "Base attribute should override included one.");
	}

	@Test
	void testNormalize_CyclicInclude_Direct() {
		RawDefinition A = createRawMaterial("test:a", "A", List.of("test:b"), null, null, null);
		RawDefinition B = createRawMaterial("test:b", "B", List.of("test:a"), null, null, null);

		Map<OpenIdentifier, RawDefinition> rawData = Map.of(A.id(), A, B.id(), B);

		IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> processor.normalize(rawData));
		assertTrue(thrown.getMessage().contains("Cyclic include dependency detected involving: test:a") ||
				thrown.getMessage().contains("Cyclic include dependency detected involving: test:b"));
	}

	@Test
	void testNormalize_EquipmentTemplateWithIdPatternAndSlots() {
		RawDefinition pickaxeTemplateRaw = createRawEquipmentTemplate(
				"test:pickaxe_template", "Pickaxe",
				null, List.of("tool", "pickaxe"),
				"forgero:{head.material.name}-pickaxe",
				Map.of(
						"head", new EquipmentTemplateSlotData(idFactory.of("forgero:pickaxe_head_type"), new OpenIdentifier("forgero", "default-pickaxe_head"), null),
						"handle", new EquipmentTemplateSlotData(idFactory.of("forgero:handle_type"), null, new OpenIdentifier("forgero", "static_oak_handle"))
				)
		);

		Map<OpenIdentifier, RawDefinition> rawData = Map.of(pickaxeTemplateRaw.id(), pickaxeTemplateRaw);
		NormalizedState state = processor.normalize(rawData);

		assertEquals(1, state.equipmentTemplates().size());
		var normalizedTemplate = state.equipmentTemplates().get(pickaxeTemplateRaw.id());
		assertNotNull(normalizedTemplate);
		assertEquals("Pickaxe", normalizedTemplate.name());
		assertTrue(normalizedTemplate.tags().contains(idFactory.of("tool")));
		assertTrue(normalizedTemplate.tags().contains(idFactory.of("pickaxe")));
		assertEquals("forgero:{head.material.name}-pickaxe", normalizedTemplate.structure().id());
		assertNotNull(normalizedTemplate.structure().slots());
		assertTrue(normalizedTemplate.structure().slots().containsKey("head"));
		assertTrue(normalizedTemplate.structure().slots().containsKey("handle"));
	}

	@Test
	void testNormalize_StaticPartWithUpgrades() {
		RawDefinition rawStaticPart = createRawStaticPart(
				"test:static_handle", "Static Handle",
				null, List.of("handle_type"),
				List.of(new AttributeDataImpl(idFactory.of("handle-durability"), idFactory.of("durability"), new ComputationData(100f, "forgero:addition", "forgero:base"), null, null)),
				null
		);
		StaticPartData originalDto = (StaticPartData) rawStaticPart.data();
		originalDto = new StaticPartData(originalDto.type(), originalDto.name(), originalDto.include(), originalDto.tags(), originalDto.attributes(),
				List.of(new UpgradeSlotData(idFactory.of("slot1"), idFactory.of("slot_type"), null, null, null)), originalDto.features());

		rawStaticPart = new RawDefinition(rawStaticPart.id(), originalDto);

		Map<OpenIdentifier, RawDefinition> rawData = Map.of(rawStaticPart.id(), rawStaticPart);
		NormalizedState state = processor.normalize(rawData);

		assertEquals(1, state.staticParts().size());
		var normalizedStaticPart = state.staticParts().get(rawStaticPart.id());
		assertNotNull(normalizedStaticPart);
		assertEquals("Static Handle", normalizedStaticPart.name());
		assertNotNull(normalizedStaticPart.upgrades());
		assertEquals(1, normalizedStaticPart.upgrades().size());
	}
}
