package com.sigmundgranaas.forgero.core.property.engine;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ComponentTraversal;
import com.sigmundgranaas.forgero.core.property.api.CompilerPass;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
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
 * Tests for the CompilerPass.resolve() default method and ComponentTraversal utility.
 *
 * <p>This validates that:
 * <ul>
 *   <li>Component trees are traversed correctly in pre-order</li>
 *   <li>The compile phase receives all components from traversal</li>
 *   <li>compile finalizes the result in a single phase without any runtime context</li>
 *   <li>The resolve() convenience method correctly chains traversal and compile</li>
 * </ul>
 */
@DisplayName("CompilerPass Resolution Tests")
class CompilerPassResolveTest extends ForgeroTest {

	// Test helper classes

	/**
	 * A simple test pass that collects component IDs in traversal order.
	 */
	private static class ComponentIdEngine implements CompilerPass<List<String>> {
		private final OpenIdentifier engineId;
		private final AtomicInteger compileCallCount = new AtomicInteger(0);

		ComponentIdEngine(OpenIdentifier engineId) {
			this.engineId = engineId;
		}

		@Override
		public ResolutionKey<List<String>> key() {
			return new ResolutionKey<>(engineId);
		}

		@Override
		public List<String> compile(Stream<Component> components) {
			compileCallCount.incrementAndGet();
			return components.map(c -> c.id().toString()).collect(Collectors.toList());
		}

		int getCompileCallCount() {
			return compileCallCount.get();
		}
	}

	/**
	 * A test pass that transforms the compiled result, proving the
	 * finalize step runs as part of resolve(). No runtime context is involved.
	 */
	private static class UppercasingEngine implements CompilerPass<List<String>> {
		private final OpenIdentifier engineId;

		UppercasingEngine(OpenIdentifier engineId) {
			this.engineId = engineId;
		}

		@Override
		public ResolutionKey<List<String>> key() {
			return new ResolutionKey<>(engineId);
		}

		@Override
		public List<String> compile(Stream<Component> components) {
			// Single-phase compile: collect all component IDs and finalize via transformation
			return components
					.map(c -> c.id().toString())
					.map(String::toUpperCase)
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
	@DisplayName("Pass Resolve Tests")
	class EngineResolveTests {

		@Test
		void resolveMethodChainsTraversalAndCompile() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component pickaxe = part(PICKAXE_ID)
					.withStructureSlot(structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron))
					.build();

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:resolve"));

			// Use the default resolve method
			List<String> result = testEngine.resolve(pickaxe);

			assertNotNull(result);
			assertEquals(2, result.size());
			assertTrue(result.contains(PICKAXE_ID.toString()));
			assertTrue(result.contains(IRON_ID.toString()));
			assertEquals(1, testEngine.getCompileCallCount(), "Compile should be called once");
		}

		@Test
		void resolveIsAPureCompileTimeOperation() {
			Component pickaxe = part(PICKAXE_ID).build();

			ComponentIdEngine testEngine = new ComponentIdEngine(id("test:no_context"));

			// resolve(component) takes no runtime state of any kind
			List<String> result = testEngine.resolve(pickaxe);

			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(PICKAXE_ID.toString(), result.get(0));
		}

		@Test
		void compileFinalizesResult() {
			Component iron = material(IRON_ID, METAL_TAG);
			Component pickaxe = part(PICKAXE_ID)
					.withStructureSlot(structureSlot("material_slot", MATERIAL_SLOT_TYPE, iron))
					.build();

			UppercasingEngine testEngine = new UppercasingEngine(id("test:finalize"));

			List<String> result = testEngine.resolve(pickaxe);

			assertEquals(2, result.size(), "Compile should preserve all entries");
			assertTrue(result.contains(IRON_ID.toString().toUpperCase()), "Compile transformation should run");
			assertTrue(result.contains(PICKAXE_ID.toString().toUpperCase()), "Compile transformation should run");
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

			List<String> result = testEngine.resolve(pickaxe);

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
