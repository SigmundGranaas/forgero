package com.sigmundgranaas.forgero.loader.gametest;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Game tests for the ForgeroServices API and related systems.
 * These tests verify that the DI infrastructure works correctly in a real Minecraft environment.
 */
public class ForgeroServicesGameTest {

	// Services received from ForgeroInitializedCallback
	private static ForgeroServices services;
	private static TagResolver tagResolver;
	private static ComponentConverter converter;
	private static Resolver resolver;
	private static ComponentRegistry componentRegistry;
	private static TaggedRegistry<Component> taggedComponents;
	private static ComponentNbtConverter nbtConverter;

	static {
		ForgeroInitializedCallback.EVENT.register(s -> {
			services = s;
			tagResolver = s.tagResolver();
			converter = s.converter();
			resolver = s.resolver();
			componentRegistry = s.componentRegistry();
			taggedComponents = s.taggedComponents();
			nbtConverter = s.nbtConverter();
		});
	}

	/**
	 * Verifies that ForgeroInitializedCallback provides a valid ForgeroServices instance.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testForgeroServicesAvailable(TestContext context) {
		assertNotNull(services, "ForgeroServices should be available via callback");
		context.complete();
	}

	/**
	 * Verifies that the TagResolver is available and functional.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testTagResolverAvailable(TestContext context) {
		assertNotNull(tagResolver, "TagResolver should be available");

		// Verify it's not the empty resolver (should have loaded tags)
		assertFalse(tagResolver.getAllTags().isEmpty(), "TagResolver should have loaded tags");

		context.complete();
	}

	/**
	 * Verifies that the ComponentRegistry is populated with components.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testComponentRegistryPopulated(TestContext context) {
		assertNotNull(componentRegistry, "ComponentRegistry should be available");
		assertFalse(componentRegistry.all().isEmpty(), "ComponentRegistry should contain components");

		context.complete();
	}

	/**
	 * Verifies that the Resolver is available for attribute computation.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testResolverAvailable(TestContext context) {
		assertNotNull(resolver, "Resolver should be available");

		context.complete();
	}

	/**
	 * Verifies that the ComponentConverter is available.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testConverterAvailable(TestContext context) {
		assertNotNull(converter, "ComponentConverter should be available");

		context.complete();
	}

	/**
	 * Verifies that the TaggedRegistry for components works correctly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testTaggedComponentsAvailable(TestContext context) {
		assertNotNull(taggedComponents, "TaggedRegistry should be available");

		context.complete();
	}

	/**
	 * Verifies that the NbtConverter is available.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testNbtConverterAvailable(TestContext context) {
		assertNotNull(nbtConverter, "NbtConverter should be available");

		context.complete();
	}

	/**
	 * Verifies that all ForgeroServices methods return consistent values.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testServicesConsistency(TestContext context) {
		// Verify that accessing via services methods returns the same instances as stored
		assertSame(services.tagResolver(), tagResolver,
				"TagResolver should be the same instance");
		assertSame(services.converter(), converter,
				"Converter should be the same instance");
		assertSame(services.resolver(), resolver,
				"Resolver should be the same instance");
		assertSame(services.componentRegistry(), componentRegistry,
				"ComponentRegistry should be the same instance");

		context.complete();
	}

	/**
	 * Verifies that TagResolver can query tag relationships.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testTagResolverQueries(TestContext context) {
		// Get all tags and verify we can query them
		var allTags = tagResolver.getAllTags();
		assertFalse(allTags.isEmpty(), "Should have tags loaded");

		// Pick a tag and verify we can get its descendants
		var firstTag = allTags.iterator().next();
		var descendants = tagResolver.getDescendants(firstTag);
		assertNotNull(descendants, "getDescendants should return non-null");
		assertTrue(descendants.contains(firstTag), "Descendants should include the tag itself");

		context.complete();
	}

	/**
	 * Verifies that components can be looked up by ID.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testComponentLookup(TestContext context) {
		var allComponents = componentRegistry.all();

		if (!allComponents.isEmpty()) {
			Component firstComponent = allComponents.iterator().next();
			var lookedUp = componentRegistry.get(firstComponent.id());

			assertTrue(lookedUp.isPresent(), "Should be able to look up component by ID");
			assertEquals(firstComponent.id(), lookedUp.get().id(), "Looked up component should match");
		}

		context.complete();
	}
}
