package com.sigmundgranaas.forgero.loader.gametest;

import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.loader.api.ForgeroServices;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ForgeroInitializedCallback event system.
 * <p>
 * Note: These tests verify that the event system works correctly.
 * The actual event firing happens during mod initialization, before these tests run.
 * We verify the event infrastructure is in place and that services received are valid.
 */
public class ForgeroInitializedCallbackTest {

	/**
	 * Helper to get services using the static accessor.
	 */
	private static ForgeroServices getServices() {
		return ForgeroInitializedCallback.getServices().orElse(null);
	}

	/**
	 * Verifies that the ForgeroInitializedCallback event was fired during initialization.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_events", required = true)
	public void testEventWasFired(TestContext context) {
		assertTrue(ForgeroInitializedCallback.isInitialized(),
				"ForgeroInitializedCallback should have been fired during mod initialization");
		context.complete();
	}

	/**
	 * Verifies that the event provided valid services.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_events", required = true)
	public void testEventProvidedValidServices(TestContext context) {
		ForgeroServices services = getServices();
		assertNotNull(services, "Event should have provided a ForgeroServices instance");

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
	 * Verifies that the event can be used for dependency injection patterns.
	 */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "forgero_events", required = true)
	public void testEventEnablesDependencyInjection(TestContext context) {
		// Simulate a service that receives its dependencies via the event
		ForgeroServices services = getServices();
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
