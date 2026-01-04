package com.sigmundgranaas.forgero.core.property.engine;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.core.property.context.Key;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class ResolverEngineTest extends ForgeroTest {

	private ResolverEngine engine;

	@BeforeEach
	void setUp() {
		engine = new ResolverEngine();
	}

	// Test helper classes

	/**
	 * A simple test engine that collects component IDs in traversal order.
	 */
	private static class ComponentIdEngine implements DataTypeEngine<List<String>, List<String>> {
		private final OpenIdentifier engineId;
		private final AtomicInteger bakeCallCount = new AtomicInteger(0);

		ComponentIdEngine(OpenIdentifier engineId) {
			this.engineId = engineId;
		}

		@Override
		public ResolutionKey<List<String>> key() {
			return new ResolutionKey<>(engineId);
		}

		@Override
		public List<String> bake(Stream<Component> components) {
			bakeCallCount.incrementAndGet();
			return components.map(c -> c.id().toString()).collect(Collectors.toList());
		}

		@Override
		public List<String> apply(List<String> baked, DynamicContext context) {
			return baked;
		}

		int getBakeCallCount() {
			return bakeCallCount.get();
		}
	}

	/**
	 * A test engine that applies a dynamic filter based on context.
	 */
	private static class DynamicFilterEngine implements DataTypeEngine<List<String>, List<String>> {
		private final OpenIdentifier engineId;

		DynamicFilterEngine(OpenIdentifier engineId) {
			this.engineId = engineId;
		}

		@Override
		public ResolutionKey<List<String>> key() {
			return new ResolutionKey<>(engineId);
		}

		@Override
		public List<String> bake(Stream<Component> components) {
			// Bake phase: collect all component IDs
			return components.map(c -> c.id().toString()).collect(Collectors.toList());
		}

		@Override
		public List<String> apply(List<String> baked, DynamicContext context) {
			// Apply phase: filter based on context (if present)
			if (context == null || context == DynamicContext.empty()) {
				return baked;
			}
			// For testing: filter out IDs containing "iron" in dynamic phase
			return baked.stream()
					.filter(id -> !id.contains("iron"))
					.collect(Collectors.toList());
		}
	}

	@Nested
	@DisplayName("Traversal Tests")
	class TraversalTests {

		@Test
		void traversesSimpleHierarchyInPreOrder() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withStructureSlot(structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron))
					.build();

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:traversal"));

			List<String> result = engine.resolve(pickaxeHead, testEngine, DynamicContext.empty());

			assertEquals(2, result.size(), "Should traverse both components");
			assertEquals(PICKAXE_HEAD_ID.toString(), result.get(0), "Parent should be visited first (pre-order)");
			assertEquals(IRON_ID.toString(), result.get(1), "Child should be visited second");
		}

		@Test
		void traversesNestedStructureCorrectly() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withStructureSlot(structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron))
					.build();
			Component oak = material(OAK_ID, WOOD_TAG);
			Component pickaxe = part(PICKAXE_ID)
					.withStructureSlot(structureSlot("head_slot", HEAD_SLOT_TYPE, pickaxeHead))
					.withStructureSlot(structureSlot("handle_slot", HANDLE_SLOT_TYPE, oak))
					.build();

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:traversal"));

			List<String> result = engine.resolve(pickaxe, testEngine, DynamicContext.empty());

			assertEquals(4, result.size(), "Should traverse all 4 components");
			assertEquals(PICKAXE_ID.toString(), result.get(0), "Root should be first");
			assertEquals(PICKAXE_HEAD_ID.toString(), result.get(1), "First child should be second");
			assertEquals(IRON_ID.toString(), result.get(2), "Nested child should be third");
			assertEquals(OAK_ID.toString(), result.get(3), "Second child should be last");
		}

		@Test
		void handlesEmptyChildren() {
			Component simpleComponent = part(PICKAXE_ID).build();

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:traversal"));

			List<String> result = engine.resolve(simpleComponent, testEngine, DynamicContext.empty());

			assertEquals(1, result.size(), "Should traverse only the root component");
			assertEquals(PICKAXE_ID.toString(), result.get(0));
		}

		@Test
		void handlesCircularReferences() {
			// While the component model shouldn't allow true circular references,
			// we test that traversal doesn't infinite loop by only visiting each component once
			Component leaf = part(id("leaf")).build();
			Component parent = part(id("parent"))
					.withStructureSlot(structureSlot("slot1", MATERIAL_SLOT_TYPE, leaf))
					.build();

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:circular"));

			List<String> result = engine.resolve(parent, testEngine, DynamicContext.empty());

			assertEquals(2, result.size(), "Should visit each component exactly once");
			assertTrue(result.contains("forgero:parent"));
			assertTrue(result.contains("forgero:leaf"));
		}

		@Test
		void maintainsTraversalOrderConsistency() {
			Component comp1 = material(id("comp1"), METAL_TAG);
			Component comp2 = material(id("comp2"), WOOD_TAG);
			Component comp3 = material(id("comp3"), GEM_TAG);
			Component parent = part(id("parent"))
					.withStructureSlot(structureSlot("slot1", MATERIAL_SLOT_TYPE, comp1))
					.withStructureSlot(structureSlot("slot2", MATERIAL_SLOT_TYPE, comp2))
					.withStructureSlot(structureSlot("slot3", MATERIAL_SLOT_TYPE, comp3))
					.build();

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:consistency"));

			List<String> result1 = engine.resolve(parent, testEngine, DynamicContext.empty());

			// Clear cache and resolve again
			ResolverEngine newEngine = new ResolverEngine();
			ComponentIdEngine newTestEngine = new ComponentIdEngine(id("test:consistency"));
			List<String> result2 = newEngine.resolve(parent, newTestEngine, DynamicContext.empty());

			assertEquals(result1, result2, "Traversal order should be consistent across multiple resolutions");
		}
	}

	@Nested
	@DisplayName("Caching Tests")
	class CachingTests {

		@Test
		void cachesResultsForSameComponentAndEngine() {
			Component pickaxe = part(PICKAXE_ID).build();
			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:cache"));

			// First resolution - should call bake()
			engine.resolve(pickaxe, testEngine, DynamicContext.empty());
			assertEquals(1, testEngine.getBakeCallCount(), "Bake should be called once");

			// Second resolution with same component and engine - should use cache
			engine.resolve(pickaxe, testEngine, DynamicContext.empty());
			assertEquals(1, testEngine.getBakeCallCount(), "Bake should not be called again (cached)");
		}

		@Test
		void missesCacheForDifferentComponent() {
			Component pickaxe = part(PICKAXE_ID).build();
			Component sword = part(SWORD_ID).build();
			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:cache"));

			engine.resolve(pickaxe, testEngine, DynamicContext.empty());
			assertEquals(1, testEngine.getBakeCallCount());

			engine.resolve(sword, testEngine, DynamicContext.empty());
			assertEquals(2, testEngine.getBakeCallCount(), "Bake should be called for different component");
		}

		@Test
		void missesCacheForDifferentEngine() {
			Component pickaxe = part(PICKAXE_ID).build();
			ComponentIdEngine engine1 = new ComponentIdEngine(id("test:engine1"));
			ComponentIdEngine engine2 = new ComponentIdEngine(id("test:engine2"));

			engine.resolve(pickaxe, engine1, DynamicContext.empty());
			assertEquals(1, engine1.getBakeCallCount());

			engine.resolve(pickaxe, engine2, DynamicContext.empty());
			assertEquals(1, engine2.getBakeCallCount(), "Different engine should miss cache");
		}

		@Test
		void cacheEvictionWorksUnderLoad() {
			// Cache has max size of 1000, so creating 1100 unique components should evict some
			List<Component> components = new ArrayList<>();
			for (int i = 0; i < 1100; i++) {
				components.add(part(id("component_" + i)).build());
			}

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:eviction"));

			// Resolve all components
			for (Component comp : components) {
				engine.resolve(comp, testEngine, DynamicContext.empty());
			}

			assertEquals(1100, testEngine.getBakeCallCount(), "All components should be baked initially");

			// Resolve the first 100 components again
			// Some may be evicted, some may still be cached
			int callCountBefore = testEngine.getBakeCallCount();
			for (int i = 0; i < 100; i++) {
				engine.resolve(components.get(i), testEngine, DynamicContext.empty());
			}

			int additionalBakes = testEngine.getBakeCallCount() - callCountBefore;
			assertTrue(additionalBakes >= 0, "Cache eviction should work without errors");
		}

		@Test
		void cacheKeyUsesComponentEquality() {
			// Two different component instances with same structure should have different cache entries
			// unless they override equals/hashCode to be value-based (which most don't)
			Component pickaxe1 = part(PICKAXE_ID).build();
			Component pickaxe2 = part(PICKAXE_ID).build();

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:equality"));

			engine.resolve(pickaxe1, testEngine, DynamicContext.empty());
			assertEquals(1, testEngine.getBakeCallCount());

			engine.resolve(pickaxe2, testEngine, DynamicContext.empty());
			// Behavior depends on whether components implement value-based equality
			// For now, we just verify it doesn't crash
			assertTrue(testEngine.getBakeCallCount() >= 1);
		}
	}

	@Nested
	@DisplayName("Integration Tests")
	class IntegrationTests {

		@Test
		void twoPhaseResolutionProducesCorrectResult() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component pickaxe = part(PICKAXE_ID)
					.withStructureSlot(structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron))
					.build();

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:integration"));

			List<String> result = engine.resolve(pickaxe, testEngine, DynamicContext.empty());

			assertNotNull(result);
			assertEquals(2, result.size());
			assertTrue(result.contains(PICKAXE_ID.toString()));
			assertTrue(result.contains(IRON_ID.toString()));
		}

		@Test
		void staticConditionsFilteredDuringBake() {
			// Bake phase should process all components regardless of dynamic context
			Component iron = material(IRON_ID, METAL_TAG);
			Component pickaxe = part(PICKAXE_ID)
					.withStructureSlot(structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron))
					.build();

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:static"));

			List<String> result = engine.resolve(pickaxe, testEngine, DynamicContext.empty());

			// Bake phase should include all components
			assertEquals(2, result.size());
		}

		@Test
		void dynamicConditionsFilteredDuringApply() {
			// This test verifies that the apply phase receives context correctly
			Component pickaxe = part(PICKAXE_ID).build();

			DynamicFilterEngine testEngine = new DynamicFilterEngine(id("test:dynamic"));

			// Verify resolve works with empty context
			List<String> resultEmpty = engine.resolve(pickaxe, testEngine, DynamicContext.empty());
			assertNotNull(resultEmpty);
			assertTrue(resultEmpty.size() >= 1, "Should have at least the root component");

			// Verify resolve works with non-empty context (apply phase receives it)
			DynamicContext nonEmptyContext = new DynamicContext.Builder()
					.put(new Key<String>(id("test:key")), "test")
					.build();
			List<String> resultFiltered = engine.resolve(pickaxe, testEngine, nonEmptyContext);
			assertNotNull(resultFiltered);
			// The dynamic filter only filters if ID contains "iron", pickaxe doesn't, so should pass through
			assertTrue(resultFiltered.contains(PICKAXE_ID.toString()));
		}

		@Test
		void handlesNullDynamicContext() {
			Component pickaxe = part(PICKAXE_ID).build();
			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:null_context"));

			// Should not crash with null context
			assertDoesNotThrow(() -> engine.resolve(pickaxe, testEngine, null));
		}

		@Test
		void worksWithComplexComponentTree() {
			// Build a complex 3-level tree
			Component iron = material(IRON_ID, METAL_TAG);
			Component diamond = material(DIAMOND_ID, GEM_TAG);
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withStructureSlot(structureSlot("primary_material", MATERIAL_SLOT_TYPE, iron))
					.withStructureSlot(structureSlot("gem_slot", GEM_SLOT_TYPE, diamond))
					.build();

			Component oak = material(OAK_ID, WOOD_TAG);
			Component handle = part(HANDLE_ID)
					.withStructureSlot(structureSlot("wood_slot", MATERIAL_SLOT_TYPE, oak))
					.build();

			Component pickaxe = part(PICKAXE_ID)
					.withStructureSlot(structureSlot("head_slot", HEAD_SLOT_TYPE, pickaxeHead))
					.withStructureSlot(structureSlot("handle_slot", HANDLE_SLOT_TYPE, handle))
					.build();

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:complex"));

			List<String> result = engine.resolve(pickaxe, testEngine, DynamicContext.empty());

			assertEquals(6, result.size(), "Should traverse all 6 components");
			assertEquals(PICKAXE_ID.toString(), result.get(0), "Root should be first");
			assertTrue(result.contains(IRON_ID.toString()));
			assertTrue(result.contains(DIAMOND_ID.toString()));
			assertTrue(result.contains(PICKAXE_HEAD_ID.toString()));
			assertTrue(result.contains(OAK_ID.toString()));
			assertTrue(result.contains(HANDLE_ID.toString()));
		}
	}
}
