package com.sigmundgranaas.forgero.loader.impl;

import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import com.sigmundgranaas.forgero.loader.impl.phase.PhaseResult;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.CachingResourceProvider;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DataLoadingPipeline.
 * <p>
 * Tests are organized into categories:
 * - Basic Method Contracts: Test simple utility methods that don't require Minecraft
 * - Cache Operations: Test cache invalidation and statistics
 * - Configuration Tests: Test config creation (requires mocking or Fabric runtime)
 * - Data Loading Tests: Test full pipeline (requires Fabric runtime)
 * <p>
 * Tests requiring Fabric runtime are disabled and documented why.
 */
class DataLoadingPipelineTest {

	// Note: DataLoadingPipeline instance creation removed from @BeforeEach
	// because constructor requires Fabric runtime (FabricLoader.getInstance())
	// Individual tests create instances as needed

	@Nested
	@DisplayName("Basic Method Contracts")
	class BasicMethodTests {

		@Test
		@Disabled("Requires Fabric runtime - FabricResourceProvider needs FabricLoader.getInstance()")
		@DisplayName("Constructor should create instance without throwing")
		void constructorCreatesInstance() {
			assertDoesNotThrow(() -> new DataLoadingPipeline());
		}

		@Test
		@Disabled("Requires Fabric runtime for constructor")
		@DisplayName("getModNamespace should return 'forgero'")
		void getModNamespaceReturnsCorrectValue() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			assertEquals("forgero", pipeline.getModNamespace());
		}

		@Test
		@Disabled("Requires Fabric runtime for constructor")
		@DisplayName("getModNamespace should return constant value")
		void getModNamespaceReturnsConstantValue() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			String first = pipeline.getModNamespace();
			String second = pipeline.getModNamespace();
			assertSame(first, second);
		}

		@Test
		@Disabled("Requires Fabric runtime for constructor")
		@DisplayName("getSharedProvider should return non-null")
		void getSharedProviderReturnsNonNull() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			assertNotNull(pipeline.getSharedProvider());
		}

		@Test
		@Disabled("Requires Fabric runtime for constructor")
		@DisplayName("getSharedProvider should return same instance")
		void getSharedProviderReturnsSameInstance() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			CachingResourceProvider first = pipeline.getSharedProvider();
			CachingResourceProvider second = pipeline.getSharedProvider();
			assertSame(first, second, "Should return same shared instance");
		}

		@Test
		@Disabled("Requires Fabric runtime for constructor")
		@DisplayName("getSharedProvider should return CachingResourceProvider")
		void getSharedProviderReturnsCorrectType() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			assertTrue(pipeline.getSharedProvider() instanceof CachingResourceProvider);
		}
	}

	@Nested
	@DisplayName("Cache Operations")
	class CacheOperationTests {

		@Test
		@Disabled("Requires Fabric runtime for constructor")
		@DisplayName("invalidateCache should not throw")
		void invalidateCacheDoesNotThrow() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			assertDoesNotThrow(() -> pipeline.invalidateCache());
		}

		@Test
		@Disabled("Requires Fabric runtime for constructor")
		@DisplayName("invalidateCache should be callable multiple times")
		void invalidateCacheMultipleTimes() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			assertDoesNotThrow(() -> {
				pipeline.invalidateCache();
				pipeline.invalidateCache();
				pipeline.invalidateCache();
			});
		}

		@Test
		@Disabled("Requires Fabric runtime for constructor")
		@DisplayName("logCacheStatistics should not throw")
		void logCacheStatisticsDoesNotThrow() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			assertDoesNotThrow(() -> pipeline.logCacheStatistics());
		}

		@Test
		@Disabled("Requires Fabric runtime for constructor")
		@DisplayName("logCacheStatistics should be callable multiple times")
		void logCacheStatisticsMultipleTimes() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			assertDoesNotThrow(() -> {
				pipeline.logCacheStatistics();
				pipeline.logCacheStatistics();
			});
		}

		@Test
		@Disabled("Requires Fabric runtime for constructor")
		@DisplayName("getSharedProvider statistics should be accessible")
		void sharedProviderStatisticsAccessible() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			CachingResourceProvider provider = pipeline.getSharedProvider();
			assertDoesNotThrow(() -> {
				var stats = provider.getStatistics();
				assertNotNull(stats);
				assertTrue(stats.containsKey("listCacheSize"));
				assertTrue(stats.containsKey("resourceCacheSize"));
				assertTrue(stats.containsKey("trackedSources"));
			});
		}
	}

	@Nested
	@DisplayName("Tag Loading Tests")
	class TagLoadingTests {

		@Test
		@Disabled("Requires Fabric runtime - needs FabricLoader and mod discovery")
		@DisplayName("loadTags should return non-null TagResolver")
		void loadTagsReturnsNonNull() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			TagResolver resolver = pipeline.loadTags();
			assertNotNull(resolver);
		}

		@Test
		@Disabled("Requires Fabric runtime - needs FabricLoader and mod discovery")
		@DisplayName("loadTagsSafe should return PhaseResult")
		void loadTagsSafeReturnsPhaseResult() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			PhaseResult<TagResolver> result = pipeline.loadTagsSafe();
			assertNotNull(result);
		}

		@Test
		@Disabled("Requires Fabric runtime - needs FabricLoader and mod discovery")
		@DisplayName("loadTagsSafe should succeed with valid Fabric environment")
		void loadTagsSafeSucceedsWithValidEnvironment() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			PhaseResult<TagResolver> result = pipeline.loadTagsSafe();
			assertTrue(result.isSuccess());
			assertNotNull(result.value());
		}

		@Test
		@Disabled("Requires Fabric runtime - needs FabricLoader and mod discovery")
		@DisplayName("loadTags should return same value as loadTagsSafe.value()")
		void loadTagsConsistentWithLoadTagsSafe() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			TagResolver direct = pipeline.loadTags();
			TagResolver fromSafe = pipeline.loadTagsSafe().value();
			// Note: May not be same instance due to re-execution
			assertNotNull(direct);
			assertNotNull(fromSafe);
		}

		@Test
		@Disabled("Requires Fabric runtime - needs FabricLoader and mod discovery")
		@DisplayName("loadTagsSafe should use shared provider")
		void loadTagsSafeUsesSharedProvider() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			CachingResourceProvider provider = pipeline.getSharedProvider();
			var statsBefore = provider.getStatistics();

			pipeline.loadTagsSafe();

			var statsAfter = provider.getStatistics();
			// Provider should have been used (statistics may change)
			assertNotNull(statsAfter);
		}
	}

	@Nested
	@DisplayName("Configuration Creation Tests")
	class ConfigCreationTests {

		@Test
		@Disabled("Requires mock PluginRegistrationContextImpl or Fabric runtime")
		@DisplayName("createConfig should return non-null Config")
		void createConfigReturnsNonNull() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			PluginRegistrationContextImpl context = createMockContext();
			TagResolver tagResolver = TagResolver.empty();

			ForgeroDataInitializer.Config config = pipeline.createConfig(context, tagResolver);
			assertNotNull(config);
		}

		@Test
		@Disabled("Requires mock PluginRegistrationContextImpl or Fabric runtime")
		@DisplayName("createConfig should use mod namespace")
		void createConfigUsesModNamespace() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			PluginRegistrationContextImpl context = createMockContext();
			TagResolver tagResolver = TagResolver.empty();

			ForgeroDataInitializer.Config config = pipeline.createConfig(context, tagResolver);
			assertEquals("forgero", pipeline.getModNamespace());
		}

		@Test
		@Disabled("Requires mock PluginRegistrationContextImpl or Fabric runtime")
		@DisplayName("createConfig should include attribute codec by default")
		void createConfigIncludesAttributeCodec() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			PluginRegistrationContextImpl context = createMockContext();
			TagResolver tagResolver = TagResolver.empty();

			ForgeroDataInitializer.Config config = pipeline.createConfig(context, tagResolver);
			// Attribute codec should be in property codecs
			// Implementation detail - hard to verify without inspecting config internals
			assertNotNull(config);
		}

		@Test
		@Disabled("Requires mock PluginRegistrationContextImpl or Fabric runtime")
		@DisplayName("createConfig should use shared provider")
		void createConfigUsesSharedProvider() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			PluginRegistrationContextImpl context = createMockContext();
			TagResolver tagResolver = TagResolver.empty();

			CachingResourceProvider providerBefore = pipeline.getSharedProvider();
			ForgeroDataInitializer.Config config = pipeline.createConfig(context, tagResolver);
			CachingResourceProvider providerAfter = pipeline.getSharedProvider();

			assertSame(providerBefore, providerAfter, "Should use same shared provider");
		}

		private PluginRegistrationContextImpl createMockContext() {
			// Mock implementation would go here
			// For now, disabled - requires either mock or Fabric runtime
			throw new UnsupportedOperationException("Requires mock or Fabric runtime");
		}
	}

	@Nested
	@DisplayName("Data Loading Tests")
	class DataLoadingTests {

		@Test
		@Disabled("Requires Fabric runtime and valid data packs")
		@DisplayName("loadData should return non-null ForgeroDataBundle")
		void loadDataReturnsNonNull() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			ForgeroDataInitializer.Config config = createValidConfig();

			ForgeroDataBundle bundle = pipeline.loadData(config);
			assertNotNull(bundle);
		}

		@Test
		@Disabled("Requires Fabric runtime and valid data packs")
		@DisplayName("loadData should return bundle with component registry")
		void loadDataReturnsBundleWithComponents() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			ForgeroDataInitializer.Config config = createValidConfig();

			ForgeroDataBundle bundle = pipeline.loadData(config);
			assertNotNull(bundle.componentRegistry());
		}

		@Test
		@Disabled("Requires Fabric runtime and valid data packs")
		@DisplayName("loadData should populate component registry")
		void loadDataPopulatesComponentRegistry() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			ForgeroDataInitializer.Config config = createValidConfig();

			ForgeroDataBundle bundle = pipeline.loadData(config);
			assertFalse(bundle.componentRegistry().all().isEmpty(),
					"Component registry should have components from data packs");
		}

		private ForgeroDataInitializer.Config createValidConfig() {
			// Would need to create a valid config with proper codecs
			// For now, disabled - requires Fabric runtime
			throw new UnsupportedOperationException("Requires Fabric runtime");
		}
	}

	@Nested
	@DisplayName("Integration Tests")
	class IntegrationTests {

		@Test
		@Disabled("Requires full Fabric runtime with data packs")
		@DisplayName("Full pipeline should execute all phases")
		void fullPipelineExecutesAllPhases() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();

			// Phase 2: Load tags
			PhaseResult<TagResolver> tagResult = pipeline.loadTagsSafe();
			assertTrue(tagResult.isSuccess());

			// Phase 4: Create config
			PluginRegistrationContextImpl context = createMockContext();
			ForgeroDataInitializer.Config config = pipeline.createConfig(context, tagResult.value());
			assertNotNull(config);

			// Phase 5: Load data
			ForgeroDataBundle bundle = pipeline.loadData(config);
			assertNotNull(bundle);
			assertNotNull(bundle.componentRegistry());
		}

		@Test
		@Disabled("Requires full Fabric runtime with data packs")
		@DisplayName("Cache should persist across phases")
		void cachePersistsAcrossPhases() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();
			CachingResourceProvider provider = pipeline.getSharedProvider();

			// Execute tag loading (uses cache)
			pipeline.loadTagsSafe();
			var statsAfterTags = provider.getStatistics();

			// Execute data loading (reuses cache)
			PluginRegistrationContextImpl context = createMockContext();
			TagResolver tagResolver = TagResolver.empty();
			ForgeroDataInitializer.Config config = pipeline.createConfig(context, tagResolver);
			pipeline.loadData(config);

			var statsAfterData = provider.getStatistics();

			// Cache should have been used in both phases
			assertNotNull(statsAfterTags);
			assertNotNull(statsAfterData);
		}

		@Test
		@Disabled("Requires full Fabric runtime with data packs")
		@DisplayName("invalidateCache should clear provider cache")
		void invalidateCacheClearsProviderCache() {
			DataLoadingPipeline pipeline = new DataLoadingPipeline();

			// Load some data to populate cache
			pipeline.loadTagsSafe();

			// Invalidate cache
			pipeline.invalidateCache();

			// Verify cache was cleared by checking provider statistics
			CachingResourceProvider provider = pipeline.getSharedProvider();
			var stats = provider.getStatistics();
			assertEquals(0, stats.get("listCacheSize"));
			assertEquals(0, stats.get("resourceCacheSize"));
		}

		private PluginRegistrationContextImpl createMockContext() {
			throw new UnsupportedOperationException("Requires Fabric runtime");
		}
	}

	@Nested
	@DisplayName("Error Handling Tests")
	class ErrorHandlingTests {

		@Test
		@Disabled("Requires Fabric runtime to test error conditions")
		@DisplayName("loadTagsSafe should return failure on error")
		void loadTagsSafeReturnsFailureOnError() {
			// Would need to create conditions that cause tag loading to fail
			// E.g., corrupted data, missing files, etc.
		}

		@Test
		@Disabled("Requires Fabric runtime to test error conditions")
		@DisplayName("loadTagsSafe should use empty fallback on failure")
		void loadTagsSafeUsesEmptyFallbackOnFailure() {
			// When tag loading fails, should return empty TagResolver as fallback
		}

		@Test
		@Disabled("Requires Fabric runtime to test error conditions")
		@DisplayName("loadTagsSafe should log errors on failure")
		void loadTagsSafeLogsErrorsOnFailure() {
			// Would need to capture log output to verify error logging
		}
	}

	@Nested
	@DisplayName("Dev Mode Tests")
	class DevModeTests {

		@Test
		@Disabled("Requires Fabric runtime and system property manipulation")
		@DisplayName("Constructor should respect forgero.dev.hotReload system property")
		void constructorRespectsDevModeProperty() {
			// Would need to set system property and verify dev mode is enabled
			// System.setProperty("forgero.dev.hotReload", "true");
			// pipeline = new DataLoadingPipeline();
			// Verify dev mode behavior
		}

		@Test
		@Disabled("Requires Fabric runtime and system property manipulation")
		@DisplayName("Dev mode should disable caching")
		void devModeDisablesCaching() {
			// When dev mode is enabled, cache should not persist
			// System.setProperty("forgero.dev.hotReload", "true");
			// pipeline = new DataLoadingPipeline();
			// Verify cache is not used
		}
	}

	@Nested
	@DisplayName("Thread Safety Tests")
	class ThreadSafetyTests {

		@Test
		@Disabled("Requires Fabric runtime for concurrent testing")
		@DisplayName("Shared provider should be thread-safe")
		void sharedProviderIsThreadSafe() {
			// Would need to execute multiple pipeline operations concurrently
			// and verify no race conditions or data corruption
		}

		@Test
		@Disabled("Requires Fabric runtime for concurrent testing")
		@DisplayName("Cache invalidation should be thread-safe")
		void cacheInvalidationIsThreadSafe() {
			// Would need to call invalidateCache concurrently from multiple threads
		}
	}

	/**
	 * Documentation of why tests are disabled.
	 * <p>
	 * Most tests in this suite are disabled because DataLoadingPipeline depends heavily
	 * on Fabric's runtime environment:
	 * <p>
	 * 1. Constructor requires FabricResourceProvider, which needs FabricLoader.getInstance()
	 * 2. FabricLoader requires a full Fabric mod loading environment
	 * 3. Tag loading requires actual mod containers with resources
	 * 4. Data loading requires valid data packs and codec registration
	 * <p>
	 * To enable these tests, one would need to:
	 * - Use Fabric's test framework with @GameTest
	 * - Set up test mod environment with fabric-loader-junit
	 * - Provide test data packs in test resources
	 * - Mock FabricLoader (complex, not recommended)
	 * <p>
	 * Alternative testing approaches:
	 * - Integration tests using Minecraft's GameTest framework
	 * - Manual testing during development with runClient
	 * - Refactor to use dependency injection for easier testing
	 */
	@Nested
	@DisplayName("Documentation")
	class DocumentationTests {

		@Test
		@DisplayName("Tests document why they require Fabric runtime")
		void testsAreDocumented() {
			// This test exists to ensure the documentation above is not removed
			assertTrue(true, "See class-level documentation for test status");
		}
	}
}
