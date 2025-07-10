// FILE: /home/sigmund/Documents/projects/forgero/1-20/forgero-core-2/src/test/java/com/sigmundgranaas/forgero/core/cof/ComponentCofTest.java
package com.sigmundgranaas.forgero.core.cof;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.cof.ComponentConstructorRegistry;
import com.sigmundgranaas.forgero.cof.codec.ComponentCofCodec;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.impl.*;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ComponentCofTest {

	private ComponentCofCodec codec;
	private ComponentRegistry componentRegistry;
	private ComponentConstructorRegistry constructorRegistry;

	// Component Definitions
	private static final OpenIdentifier IRON_ID = new OpenIdentifier("forgero", "iron");
	private static final Component IRON_COMPONENT = new StaticComponent(IRON_ID, Set.of(new OpenIdentifier("forgero:metal")), Collections.emptyList());

	private static final OpenIdentifier GOLD_ID = new OpenIdentifier("forgero", "gold");
	private static final Component GOLD_COMPONENT = new StaticComponent(GOLD_ID, Set.of(new OpenIdentifier("forgero:metal")), List.of());

	private static final OpenIdentifier OAK_ID = new OpenIdentifier("forgero", "oak");
	private static final Component OAK_COMPONENT = new StaticComponent(OAK_ID, Set.of(new OpenIdentifier("forgero:wood")), Collections.emptyList());

	private static final OpenIdentifier PICKAXE_HEAD_ID = new OpenIdentifier("forgero", "iron-pickaxe_head");
	private static final Component PICKAXE_HEAD_COMPONENT = new StaticComponent(PICKAXE_HEAD_ID, Set.of(new OpenIdentifier("forgero:pickaxe_head")), List.of());

	private static final OpenIdentifier HANDLE_ID = new OpenIdentifier("forgero", "oak-handle");
	private static final Component HANDLE_COMPONENT = new StaticComponent(HANDLE_ID, Set.of(new OpenIdentifier("forgero:handle")), List.of());

	private static final OpenIdentifier GEM_ID = new OpenIdentifier("forgero", "diamond-gem");
	private static final Component GEM_COMPONENT = new StaticComponent(GEM_ID, Set.of(new OpenIdentifier("forgero:gem")), List.of(new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 10)));

	private static final OpenIdentifier SWORD_BLADE_ID = new OpenIdentifier("forgero:sword-blade");
	private static final Component SWORD_BLADE_COMPONENT = new StructuredPart(SWORD_BLADE_ID, Set.of(new OpenIdentifier("forgero:sword_blade")), List.of(), new ComponentStructure(Map.of(new OpenIdentifier("forgero:material"), new StructureSlot(new OpenIdentifier("forgero:material"), new OpenIdentifier("forgero:metal"), "", IRON_COMPONENT))));

	private static final OpenIdentifier HILT_ID = new OpenIdentifier("forgero:hilt");
	private static final Component HILT_COMPONENT = new ExtensiblePart(HILT_ID, Set.of(new OpenIdentifier("forgero:hilt")), List.of(), new ComponentUpgrades(List.of(new UpgradeSlot(new OpenIdentifier("forgero:pommel_slot"), new OpenIdentifier("forgero:pommel"), "", (comp) -> true, Optional.empty()))));

	private static final OpenIdentifier FANCY_HILT_ID = new OpenIdentifier("forgero:fancy-hilt");
	private static final Component FANCY_HILT_COMPONENT = new ExtensiblePart(FANCY_HILT_ID, Set.of(new OpenIdentifier("forgero:hilt")), List.of(), new ComponentUpgrades(List.of(new UpgradeSlot(new OpenIdentifier("forgero:pommel_slot"), new OpenIdentifier("forgero:pommel"), "", (comp) -> true, Optional.empty()))));

	private static final OpenIdentifier SWORD_ID = new OpenIdentifier("forgero:sword");
	private static final Component SWORD_COMPONENT = new StructuredEquipment(SWORD_ID, Set.of(new OpenIdentifier("forgero:sword")), List.of(), new ComponentStructure(Map.of(new OpenIdentifier("forgero:blade"), new StructureSlot(new OpenIdentifier("forgero:blade"), new OpenIdentifier("forgero:sword_blade"), "", SWORD_BLADE_COMPONENT), new OpenIdentifier("forgero:handle"), new StructureSlot(new OpenIdentifier("forgero:handle"), new OpenIdentifier("forgero:hilt"), "", HILT_COMPONENT))));

	private static final OpenIdentifier AMULET_ID = new OpenIdentifier("forgero:amulet");
	private static final Component AMULET_COMPONENT = new ExtensibleEquipment(AMULET_ID, Set.of(new OpenIdentifier("forgero:amulet")), List.of(), new ComponentUpgrades(List.of(new UpgradeSlot(new OpenIdentifier("forgero:gem_slot"), new OpenIdentifier("forgero:gem"), "", (comp) -> true, Optional.empty()))));

	private static final OpenIdentifier SHIELD_ID = new OpenIdentifier("forgero:shield");
	private static final Component SHIELD_COMPONENT = new StaticEquipment(SHIELD_ID, Set.of(new OpenIdentifier("forgero:shield")), List.of());

	private static final OpenIdentifier AXE_HEAD_ID = new OpenIdentifier("forgero:axe-head");
	private static final Component AXE_HEAD_COMPONENT = new StructuredExtensiblePart(AXE_HEAD_ID, Set.of(new OpenIdentifier("forgero:axe_head")), List.of(), new ComponentStructure(Map.of(new OpenIdentifier("forgero:material"), new StructureSlot(new OpenIdentifier("forgero:material"), new OpenIdentifier("forgero:metal"), "", IRON_COMPONENT))), new ComponentUpgrades(List.of(new UpgradeSlot(new OpenIdentifier("forgero:rune_slot"), new OpenIdentifier("forgero:rune"), "", (comp) -> true, Optional.empty()))));

	private static final OpenIdentifier PICKAXE_ID = new OpenIdentifier("forgero", "iron-pickaxe");
	private static final Component PRISTINE_PICKAXE = new StructuredExtensibleEquipment(PICKAXE_ID, Set.of(new OpenIdentifier("forgero:pickaxe")), List.of(), new ComponentStructure(Map.of(new OpenIdentifier("forgero:head"), new StructureSlot(new OpenIdentifier("forgero:head"), new OpenIdentifier("forgero:pickaxe_head"), "", PICKAXE_HEAD_COMPONENT), new OpenIdentifier("forgero:handle"), new StructureSlot(new OpenIdentifier("forgero:handle"), new OpenIdentifier("forgero:handle"), "", HANDLE_COMPONENT))), new ComponentUpgrades(List.of(new UpgradeSlot(new OpenIdentifier("forgero:gem_slot"), new OpenIdentifier("forgero:gem"), "A slot for a gem", (comp) -> true, Optional.empty()))));

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

		// Phase 2: Test versioning
		assertEquals(1, serialized.getAsJsonObject().get("cof_version").getAsInt(), "Serialized object should have cof_version = 1");

		Component deserialized = codec.parse(JsonOps.INSTANCE, serialized).getOrThrow(false, System.err::println);
		assertEquals(mutated, deserialized, "Deserialized component should be equal to the mutated one for " + pristine.getClass().getSimpleName());
	}

	@Test
	void testStaticComponentCycle() {
		testPristineSerialization(IRON_COMPONENT);
		Component mutated = new StaticComponent(IRON_ID, Set.of(new OpenIdentifier("forgero:metal")), List.of(new SimpleAttribute(DefaultAttributes.DURABILITY, 100)));
		testMutatedCycle(IRON_COMPONENT, mutated);
	}

	@Test
	void testStructuredPartCycle() {
		testPristineSerialization(SWORD_BLADE_COMPONENT);
		ComponentMutater mutater = new ComponentMutaterImpl();
		Component mutated = mutater.setSlot(SWORD_BLADE_COMPONENT, new OpenIdentifier("forgero:material"), GOLD_COMPONENT);
		testMutatedCycle(SWORD_BLADE_COMPONENT, mutated);
	}

	@Test
	void testExtensiblePartCycle() {
		testPristineSerialization(HILT_COMPONENT);
		ComponentMutater mutater = new ComponentMutaterImpl();
		Component mutated = mutater.setSlot(HILT_COMPONENT, new OpenIdentifier("forgero:pommel_slot"), IRON_COMPONENT);
		testMutatedCycle(HILT_COMPONENT, mutated);
	}

	@Test
	void testStructuredEquipmentCycle() {
		testPristineSerialization(SWORD_COMPONENT);
		ComponentMutater mutater = new ComponentMutaterImpl();
		Component mutated = mutater.setSlot(SWORD_COMPONENT, new OpenIdentifier("forgero:handle"), FANCY_HILT_COMPONENT);
		testMutatedCycle(SWORD_COMPONENT, mutated);
	}

	@Test
	void testExtensibleEquipmentCycle() {
		testPristineSerialization(AMULET_COMPONENT);
		ComponentMutater mutater = new ComponentMutaterImpl();
		Component mutated = mutater.setSlot(AMULET_COMPONENT, new OpenIdentifier("forgero:gem_slot"), GEM_COMPONENT);
		testMutatedCycle(AMULET_COMPONENT, mutated);
	}

	@Test
	void testStaticEquipmentCycle() {
		testPristineSerialization(SHIELD_COMPONENT);
		Component mutated = new StaticEquipment(SHIELD_ID, Set.of(new OpenIdentifier("forgero:shield")), List.of(new SimpleAttribute(DefaultAttributes.ARMOR, 5)));
		testMutatedCycle(SHIELD_COMPONENT, mutated);
	}

	@Test
	void testStructuredExtensiblePartCycle() {
		testPristineSerialization(AXE_HEAD_COMPONENT);
		ComponentMutater mutater = new ComponentMutaterImpl();
		Component mutated = mutater.setSlot(AXE_HEAD_COMPONENT, new OpenIdentifier("forgero:rune_slot"), GEM_COMPONENT);
		testMutatedCycle(AXE_HEAD_COMPONENT, mutated);
	}

	@Test
	void testStructuredExtensibleEquipmentCycle() {
		testPristineSerialization(PRISTINE_PICKAXE);
		ComponentMutater mutater = new ComponentMutaterImpl();
		Component mutated = mutater.setSlot(PRISTINE_PICKAXE, new OpenIdentifier("forgero:gem_slot"), GEM_COMPONENT);
		testMutatedCycle(PRISTINE_PICKAXE, mutated);
	}


	@Test
	void testCustomComponentRegistrationAndCycle() {
		// Define a custom component class for testing
		record CustomComponent(OpenIdentifier id, Set<OpenIdentifier> tags, List<Property> properties) implements Component {
			@Override public List<Property> getProperties() { return properties; }
			@Override public Set<OpenIdentifier> getTags() { return tags; }
		}

		// Register the custom component type with the constructor registry
		constructorRegistry.register("custom:custom_component", CustomComponent.class, (dto, props, struct, upgs) ->
				DataResult.success(new CustomComponent(dto.id(), dto.tags() != null ? dto.tags() : Collections.emptySet(), props))
		);

		OpenIdentifier customId = new OpenIdentifier("custom:my_item");
		Component pristineCustom = new CustomComponent(customId, Set.of(), List.of());

		// Create a new registry for this test that includes the custom component
		ComponentRegistry customRegistry = componentRegistry.toBuilder().add(pristineCustom).build();

		Component mutatedCustom = new CustomComponent(customId, Set.of(new OpenIdentifier("custom:test_tag")), List.of());

		// Re-create codec with the new registry to pick up the pristine custom component
		ComponentCofCodec customCodec = new ComponentCofCodec(customRegistry, constructorRegistry);

		// Test serialization
		JsonElement serialized = customCodec.encodeStart(JsonOps.INSTANCE, mutatedCustom).getOrThrow(false, System.err::println);
		assertTrue(serialized.isJsonObject());
		assertEquals("custom:custom_component", serialized.getAsJsonObject().get("component_type").getAsString());

		// Test deserialization
		Component deserialized = customCodec.parse(JsonOps.INSTANCE, serialized).getOrThrow(false, System.err::println);
		assertEquals(mutatedCustom, deserialized);
		assertInstanceOf(CustomComponent.class, deserialized);
	}

	@Test
	void testSerializationCache() throws NoSuchFieldException, IllegalAccessException {
		// 1. Create the component to be serialized
		Component mutated = new StaticEquipment(SHIELD_ID, Set.of(new OpenIdentifier("forgero:shield")), List.of(new SimpleAttribute(DefaultAttributes.ARMOR, 5)));

		// 2. Use reflection to get access to the real cache instance
		Field cacheField = ComponentCofCodec.class.getDeclaredField("serializationCache");
		cacheField.setAccessible(true);
		@SuppressWarnings("unchecked")
		Cache<Component, JsonElement> cache = (Cache<Component, JsonElement>) cacheField.get(codec);
		cache.invalidateAll(); // Ensure cache is empty before the test

		// 3. First encoding: This should perform the serialization and populate the cache.
		JsonElement firstResult = codec.encodeStart(JsonOps.INSTANCE, mutated).getOrThrow(false, System.err::println);

		// 4. Assert cache was populated
		assertNotNull(cache.getIfPresent(mutated), "Cache should contain the component after the first serialization.");
		assertEquals(firstResult, cache.getIfPresent(mutated), "The value in the cache should match the result of the serialization.");

		// 5. Manually insert a different value into the cache for the same key.
		// This "poison pill" will let us verify that the cache is read on the next call.
		JsonObject poisonPill = new JsonObject();
		poisonPill.addProperty("cached", true);
		cache.put(mutated, poisonPill);

		// 6. Second encoding: This should hit the cache and return our "poison pill".
		DataResult<JsonElement> secondResult = codec.encodeStart(JsonOps.INSTANCE, mutated);

		// 7. Assert cache was hit
		assertTrue(secondResult.result().isPresent(), "The second encoding should succeed.");
		assertEquals(poisonPill, secondResult.result().get(), "Second call should return the value we manually put in the cache.");
		assertNotEquals(firstResult, secondResult.result().get(), "The result of the second call should be different from the first, proving the cache was used.");
	}
}
