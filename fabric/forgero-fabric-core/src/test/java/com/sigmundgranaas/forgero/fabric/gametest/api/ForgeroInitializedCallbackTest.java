package com.sigmundgranaas.forgero.fabric.gametest.api;

import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ForgeroInitializedCallback event system.
 * <p>
 * Note: The ForgeroInitializedCallback event fires during mod initialization,
 * before these test classes are loaded. Therefore, we cannot capture the event
 * directly in these tests. Instead, we verify:
 * 1. The event infrastructure is properly set up
 * 2. Services are available (proving initialization completed successfully)
 * 3. New listeners can be registered for future use
 */
public class ForgeroInitializedCallbackTest {

	/**
	 * Verifies that the ForgeroInitializedCallback event infrastructure exists.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_events", required = true)
	public void testEventInfrastructureExists(TestContext context) {
		// The EVENT field should be non-null and properly initialized
		assertNotNull(ForgeroInitializedCallback.EVENT,
				"ForgeroInitializedCallback.EVENT should be initialized");
		assertNotNull(ForgeroInitializedCallback.EVENT.invoker(),
				"EVENT.invoker() should return a valid invoker");

		context.complete();
	}

	/**
	 * Verifies that services are available, proving the initialization event was processed.
	 * Since services are set up during the event, their availability proves the event fired.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_events", required = true)
	public void testServicesAvailableAfterInitialization(TestContext context) {
		// Get services via ForgeroApi - if these work, initialization completed
		ForgeroServices services = ForgeroApi.services();
		assertNotNull(services, "ForgeroServices should be available after initialization");

		// Verify all service methods return non-null
		assertNotNull(services.tagResolver(), "tagResolver() should not be null");
		assertNotNull(services.converter(), "converter() should not be null");
		assertNotNull(services.resolver(), "resolver() should not be null");
		assertNotNull(services.componentRegistry(), "componentRegistry() should not be null");
		assertNotNull(services.taggedComponents(), "taggedComponents() should not be null");
		assertNotNull(services.nbtConverter(), "nbtConverter() should not be null");

		context.complete();
	}

	/**
	 * Verifies that new event listeners can be registered.
	 * While they won't receive past events, they can be used for re-initialization scenarios.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_events", required = true)
	public void testCanRegisterNewListeners(TestContext context) {
		AtomicBoolean listenerRegistered = new AtomicBoolean(false);

		// Register a new listener - should not throw
		ForgeroInitializedCallback.EVENT.register(services -> {
			listenerRegistered.set(true);
		});

		// The listener is registered (though it won't fire since init already happened)
		// This proves the event system accepts new registrations
		assertNotNull(ForgeroInitializedCallback.EVENT.invoker(),
				"Event should still have a valid invoker after registration");

		context.complete();
	}

	/**
	 * Verifies that the event enables dependency injection patterns.
	 * Tests that services obtained can be used to create dependent services.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_events", required = true)
	public void testEventEnablesDependencyInjection(TestContext context) {
		// Get services via ForgeroApi
		ForgeroServices services = ForgeroApi.services();
		assertNotNull(services, "Services should be available for DI");

		// Create a mock service that depends on ForgeroServices
		MockDependentService mockService = new MockDependentService(services);
		assertTrue(mockService.canQueryTags(), "Dependent service should be able to use injected services");

		context.complete();
	}

	/**
	 * Mock service class to demonstrate dependency injection pattern.
	 */
	private static class MockDependentService {
		private final ForgeroServices services;

		public MockDependentService(ForgeroServices services) {
			this.services = services;
		}

		public boolean canQueryTags() {
			return services.tagResolver() != null && !services.tagResolver().getAllTags().isEmpty();
		}
	}
}
