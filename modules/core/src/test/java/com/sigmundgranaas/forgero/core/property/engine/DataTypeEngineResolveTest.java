package com.sigmundgranaas.forgero.core.property.engine;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ComponentTraversal;
import com.sigmundgranaas.forgero.core.property.api.DataTypeEngine;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.core.property.context.Key;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the DataTypeEngine.resolve() default method and ComponentTraversal utility.
 *
 * <p>This validates that:
 * <ul>
 *   <li>Component trees are traversed correctly in pre-order</li>
 *   <li>The bake phase receives all components from traversal</li>
 *   <li>The apply phase receives the dynamic context</li>
 *   <li>The resolve() convenience method correctly chains traversal, bake, and apply</li>
 * </ul>
 */
@DisplayName("DataTypeEngine Resolution Tests")
class DataTypeEngineResolveTest extends ForgeroTest {

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
	@DisplayName("ComponentTraversal Tests")
	class TraversalTests {

		@Test
		void traversesSimpleHierarchyInPreOrder() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component pickaxeHead = part(PICKAXE_HEAD_ID)
					.withStructureSlot(structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron))
					.build();

			List<Component> result = ComponentTraversal.traverse(pickaxeHead);

			assertEquals(2, result.size(), "Should traverse both components");
			assertEquals(PICKAXE_HEAD_ID.toString(), result.get(0).id().toString(), "Parent should be visited first (pre-order)");
			assertEquals(IRON_ID.toString(), result.get(1).id().toString(), "Child should be visited second");
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

			List<Component> result = ComponentTraversal.traverse(pickaxe);

			assertEquals(4, result.size(), "Should traverse all 4 components");
			assertEquals(PICKAXE_ID.toString(), result.get(0).id().toString(), "Root should be first");
			assertEquals(PICKAXE_HEAD_ID.toString(), result.get(1).id().toString(), "First child should be second");
			assertEquals(IRON_ID.toString(), result.get(2).id().toString(), "Nested child should be third");
			assertEquals(OAK_ID.toString(), result.get(3).id().toString(), "Second child should be last");
		}

		@Test
		void handlesEmptyChildren() {
			Component simpleComponent = part(PICKAXE_ID).build();

			List<Component> result = ComponentTraversal.traverse(simpleComponent);

			assertEquals(1, result.size(), "Should traverse only the root component");
			assertEquals(PICKAXE_ID.toString(), result.get(0).id().toString());
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

			List<Component> result1 = ComponentTraversal.traverse(parent);
			List<Component> result2 = ComponentTraversal.traverse(parent);

			assertEquals(result1.size(), result2.size(), "Traversal should return same number of components");
			for (int i = 0; i < result1.size(); i++) {
				assertEquals(result1.get(i).id(), result2.get(i).id(), "Traversal order should be consistent");
			}
		}
	}

	@Nested
	@DisplayName("Engine Resolve Tests")
	class EngineResolveTests {

		@Test
		void resolveMethodChainsBakeAndApply() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component pickaxe = part(PICKAXE_ID)
					.withStructureSlot(structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron))
					.build();

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:resolve"));

			// Use the default resolve method
			List<String> result = testEngine.resolve(pickaxe, DynamicContext.empty());

			assertNotNull(result);
			assertEquals(2, result.size());
			assertTrue(result.contains(PICKAXE_ID.toString()));
			assertTrue(result.contains(IRON_ID.toString()));
			assertEquals(1, testEngine.getBakeCallCount(), "Bake should be called once");
		}

		@Test
		void resolveWithoutContextUsesEmptyContext() {
			Component pickaxe = part(PICKAXE_ID).build();

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:no_context"));

			// Use the convenience resolve(component) method
			List<String> result = testEngine.resolve(pickaxe);

			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(PICKAXE_ID.toString(), result.get(0));
		}

		@Test
		void dynamicContextPassedToApplyPhase() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component pickaxe = part(PICKAXE_ID)
					.withStructureSlot(structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron))
					.build();

			DynamicFilterEngine testEngine = new DynamicFilterEngine(id("test:dynamic"));

			// With empty context - no filtering
			List<String> resultEmpty = testEngine.resolve(pickaxe, DynamicContext.empty());
			assertEquals(2, resultEmpty.size(), "Empty context should not filter");

			// With non-empty context - filtering applies
			DynamicContext filterContext = new DynamicContext.Builder()
					.put(new Key<String>(id("test:key")), "trigger_filter")
					.build();
			List<String> resultFiltered = testEngine.resolve(pickaxe, filterContext);

			// The filter removes IDs containing "iron"
			assertEquals(1, resultFiltered.size(), "Should filter out iron");
			assertFalse(resultFiltered.contains(IRON_ID.toString()), "Iron should be filtered out");
			assertTrue(resultFiltered.contains(PICKAXE_ID.toString()), "Pickaxe should remain");
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

			List<String> result = testEngine.resolve(pickaxe, DynamicContext.empty());

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
