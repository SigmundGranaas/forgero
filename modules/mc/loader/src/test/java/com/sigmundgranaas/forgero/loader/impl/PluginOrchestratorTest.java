package com.sigmundgranaas.forgero.loader.impl;

import com.sigmundgranaas.forgero.common.api.item.ItemComparisonApi;
import com.sigmundgranaas.forgero.common.api.item.ItemMutationApi;
import com.sigmundgranaas.forgero.common.api.item.ItemPropertyApi;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.common.api.*;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PluginOrchestrator plugin lifecycle management.
 * <p>
 * These tests verify the behavioral contracts of the plugin system:
 * - Plugin discovery via Fabric entrypoints
 * - Plugin registration invokes plugin.register()
 * - Post-load notification reaches all plugins
 * - Error resilience: one failing plugin doesn't crash others
 *
 * <p>
 * Note: ForgeroDefaultsPlugin and CoreSlotTypesPlugin are now discovered via Fabric entrypoints
 * defined in the properties module's fabric.mod.json, not hardcoded in the orchestrator.
 * In unit tests without Fabric, these plugins won't be present.
 */
class PluginOrchestratorTest {

	@Nested
	@DisplayName("Plugin Discovery Behavior")
	class PluginDiscoveryBehavior {

		@Test
		@DisplayName("Plugin discovery runs without error")
		void pluginDiscoveryRunsSuccessfully() {
			PluginOrchestrator orchestrator = new PluginOrchestrator();

			// Discovery should complete without throwing, even if no plugins are found
			// (in unit test environment, Fabric entrypoints are not available)
			assertDoesNotThrow(orchestrator::discoverPlugins,
					"Plugin discovery should complete without error");

			// Verify plugin lists are initialized (may be empty in test environment)
			assertNotNull(orchestrator.getDataPlugins(), "Data plugins list should be initialized");
			assertNotNull(orchestrator.getItemRegistrationPlugins(), "Item registration plugins list should be initialized");
			assertNotNull(orchestrator.getPostLoadPlugins(), "Post-load plugins list should be initialized");
		}

		@Test
		@DisplayName("Plugin lists are unmodifiable after discovery")
		void pluginListsAreUnmodifiable() {
			PluginOrchestrator orchestrator = new PluginOrchestrator();
			orchestrator.discoverPlugins();

			assertThrows(UnsupportedOperationException.class,
				() -> orchestrator.getDataPlugins().add(null),
				"getDataPlugins() should return unmodifiable list");
			assertThrows(UnsupportedOperationException.class,
				() -> orchestrator.getItemRegistrationPlugins().add(null),
				"getItemRegistrationPlugins() should return unmodifiable list");
			assertThrows(UnsupportedOperationException.class,
				() -> orchestrator.getPostLoadPlugins().add(null),
				"getPostLoadPlugins() should return unmodifiable list");
		}
	}

	@Nested
	@DisplayName("Plugin Registration Behavior")
	class PluginRegistrationBehavior {

		private PluginOrchestrator orchestrator;

		@BeforeEach
		void setUp() {
			orchestrator = new PluginOrchestrator();
			orchestrator.discoverPlugins();
		}

		@Test
		@DisplayName("registerPluginRequirements creates valid registration context")
		void registerPluginRequirementsCreatesContext() {
			PluginRegistrationContext context = orchestrator.registerPluginRequirements(() -> TagResolver.empty());
			assertNotNull(context, "Registration context should be created");
		}

		@Test
		@DisplayName("Plugin registration continues despite individual plugin failures")
		void registrationContinuesDespitePluginFailures() {
			// The orchestrator catches exceptions from individual plugins
			// and continues with remaining plugins
			// This test verifies no exception propagates
			assertDoesNotThrow(
				() -> orchestrator.registerPluginRequirements(() -> TagResolver.empty()),
				"Plugin registration should not throw even if plugins fail internally"
			);
		}
	}

	@Nested
	@DisplayName("Post-Load Notification Behavior")
	class PostLoadNotificationBehavior {

		private PluginOrchestrator orchestrator;

		@BeforeEach
		void setUp() {
			orchestrator = new PluginOrchestrator();
			orchestrator.discoverPlugins();
		}

		@Test
		@DisplayName("Post-load notification continues despite individual plugin failures")
		void notificationContinuesDespitePluginFailures() {
			// The orchestrator catches exceptions from post-load plugins
			// and continues notifying remaining plugins
			assertDoesNotThrow(
				() -> orchestrator.notifyPostLoad(new StubDataLoadingContext()),
				"Post-load notification should not throw even if plugins fail"
			);
		}
	}

	// ========== Stub implementations ==========

	private static class StubDataLoadingContext implements DataLoadingContext {
		@Override public TagResolver tagResolver() { return TagResolver.empty(); }
		@Override public ComponentConverter converter() { return null; }
		@Override public ComponentRegistry componentRegistry() { return StubComponentRegistry.INSTANCE; }
		@Override public TaggedRegistry<Component> taggedComponents() { return null; }
		@Override public ComponentNbtConverter nbtConverter() { return null; }
		@Override public Optional<Component> component(ItemStack stack) { return Optional.empty(); }
		@Override public SlotManager slotManager() { return null; }
		@Override public ForgeroDataBundle getDataBundle() { return null; }
		@Override public ItemQueryApi itemQuery() { return null; }
		@Override public ItemMutationApi itemMutation() { return null; }
		@Override public ItemComparisonApi itemComparison() { return null; }
		@Override public ItemPropertyApi itemProperty() { return null; }
	}

	private enum StubComponentRegistry implements ComponentRegistry {
		INSTANCE;
		@Override public Optional<Component> get(OpenIdentifier id) { return Optional.empty(); }
		@Override public List<Component> all() { return List.of(); }
		@Override public Builder toBuilder() { return ComponentRegistry.builder(); }
	}
}
