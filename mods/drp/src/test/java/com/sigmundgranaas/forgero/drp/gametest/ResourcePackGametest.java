package com.sigmundgranaas.forgero.drp.gametest;

import com.sigmundgranaas.forgero.drp.api.DRPApi;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.testutil.DRPTestInitializer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * GameTests for verifying DRP resource pack integration.
 */
public class ResourcePackGametest {

	private static final String TEST_NAMESPACE = DRPTestInitializer.TEST_NAMESPACE;

	/**
	 * Verifies that the DRP API is initialized and accessible.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_integration")
	public void testDRPApiInitialized(TestContext context) {
		DRPApi api = DRPApi.getInstance();
		context.assertTrue(api != null, "DRPApi should be initialized");

		context.complete();
	}

	/**
	 * Verifies that test resources were initialized.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_integration")
	public void testTestResourcesInitialized(TestContext context) {
		context.assertTrue(DRPTestInitializer.isInitialized(), "Test resources should be initialized");

		DynamicResourcePack testPack = DRPTestInitializer.getTestPack();
		context.assertTrue(testPack != null, "Test pack should exist");

		context.complete();
	}

	/**
	 * Verifies that registered packs are tracked by the API.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_integration")
	public void testPacksRegistered(TestContext context) {
		DRPApi api = DRPApi.getInstance();
		List<DynamicResourcePack> packs = api.getRegisteredPacks();

		context.assertTrue(!packs.isEmpty(), "At least one pack should be registered");

		boolean hasTestPack = packs.stream()
				.anyMatch(p -> p.getId().getNamespace().equals(TEST_NAMESPACE));
		context.assertTrue(hasTestPack, "Test pack should be in registered packs");

		context.complete();
	}

	/**
	 * Verifies that the test namespace is available in the server.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_integration")
	public void testNamespaceAvailable(TestContext context) {
		ResourceManager resourceManager = context.getWorld().getServer().getResourceManager();

		// Check if our namespace exists in the resource manager
		boolean namespaceExists = resourceManager.getAllNamespaces().contains(TEST_NAMESPACE);
		context.assertTrue(namespaceExists, "Test namespace should be available in resource manager");

		context.complete();
	}

	/**
	 * Verifies that generated tag files are accessible.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_integration")
	public void testTagResourceAccessible(TestContext context) {
		ResourceManager resourceManager = context.getWorld().getServer().getResourceManager();
		Identifier tagId = new Identifier(TEST_NAMESPACE, "tags/items/test_items.json");

		Optional<Resource> resource = resourceManager.getResource(tagId);
		context.assertTrue(resource.isPresent(), "Tag resource should be accessible: " + tagId);

		context.complete();
	}

	/**
	 * Verifies that generated recipe files are accessible.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_integration")
	public void testRecipeResourceAccessible(TestContext context) {
		ResourceManager resourceManager = context.getWorld().getServer().getResourceManager();
		Identifier recipeId = new Identifier(TEST_NAMESPACE, "recipes/test_shapeless.json");

		Optional<Resource> resource = resourceManager.getResource(recipeId);
		context.assertTrue(resource.isPresent(), "Recipe resource should be accessible: " + recipeId);

		context.complete();
	}

	/**
	 * Verifies that multiple resource types can coexist.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_integration")
	public void testMultipleResourceTypes(TestContext context) {
		ResourceManager resourceManager = context.getWorld().getServer().getResourceManager();

		// Check tags
		Optional<Resource> tagResource = resourceManager.getResource(
				new Identifier(TEST_NAMESPACE, "tags/items/test_items.json"));

		// Check recipes
		Optional<Resource> recipeResource = resourceManager.getResource(
				new Identifier(TEST_NAMESPACE, "recipes/test_shapeless.json"));

		context.assertTrue(tagResource.isPresent(), "Tag resource should exist");
		context.assertTrue(recipeResource.isPresent(), "Recipe resource should exist");

		context.complete();
	}

	/**
	 * Verifies that DRP packs have correct metadata.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "drp_integration")
	public void testPackMetadata(TestContext context) {
		DynamicResourcePack testPack = DRPTestInitializer.getTestPack();

		context.assertTrue(testPack != null, "Test pack should exist");
		context.assertTrue(testPack.getId() != null, "Pack should have an ID");
		context.assertTrue(testPack.getId().getNamespace().equals(TEST_NAMESPACE),
				"Pack namespace should be " + TEST_NAMESPACE);

		context.complete();
	}
}
