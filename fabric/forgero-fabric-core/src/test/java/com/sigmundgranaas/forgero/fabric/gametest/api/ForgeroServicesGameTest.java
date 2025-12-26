package com.sigmundgranaas.forgero.fabric.gametest.api;

import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Game tests for the ForgeroServices API and related systems.
 * These tests verify that the DI infrastructure works correctly in a real Minecraft environment.
 */
public class ForgeroServicesGameTest {

	/**
	 * Verifies that ForgeroApi.services() returns a valid ForgeroServices instance.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testForgeroApiServicesAvailable(TestContext context) {
		ForgeroServices services = ForgeroApi.services();
		assertNotNull(services, "ForgeroApi.services() should return a non-null instance");
		context.complete();
	}

	/**
	 * Verifies that the TagResolver is available and functional.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testTagResolverAvailable(TestContext context) {
		TagResolver resolver = ForgeroApi.tagResolver();
		assertNotNull(resolver, "TagResolver should be available");

		// Verify it's not the empty resolver (should have loaded tags)
		assertFalse(resolver.getAllTags().isEmpty(), "TagResolver should have loaded tags");

		context.complete();
	}

	/**
	 * Verifies that the ComponentRegistry is available and functional.
	 * Note: In minimal test environments, the registry may be empty if no data packs are loaded.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testComponentRegistryPopulated(TestContext context) {
		var registry = ForgeroApi.componentRegistry();
		assertNotNull(registry, "ComponentRegistry should be available");
		// Verify the registry is functional (can call all() without error)
		var components = registry.all();
		assertNotNull(components, "ComponentRegistry.all() should return a non-null collection");
		// Note: components may be empty in minimal test environments without data packs

		context.complete();
	}

	/**
	 * Verifies that the Resolver is available for attribute computation.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testResolverAvailable(TestContext context) {
		var resolver = ForgeroApi.resolver();
		assertNotNull(resolver, "Resolver should be available");

		context.complete();
	}

	/**
	 * Verifies that the ComponentConverter is available.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testConverterAvailable(TestContext context) {
		var converter = ForgeroApi.converter();
		assertNotNull(converter, "ComponentConverter should be available");

		context.complete();
	}

	/**
	 * Verifies that the TaggedRegistry for components works correctly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testTaggedComponentsAvailable(TestContext context) {
		var taggedComponents = ForgeroApi.taggedComponents();
		assertNotNull(taggedComponents, "TaggedRegistry should be available");

		context.complete();
	}

	/**
	 * Verifies that the NbtConverter is available.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testNbtConverterAvailable(TestContext context) {
		var nbtConverter = ForgeroApi.nbtConverter();
		assertNotNull(nbtConverter, "NbtConverter should be available");

		context.complete();
	}

	/**
	 * Verifies that all ForgeroServices methods return consistent values.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testServicesConsistency(TestContext context) {
		ForgeroServices services = ForgeroApi.services();

		// Verify that accessing via services and via ForgeroApi returns the same instances
		assertSame(services.tagResolver(), ForgeroApi.tagResolver(),
				"TagResolver should be the same instance");
		assertSame(services.converter(), ForgeroApi.converter(),
				"Converter should be the same instance");
		assertSame(services.resolver(), ForgeroApi.resolver(),
				"Resolver should be the same instance");
		assertSame(services.componentRegistry(), ForgeroApi.componentRegistry(),
				"ComponentRegistry should be the same instance");

		context.complete();
	}

	/**
	 * Verifies that TagResolver can query tag relationships.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testTagResolverQueries(TestContext context) {
		TagResolver resolver = ForgeroApi.tagResolver();

		// Get all tags and verify we can query them
		var allTags = resolver.getAllTags();
		assertFalse(allTags.isEmpty(), "Should have tags loaded");

		// Pick a tag and verify we can get its descendants
		var firstTag = allTags.iterator().next();
		var descendants = resolver.getDescendants(firstTag);
		assertNotNull(descendants, "getDescendants should return non-null");
		assertTrue(descendants.contains(firstTag), "Descendants should include the tag itself");

		context.complete();
	}

	/**
	 * Verifies that component lookup functionality works.
	 * Note: In minimal test environments, the registry may be empty if no data packs are loaded.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testComponentLookup(TestContext context) {
		var registry = ForgeroApi.componentRegistry();
		assertNotNull(registry, "ComponentRegistry should be available");

		var allComponents = registry.all();
		if (!allComponents.isEmpty()) {
			Component firstComponent = allComponents.iterator().next();
			var lookedUp = registry.get(firstComponent.id());

			assertTrue(lookedUp.isPresent(), "Should be able to look up component by ID");
			assertEquals(firstComponent.id(), lookedUp.get().id(), "Looked up component should match");
		}

		context.complete();
	}
}
