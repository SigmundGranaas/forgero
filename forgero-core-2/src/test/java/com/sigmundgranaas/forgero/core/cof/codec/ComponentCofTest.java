package com.sigmundgranaas.forgero.core.cof.codec;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.cof.ComponentConstructorRegistry;
import com.sigmundgranaas.forgero.cof.codec.ComponentCofCodec;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.component.mutation.impl.ComponentMutaterImpl;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.api.PropertyRegistry;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class ComponentCofTest {

	private ComponentCofCodec codec;
	private ComponentRegistry componentRegistry;
	private ComponentConstructorRegistry constructorRegistry;

	// Component Definitions
	private static final Component IRON_COMPONENT = part(IRON_ID).withTag(METAL_TAG).withTag(MATERIAL_SLOT_TYPE).build();
	private static final Component GOLD_COMPONENT = part(GOLD_ID).withTag(METAL_TAG).withTag(MATERIAL_SLOT_TYPE).build();
	private static final Component OAK_COMPONENT = part(OAK_ID).withTag(WOOD_TAG).build();
	private static final Component PICKAXE_HEAD_COMPONENT = part(id("iron-pickaxe_head")).withTag(PICKAXE_HEAD_TAG).build();
	private static final Component HANDLE_COMPONENT = part(id("oak-handle")).withTag(HANDLE_TAG).build();
	private static final Component GEM_COMPONENT = part(GEM_ID).withTag(GEM_TAG).withAttribute(DefaultAttributes.ATTACK_DAMAGE, 10).build();
	private static final Component SWORD_BLADE_COMPONENT = part(id("sword-blade")).withTag(SWORD_BLADE_TAG)
			.withStructureSlot(structureSlot("material", MATERIAL_SLOT_TYPE, IRON_COMPONENT))
			.build();
	private static final Component HILT_COMPONENT = part(HILT_ID).withTag(HILT_TAG)
			.withUpgradeSlot(upgradeSlot("pommel_slot", POMMEL_TAG))
			.build();
	private static final Component FANCY_HILT_COMPONENT = part(id("fancy-hilt")).withTag(HILT_TAG)
			.withUpgradeSlot(upgradeSlot("pommel_slot", POMMEL_TAG))
			.build();
	private static final Component SWORD_COMPONENT = tool(SWORD_ID).withTag(SWORD_TAG)
			.withPart(SWORD_BLADE_COMPONENT, "blade", SWORD_BLADE_TAG)
			.withPart(HILT_COMPONENT, "handle", HILT_TAG)
			.build();
	private static final Component AMULET_COMPONENT = tool(AMULET_ID).withTag(AMULET_TAG)
			.withUpgradeSlot(upgradeSlot("gem_slot", GEM_TAG))
			.build();
	private static final Component SHIELD_COMPONENT = tool(SHIELD_ID).withTag(SHIELD_TAG).build();
	private static final Component AXE_HEAD_COMPONENT = part(AXE_HEAD_ID).withTag(AXE_HEAD_TAG)
			.withStructureSlot(structureSlot("material", MATERIAL_SLOT_TYPE, IRON_COMPONENT))
			.withUpgradeSlot(upgradeSlot("rune_slot", RUNE_TAG))
			.build();
	private static final Component PRISTINE_PICKAXE = tool(id("iron-pickaxe")).withTag(PICKAXE_TAG)
			.withPart(PICKAXE_HEAD_COMPONENT, "head", PICKAXE_HEAD_TAG)
			.withPart(HANDLE_COMPONENT, "handle", HANDLE_TAG)
			.withUpgradeSlot(upgradeSlot("gem_slot", GEM_TAG))
			.build();

	@BeforeEach
	void setUp() {
		PropertyRegistry.getInstance().reset();

		constructorRegistry = ComponentConstructorRegistry.getInstance();
		constructorRegistry.clear();
		constructorRegistry.registerCoreTypes();

		ComponentRegistry.Builder registryBuilder = ComponentRegistry.builder();
		registryBuilder.add(IRON_COMPONENT).add(OAK_COMPONENT).add(PICKAXE_HEAD_COMPONENT)
				.add(HANDLE_COMPONENT).add(GEM_COMPONENT).add(PRISTINE_PICKAXE)
				.add(SWORD_BLADE_COMPONENT).add(HILT_COMPONENT).add(SWORD_COMPONENT)
				.add(AMULET_COMPONENT).add(SHIELD_COMPONENT).add(AXE_HEAD_COMPONENT)
				.add(GOLD_COMPONENT).add(FANCY_HILT_COMPONENT);

		componentRegistry = registryBuilder.build();
		codec = new ComponentCofCodec(componentRegistry, constructorRegistry);
	}

	private void testPristineSerialization(Component component) {
		JsonElement result = codec.encodeStart(JsonOps.INSTANCE, component).getOrThrow(false, System.err::println);
		assertTrue(result.isJsonPrimitive(), "Pristine " + component.getClass().getSimpleName() + " should serialize to ID string");
		assertEquals(component.id().toString(), result.getAsString());
	}

	private void testMutatedCycle(Component pristine, Component mutated) {
		assertNotEquals(pristine, mutated, "Mutated component should not be equal to pristine for " + pristine.getClass().getSimpleName());

		JsonElement serialized = codec.encodeStart(JsonOps.INSTANCE, mutated).getOrThrow(false, System.err::println);
		assertTrue(serialized.isJsonObject(), "Mutated " + pristine.getClass().getSimpleName() + " should serialize to a full object");

		assertEquals(1, serialized.getAsJsonObject().get("cof_version").getAsInt(), "Serialized object should have cof_version = 1");

		Component deserialized = codec.parse(JsonOps.INSTANCE, serialized).getOrThrow(false, System.err::println);
		assertEquals(mutated, deserialized, "Deserialized component should be equal to the mutated one for " + pristine.getClass().getSimpleName());
	}

	@Test
	void testStaticComponentCycle() {
		testPristineSerialization(IRON_COMPONENT);
		Component mutated = part(IRON_ID).withTag(METAL_TAG).withAttribute(DefaultAttributes.DURABILITY, 100).build();
		testMutatedCycle(IRON_COMPONENT, mutated);
	}

	@Test
	void testStructuredPartCycle() {
		testPristineSerialization(SWORD_BLADE_COMPONENT);
		ComponentMutater mutater = new ComponentMutaterImpl();
		Component mutated = mutater.setSlot(SWORD_BLADE_COMPONENT, id("material"), GOLD_COMPONENT);
		testMutatedCycle(SWORD_BLADE_COMPONENT, mutated);
	}

	@Test
	void testExtensiblePartCycle() {
		testPristineSerialization(HILT_COMPONENT);
		ComponentMutater mutater = new ComponentMutaterImpl();
		Component mutated = mutater.setSlot(HILT_COMPONENT, id("pommel_slot"), IRON_COMPONENT);
		testMutatedCycle(HILT_COMPONENT, mutated);
	}

	@Test
	void testStructuredEquipmentCycle() {
		testPristineSerialization(SWORD_COMPONENT);
		ComponentMutater mutater = new ComponentMutaterImpl();
		Component mutated = mutater.setSlot(SWORD_COMPONENT, id("handle"), FANCY_HILT_COMPONENT);
		testMutatedCycle(SWORD_COMPONENT, mutated);
	}

	@Test
	void testExtensibleEquipmentCycle() {
		testPristineSerialization(AMULET_COMPONENT);
		ComponentMutater mutater = new ComponentMutaterImpl();
		Component mutated = mutater.setSlot(AMULET_COMPONENT, id("gem_slot"), GEM_COMPONENT);
		testMutatedCycle(AMULET_COMPONENT, mutated);
	}

	@Test
	void testStaticEquipmentCycle() {
		testPristineSerialization(SHIELD_COMPONENT);
		Component mutated = tool(SHIELD_ID).withTag(SHIELD_TAG).withAttribute(DefaultAttributes.ARMOR, 5).build();
		testMutatedCycle(SHIELD_COMPONENT, mutated);
	}

	@Test
	void testStructuredExtensiblePartCycle() {
		testPristineSerialization(AXE_HEAD_COMPONENT);
		ComponentMutater mutater = new ComponentMutaterImpl();
		Component mutated = mutater.setSlot(AXE_HEAD_COMPONENT, id("rune_slot"), GEM_COMPONENT);
		testMutatedCycle(AXE_HEAD_COMPONENT, mutated);
	}

	@Test
	void testStructuredExtensibleEquipmentCycle() {
		testPristineSerialization(PRISTINE_PICKAXE);
		ComponentMutater mutater = new ComponentMutaterImpl();
		Component mutated = mutater.setSlot(PRISTINE_PICKAXE, id("gem_slot"), GEM_COMPONENT);
		testMutatedCycle(PRISTINE_PICKAXE, mutated);
	}


	@Test
	void testCustomComponentRegistrationAndCycle() {
		record CustomComponent(OpenIdentifier id, Set<OpenIdentifier> tags, List<Property> properties) implements Component {
			@Override public List<Property> getProperties() { return properties; }
			@Override public Set<OpenIdentifier> getTags() { return tags; }
		}

		constructorRegistry.register("custom:custom_component", CustomComponent.class, (dto, props, struct, upgs) ->
				DataResult.success(new CustomComponent(dto.id(), dto.tags() != null ? dto.tags() : Collections.emptySet(), props))
		);

		OpenIdentifier customId = id("custom:my_item");
		Component pristineCustom = new CustomComponent(customId, Set.of(), List.of());

		ComponentRegistry customRegistry = componentRegistry.toBuilder().add(pristineCustom).build();
		Component mutatedCustom = new CustomComponent(customId, Set.of(id("custom:test_tag")), List.of());
		ComponentCofCodec customCodec = new ComponentCofCodec(customRegistry, constructorRegistry);

		JsonElement serialized = customCodec.encodeStart(JsonOps.INSTANCE, mutatedCustom).getOrThrow(false, System.err::println);
		assertTrue(serialized.isJsonObject());
		assertEquals("custom:custom_component", serialized.getAsJsonObject().get("component_type").getAsString());

		Component deserialized = customCodec.parse(JsonOps.INSTANCE, serialized).getOrThrow(false, System.err::println);
		assertEquals(mutatedCustom, deserialized);
		assertInstanceOf(CustomComponent.class, deserialized);
	}

	@Test
	void testSerializationCache() throws NoSuchFieldException, IllegalAccessException {
		Component mutated = new StaticComponent(SHIELD_ID, Set.of(id("forgero:shield")), List.of(attribute(DefaultAttributes.ARMOR).withValue(5).build()));

		Field cacheField = ComponentCofCodec.class.getDeclaredField("serializationCache");
		cacheField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Cache<Component, JsonElement> cache = (Cache<Component, JsonElement>) cacheField.get(codec);
		cache.invalidateAll();

		JsonElement firstResult = codec.encodeStart(JsonOps.INSTANCE, mutated).getOrThrow(false, System.err::println);

		assertNotNull(cache.getIfPresent(mutated));
		assertEquals(firstResult, cache.getIfPresent(mutated));

		JsonObject poisonPill = new JsonObject();
		poisonPill.addProperty("cached", true);
		cache.put(mutated, poisonPill);

		DataResult<JsonElement> secondResult = codec.encodeStart(JsonOps.INSTANCE, mutated);

		assertTrue(secondResult.result().isPresent());
		assertEquals(poisonPill, secondResult.result().get());
		assertNotEquals(firstResult, secondResult.result().get());
	}
}
