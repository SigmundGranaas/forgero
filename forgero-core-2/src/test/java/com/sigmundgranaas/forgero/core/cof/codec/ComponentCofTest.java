package com.sigmundgranaas.forgero.core.cof.codec;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.cof.ComponentConstructor;
import com.sigmundgranaas.forgero.cof.codec.CofCodecs;
import com.sigmundgranaas.forgero.cof.codec.ComponentCofCodec;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeCodec;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.impl.*;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.component.mutation.impl.ComponentMutaterImpl;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.codec.KeyMapDispatchCodec;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.core.condition.api.ConditionCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class ComponentCofTest {
	private static final Component IRON_COMPONENT = new StaticComponent(IRON_ID, Set.of(METAL_TAG, MATERIAL_SLOT_TYPE), Collections.emptyMap());
	private static final Component GOLD_COMPONENT = new StaticComponent(GOLD_ID, Set.of(METAL_TAG, MATERIAL_SLOT_TYPE), Collections.emptyMap());
	private static final Component OAK_COMPONENT = new StaticComponent(OAK_ID, Set.of(WOOD_TAG), Collections.emptyMap());
	private static final Component PICKAXE_HEAD_COMPONENT = new StaticComponent(id("iron-pickaxe_head"), Set.of(PICKAXE_HEAD_TAG), Collections.emptyMap());
	private static final Component HANDLE_COMPONENT = new StaticComponent(id("oak-handle"), Set.of(HANDLE_TAG), Collections.emptyMap());
	private static final Component GEM_COMPONENT = new StaticComponent(GEM_ID, Set.of(GEM_TAG), Map.of(Attribute.KEY.key(), List.of(new SimpleAttribute(new OpenIdentifier("forgero:attack_damage"), 10))));
	private static final Component SWORD_BLADE_COMPONENT = new StructuredPart(id("sword-blade"), Set.of(SWORD_BLADE_TAG), Collections.emptyMap(),
			new ComponentStructure(Map.of(id("material"), new StructureSlot(id("material"), MATERIAL_SLOT_TYPE, "", IRON_COMPONENT)))
	);
	private static final Component HILT_COMPONENT = new ExtensiblePart(HILT_ID, Set.of(HILT_TAG), Collections.emptyMap(),
			new ComponentUpgrades(List.of(new UpgradeSlot(id("pommel_slot"), POMMEL_TAG, "", c -> true, Optional.empty())))
	);
	private static final Component FANCY_HILT_COMPONENT = new ExtensiblePart(id("fancy-hilt"), Set.of(HILT_TAG), Collections.emptyMap(),
			new ComponentUpgrades(List.of(new UpgradeSlot(id("pommel_slot"), POMMEL_TAG, "", c -> true, Optional.empty())))
	);
	private static final Component SWORD_COMPONENT = new StructuredEquipment(SWORD_ID, Set.of(SWORD_TAG), Collections.emptyMap(),
			new ComponentStructure(Map.of(
					id("blade"), new StructureSlot(id("blade"), SWORD_BLADE_TAG, "", SWORD_BLADE_COMPONENT),
					id("handle"), new StructureSlot(id("handle"), HILT_TAG, "", HILT_COMPONENT)
			))
	);
	private static final Component AMULET_COMPONENT = new ExtensibleEquipment(AMULET_ID, Set.of(AMULET_TAG), Collections.emptyMap(),
			new ComponentUpgrades(List.of(new UpgradeSlot(id("gem_slot"), GEM_TAG, "", c -> true, Optional.empty())))
	);
	private static final Component SHIELD_COMPONENT = new StaticEquipment(SHIELD_ID, Set.of(SHIELD_TAG), Collections.emptyMap());
	private static final Component AXE_HEAD_COMPONENT = new StructuredExtensiblePart(AXE_HEAD_ID, Set.of(AXE_HEAD_TAG), Collections.emptyMap(),
			new ComponentStructure(Map.of(id("material"), new StructureSlot(id("material"), MATERIAL_SLOT_TYPE, "", IRON_COMPONENT))),
			new ComponentUpgrades(List.of(new UpgradeSlot(id("rune_slot"), RUNE_TAG, "", c -> true, Optional.empty())))
	);
	private static final Component PRISTINE_PICKAXE = new StructuredExtensibleEquipment(id("iron-pickaxe"), Set.of(PICKAXE_TAG), Collections.emptyMap(),
			new ComponentStructure(Map.of(
					id("head"), new StructureSlot(id("head"), PICKAXE_HEAD_TAG, "", PICKAXE_HEAD_COMPONENT),
					id("handle"), new StructureSlot(id("handle"), HANDLE_TAG, "", HANDLE_COMPONENT)
			)),
			new ComponentUpgrades(List.of(new UpgradeSlot(id("gem_slot"), GEM_TAG, "", c -> true, Optional.empty())))
	);

	private ComponentCofCodec codec;
	private ComponentRegistry componentRegistry;
	private ComponentConstructor constructorRegistry;
	private ComponentMutater mutater;

	@BeforeEach
	void setUp() {
		constructorRegistry = ComponentConstructor.defaults();
		mutater = new ComponentMutaterImpl();
		ComponentRegistry.Builder registryBuilder = ComponentRegistry.builder();
		registryBuilder.add(IRON_COMPONENT).add(OAK_COMPONENT).add(PICKAXE_HEAD_COMPONENT)
				.add(HANDLE_COMPONENT).add(GEM_COMPONENT).add(PRISTINE_PICKAXE)
				.add(SWORD_BLADE_COMPONENT).add(HILT_COMPONENT).add(SWORD_COMPONENT)
				.add(AMULET_COMPONENT).add(SHIELD_COMPONENT).add(AXE_HEAD_COMPONENT)
				.add(GOLD_COMPONENT).add(FANCY_HILT_COMPONENT);

		componentRegistry = registryBuilder.build();

		Map<PropertyKey<?>, Codec<? extends List<?>>> propertyCodecs = new HashMap<>();
		propertyCodecs.put(Attribute.KEY, ListCodecWrapper.of(new AttributeCodec(new ConditionCodec(new HashMap<>(), new HashMap<>()))));
		KeyMapDispatchCodec propertyMapCodec = new KeyMapDispatchCodec(propertyCodecs);

		codec = new ComponentCofCodec(componentRegistry, constructorRegistry,
				CofCodecs.create(propertyMapCodec.codec())
		);
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

		// Asserting on the re-serialized JSON is a robust way to check for deep equality
		JsonElement reserialized = codec.encodeStart(JsonOps.INSTANCE, deserialized).getOrThrow(false, System.err::println);
		assertEquals(serialized, reserialized, "Re-serialized component should be identical to the original serialized JSON");
	}

	@Test
	void testStaticComponentCycle() {
		testPristineSerialization(IRON_COMPONENT);
		Component mutated = new StaticComponent(IRON_ID, Set.of(METAL_TAG), Map.of(Attribute.KEY.key(), List.of(new SimpleAttribute(new OpenIdentifier("forgero:durability"), 100f))));
		testMutatedCycle(IRON_COMPONENT, mutated);
	}

	@Test
	void testStructuredPartCycle() {
		testPristineSerialization(SWORD_BLADE_COMPONENT);
		Component mutated = mutater.setSlot(SWORD_BLADE_COMPONENT, id("material"), GOLD_COMPONENT);
		testMutatedCycle(SWORD_BLADE_COMPONENT, mutated);
	}

	@Test
	void testExtensiblePartCycle() {
		testPristineSerialization(HILT_COMPONENT);
		Component mutated = mutater.setSlot(HILT_COMPONENT, id("pommel_slot"), IRON_COMPONENT);
		testMutatedCycle(HILT_COMPONENT, mutated);
	}

	@Test
	void testStructuredEquipmentCycle() {
		testPristineSerialization(SWORD_COMPONENT);
		Component mutated = mutater.setSlot(SWORD_COMPONENT, id("handle"), FANCY_HILT_COMPONENT);
		testMutatedCycle(SWORD_COMPONENT, mutated);
	}

	@Test
	void testExtensibleEquipmentCycle() {
		testPristineSerialization(AMULET_COMPONENT);
		Component mutated = mutater.setSlot(AMULET_COMPONENT, id("gem_slot"), GEM_COMPONENT);
		testMutatedCycle(AMULET_COMPONENT, mutated);
	}

	@Test
	void testStaticEquipmentCycle() {
		testPristineSerialization(SHIELD_COMPONENT);
		Component mutated = new StaticEquipment(SHIELD_ID, Set.of(SHIELD_TAG), Map.of(Attribute.KEY.key(), List.of(new SimpleAttribute(new OpenIdentifier( "forgero:armor"), 5f))));
		testMutatedCycle(SHIELD_COMPONENT, mutated);
	}

	@Test
	void testStructuredExtensiblePartCycle() {
		testPristineSerialization(AXE_HEAD_COMPONENT);
		Component mutated = mutater.setSlot(AXE_HEAD_COMPONENT, id("rune_slot"), GEM_COMPONENT);
		testMutatedCycle(AXE_HEAD_COMPONENT, mutated);
	}

	@Test
	void testStructuredExtensibleEquipmentCycle() {
		testPristineSerialization(PRISTINE_PICKAXE);
		Component mutated = mutater.setSlot(PRISTINE_PICKAXE, id("gem_slot"), GEM_COMPONENT);
		testMutatedCycle(PRISTINE_PICKAXE, mutated);
	}

	@Test
	void testSerializationCache() throws NoSuchFieldException, IllegalAccessException {
		Component mutated = new StaticEquipment(SHIELD_ID, Set.of(id("forgero:shield")), Map.of(Attribute.KEY.key(), List.of(new SimpleAttribute(new OpenIdentifier( "forgero:armor"), 5f))));

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
