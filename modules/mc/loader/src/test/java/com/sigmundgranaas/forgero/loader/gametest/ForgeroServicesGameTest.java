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

	/**
	 * Gets the ForgeroServices, either from the stored static accessor or by registering
	 * for the callback if not yet available.
	 */
	private static ForgeroServices getServices() {
		return ForgeroInitializedCallback.getServices().orElse(null);
	}

	private static TagResolver getTagResolver() {
		ForgeroServices s = getServices();
		return s != null ? s.tagResolver() : null;
	}

	private static ComponentConverter getConverter() {
		ForgeroServices s = getServices();
		return s != null ? s.converter() : null;
	}

	private static Resolver getResolver() {
		ForgeroServices s = getServices();
		return s != null ? s.resolver() : null;
	}

	private static ComponentRegistry getComponentRegistry() {
		ForgeroServices s = getServices();
		return s != null ? s.componentRegistry() : null;
	}

	private static TaggedRegistry<Component> getTaggedComponents() {
		ForgeroServices s = getServices();
		return s != null ? s.taggedComponents() : null;
	}

	private static ComponentNbtConverter getNbtConverter() {
		ForgeroServices s = getServices();
		return s != null ? s.nbtConverter() : null;
	}

	/**
	 * Verifies that ForgeroInitializedCallback provides a valid ForgeroServices instance.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testForgeroServicesAvailable(TestContext context) {
		assertNotNull(getServices(), "ForgeroServices should be available via callback");
		context.complete();
	}

	/**
	 * Verifies that the TagResolver is available and functional.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testTagResolverAvailable(TestContext context) {
		TagResolver tagResolver = getTagResolver();
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
		ComponentRegistry componentRegistry = getComponentRegistry();
		assertNotNull(componentRegistry, "ComponentRegistry should be available");
		assertFalse(componentRegistry.all().isEmpty(), "ComponentRegistry should contain components");

		context.complete();
	}

	/**
	 * Verifies that the Resolver is available for attribute computation.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testResolverAvailable(TestContext context) {
		assertNotNull(getResolver(), "Resolver should be available");

		context.complete();
	}

	/**
	 * Verifies that the ComponentConverter is available.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testConverterAvailable(TestContext context) {
		assertNotNull(getConverter(), "ComponentConverter should be available");

		context.complete();
	}

	/**
	 * Verifies that the TaggedRegistry for components works correctly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testTaggedComponentsAvailable(TestContext context) {
		assertNotNull(getTaggedComponents(), "TaggedRegistry should be available");

		context.complete();
	}

	/**
	 * Verifies that the NbtConverter is available.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testNbtConverterAvailable(TestContext context) {
		assertNotNull(getNbtConverter(), "NbtConverter should be available");

		context.complete();
	}

	/**
	 * Verifies that all ForgeroServices methods return consistent values.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testServicesConsistency(TestContext context) {
		ForgeroServices services = getServices();
		assertNotNull(services, "Services must be available for consistency check");

		// Verify that accessing via services methods returns the same instances
		assertSame(services.tagResolver(), getTagResolver(),
				"TagResolver should be the same instance");
		assertSame(services.converter(), getConverter(),
				"Converter should be the same instance");
		assertSame(services.resolver(), getResolver(),
				"Resolver should be the same instance");
		assertSame(services.componentRegistry(), getComponentRegistry(),
				"ComponentRegistry should be the same instance");

		context.complete();
	}

	/**
	 * Verifies that TagResolver can query tag relationships.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_services", required = true)
	public void testTagResolverQueries(TestContext context) {
		TagResolver tagResolver = getTagResolver();
		assertNotNull(tagResolver, "TagResolver must be available");

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
		ComponentRegistry componentRegistry = getComponentRegistry();
		assertNotNull(componentRegistry, "ComponentRegistry must be available");

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
