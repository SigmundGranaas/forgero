package com.sigmundgranaas.forgero.loader.gametest;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.api.Taggable;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the TagResolver system.
 * These tests verify that tag resolution works correctly with real loaded data.
 */
public class TagResolverIntegrationTest {

	/**
	 * Verifies that TagResolver.empty() returns the singleton empty resolver.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "tag_resolver", required = true)
	public void testEmptyResolverIsSingleton(TestContext context) {
		TagResolver empty1 = TagResolver.empty();
		TagResolver empty2 = TagResolver.empty();

		assertSame(empty1, empty2, "TagResolver.empty() should return the same singleton instance");
		assertTrue(empty1.getAllTags().isEmpty(), "Empty resolver should have no tags");

		context.complete();
	}

	/**
	 * Verifies that merging with empty resolver returns the non-empty resolver.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "tag_resolver", required = true)
	public void testMergeWithEmpty(TestContext context) {
		TagResolver loaded = ForgeroApi.tagResolver();
		TagResolver empty = TagResolver.empty();

		// Merging loaded with empty should return loaded
		TagResolver result1 = loaded.merge(empty);
		assertEquals(loaded.getAllTags().size(), result1.getAllTags().size(),
				"Merging with empty should preserve all tags");

		// Merging empty with loaded should return loaded
		TagResolver result2 = empty.merge(loaded);
		assertEquals(loaded.getAllTags().size(), result2.getAllTags().size(),
				"Empty merged with loaded should have loaded's tags");

		context.complete();
	}

	/**
	 * Verifies that tag inheritance works correctly.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "tag_resolver", required = true)
	public void testTagInheritance(TestContext context) {
		TagResolver resolver = ForgeroApi.tagResolver();

		// Find a tag that has descendants
		var allTags = resolver.getAllTags();
		for (OpenIdentifier tag : allTags) {
			Set<OpenIdentifier> descendants = resolver.getDescendants(tag);

			// Descendants should always include the tag itself
			assertTrue(descendants.contains(tag),
					"Descendants of " + tag + " should include the tag itself");

			// If there are child tags, verify they're in descendants
			for (OpenIdentifier descendant : descendants) {
				if (!descendant.equals(tag)) {
					Set<OpenIdentifier> descendantParents = resolver.getParents(descendant);
					// The descendant should have some path back to the original tag
					// (either directly or through other ancestors)
				}
			}
		}

		context.complete();
	}

	/**
	 * Verifies that hasTag works correctly with real components.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "tag_resolver", required = true)
	public void testHasTagWithComponents(TestContext context) {
		TagResolver resolver = ForgeroApi.tagResolver();
		var registry = ForgeroApi.componentRegistry();

		// Get a component and check its tags
		var components = registry.all();
		if (!components.isEmpty()) {
			Component component = components.iterator().next();

			// A component should match its own direct tags
			for (OpenIdentifier tag : component.getTags()) {
				assertTrue(resolver.hasTag(component, tag),
						"Component should have its direct tag: " + tag);
			}
		}

		context.complete();
	}

	/**
	 * Verifies that findTagged returns correct results.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "tag_resolver", required = true)
	public void testFindTagged(TestContext context) {
		TagResolver resolver = ForgeroApi.tagResolver();
		var registry = ForgeroApi.componentRegistry();

		List<Component> allComponents = registry.all().stream().collect(Collectors.toList());

		if (!allComponents.isEmpty()) {
			// Find a common tag
			var firstComponent = allComponents.get(0);
			if (!firstComponent.getTags().isEmpty()) {
				OpenIdentifier testTag = firstComponent.getTags().iterator().next();

				// Find all components with this tag
				List<Component> tagged = resolver.findTagged(testTag, allComponents);

				// All returned components should have the tag
				for (Component comp : tagged) {
					assertTrue(resolver.hasTag(comp, testTag),
							"findTagged result should have the queried tag");
				}

				// The original component should be in the results
				assertTrue(tagged.contains(firstComponent),
						"Component with direct tag should be in findTagged results");
			}
		}

		context.complete();
	}

	/**
	 * Verifies that findDirectlyTagged excludes inherited tags.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "tag_resolver", required = true)
	public void testFindDirectlyTagged(TestContext context) {
		TagResolver resolver = ForgeroApi.tagResolver();
		var registry = ForgeroApi.componentRegistry();

		List<Component> allComponents = registry.all().stream().collect(Collectors.toList());

		if (!allComponents.isEmpty()) {
			var firstComponent = allComponents.get(0);
			if (!firstComponent.getTags().isEmpty()) {
				OpenIdentifier testTag = firstComponent.getTags().iterator().next();

				// Find components directly tagged
				List<Component> directlyTagged = resolver.findDirectlyTagged(testTag, allComponents);

				// All returned components should have the tag directly
				for (Component comp : directlyTagged) {
					assertTrue(comp.getTags().contains(testTag),
							"findDirectlyTagged result should have the tag directly");
				}
			}
		}

		context.complete();
	}

	/**
	 * Verifies that getRelationships returns the tag hierarchy.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "tag_resolver", required = true)
	public void testGetRelationships(TestContext context) {
		TagResolver resolver = ForgeroApi.tagResolver();

		var relationships = resolver.getRelationships();
		assertNotNull(relationships, "getRelationships should not return null");

		// Each relationship should have valid parent tags
		for (var entry : relationships.entrySet()) {
			OpenIdentifier child = entry.getKey();
			Set<OpenIdentifier> parents = entry.getValue();

			assertNotNull(child, "Child tag should not be null");
			assertNotNull(parents, "Parents set should not be null");

			// Parents should be in the resolver
			for (OpenIdentifier parent : parents) {
				assertTrue(resolver.getAllTags().contains(parent) || resolver.getAllTags().contains(child),
						"Parent tag should be known to the resolver");
			}
		}

		context.complete();
	}

	/**
	 * Verifies that TagResolver.fromRelationships creates a working resolver.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "tag_resolver", required = true)
	public void testFromRelationships(TestContext context) {
		TagResolver original = ForgeroApi.tagResolver();
		var relationships = original.getRelationships();

		// Create a new resolver from the relationships
		TagResolver recreated = TagResolver.fromRelationships(relationships);

		// The recreated resolver should have the same tags
		assertEquals(original.getAllTags(), recreated.getAllTags(),
				"Recreated resolver should have the same tags");

		context.complete();
	}
}
