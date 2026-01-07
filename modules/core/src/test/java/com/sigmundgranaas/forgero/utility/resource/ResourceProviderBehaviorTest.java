package com.sigmundgranaas.forgero.utility.resource;

import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceFilter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.CompositeResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.CompositeResourceProvider.ConflictStrategy;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ProgrammaticResourceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Resource Provider Behavior")
class ResourceProviderBehaviorTest {

	@Nested
	@DisplayName("ProgrammaticResourceProvider")
	class ProgrammaticResourceProviderTests {

		private ProgrammaticResourceProvider provider;

		@BeforeEach
		void setUp() {
			provider = new ProgrammaticResourceProvider("test-provider", 100);
		}

		@Nested
		@DisplayName("Static Resource Registration")
		class StaticResourceRegistration {

			@Test
			@DisplayName("registers and reads static byte content")
			void registersAndReadsStaticByteContent() throws IOException {
				ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");
				byte[] content = "{\"type\": \"material\"}".getBytes(StandardCharsets.UTF_8);

				provider.register(path, content);

				Optional<InputStream> result = provider.read(path);
				assertTrue(result.isPresent());
				assertEquals("{\"type\": \"material\"}", new String(result.get().readAllBytes()));
			}

			@Test
			@DisplayName("registers and reads static string content")
			void registersAndReadsStaticStringContent() throws IOException {
				ResourcePath path = ResourcePath.file("forgero", "schematics", "sword_blade", "json");
				String content = "{\"name\": \"sword_blade\"}";

				provider.register(path, content);

				Optional<InputStream> result = provider.read(path);
				assertTrue(result.isPresent());
				assertEquals(content, new String(result.get().readAllBytes()));
			}

			@Test
			@DisplayName("registerJson convenience method works")
			void registerJsonConvenienceMethodWorks() throws IOException {
				provider.registerJson("forgero", "materials/gold", "{\"value\": 42}");

				ResourcePath path = ResourcePath.file("forgero", "materials", "gold", "json");
				Optional<InputStream> result = provider.read(path);

				assertTrue(result.isPresent());
				assertEquals("{\"value\": 42}", new String(result.get().readAllBytes()));
			}

			@Test
			@DisplayName("adds namespace when registering")
			void addsNamespaceWhenRegistering() {
				provider.registerJson("custom", "test/file", "{}");

				assertTrue(provider.getNamespaces().contains("custom"));
			}
		}

		@Nested
		@DisplayName("Dynamic Resource Registration")
		class DynamicResourceRegistration {

			@Test
			@DisplayName("dynamic supplier called on each read")
			void dynamicSupplierCalledOnEachRead() throws IOException {
				AtomicInteger counter = new AtomicInteger(0);
				ResourcePath path = ResourcePath.file("forgero", "generated", "stats", "json");

				provider.registerDynamic(path, () -> 
						("{\"count\": " + counter.incrementAndGet() + "}").getBytes(StandardCharsets.UTF_8));

				assertEquals("{\"count\": 1}", new String(provider.read(path).get().readAllBytes()));
				assertEquals("{\"count\": 2}", new String(provider.read(path).get().readAllBytes()));
				assertEquals("{\"count\": 3}", new String(provider.read(path).get().readAllBytes()));
			}

			@Test
			@DisplayName("dynamic string supplier works")
			void dynamicStringSupplierWorks() throws IOException {
				ResourcePath path = ResourcePath.file("forgero", "dynamic", "test", "json");

				provider.registerDynamicString(path, () -> "{\"dynamic\": true}");

				assertEquals("{\"dynamic\": true}", new String(provider.read(path).get().readAllBytes()));
			}

			@Test
			@DisplayName("returns empty on supplier exception")
			void returnsEmptyOnSupplierException() {
				ResourcePath path = ResourcePath.file("forgero", "error", "resource", "json");

				provider.registerDynamic(path, () -> {
					throw new RuntimeException("Generation failed");
				});

				Optional<InputStream> result = provider.read(path);
				assertTrue(result.isEmpty());
			}
		}

		@Nested
		@DisplayName("Resource Listing")
		class ResourceListing {

			@Test
			@DisplayName("lists resources in directory")
			void listsResourcesInDirectory() {
				provider.registerJson("forgero", "materials/iron", "{}");
				provider.registerJson("forgero", "materials/gold", "{}");
				provider.registerJson("forgero", "schematics/blade", "{}");

				List<ResourcePath> materials = provider
						.list(ResourcePath.directory("forgero", "materials"), false, ResourceFilter.JSON)
						.toList();

				assertEquals(2, materials.size());
			}

			@Test
			@DisplayName("lists resources recursively")
			void listsResourcesRecursively() {
				provider.registerJson("forgero", "a/b/file1", "{}");
				provider.registerJson("forgero", "a/file2", "{}");
				provider.registerJson("forgero", "other/file3", "{}");

				List<ResourcePath> results = provider
						.list(ResourcePath.directory("forgero", "a"), true, ResourceFilter.JSON)
						.toList();

				assertEquals(2, results.size());
			}

			@Test
			@DisplayName("filters by namespace")
			void filtersByNamespace() {
				provider.registerJson("forgero", "test/file1", "{}");
				provider.registerJson("minecraft", "test/file2", "{}");

				List<ResourcePath> forgeroOnly = provider
						.list(ResourcePath.directory("forgero", "test"), true, ResourceFilter.JSON)
						.toList();

				assertEquals(1, forgeroOnly.size());
				assertEquals("forgero", forgeroOnly.get(0).namespace());
			}
		}

		@Nested
		@DisplayName("Resource Management")
		class ResourceManagement {

			@Test
			@DisplayName("unregister removes resource")
			void unregisterRemovesResource() {
				ResourcePath path = ResourcePath.file("forgero", "test", "file", "json");
				provider.register(path, "content");

				assertTrue(provider.isRegistered(path));

				provider.unregister(path);

				assertFalse(provider.isRegistered(path));
				assertTrue(provider.read(path).isEmpty());
			}

			@Test
			@DisplayName("clear removes all resources")
			void clearRemovesAllResources() {
				provider.registerJson("forgero", "file1", "{}");
				provider.registerJson("forgero", "file2", "{}");

				assertEquals(2, provider.resourceCount());

				provider.clear();

				assertEquals(0, provider.resourceCount());
				assertTrue(provider.getNamespaces().isEmpty());
			}

			@Test
			@DisplayName("exists returns correct status")
			void existsReturnsCorrectStatus() {
				ResourcePath existing = ResourcePath.file("forgero", "exists", "file", "json");
				ResourcePath missing = ResourcePath.file("forgero", "missing", "file", "json");

				provider.register(existing, "content");

				assertTrue(provider.exists(existing));
				assertFalse(provider.exists(missing));
			}

			@Test
			@DisplayName("resourceCount includes both static and dynamic")
			void resourceCountIncludesBothStaticAndDynamic() {
				provider.registerJson("forgero", "static", "{}");
				provider.registerDynamicString(
						ResourcePath.file("forgero", "dynamic", "file", "json"),
						() -> "{}"
				);

				assertEquals(2, provider.resourceCount());
			}
		}

		@Nested
		@DisplayName("Provider Metadata")
		class ProviderMetadata {

			@Test
			@DisplayName("returns configured priority")
			void returnsConfiguredPriority() {
				ProgrammaticResourceProvider highPriority = new ProgrammaticResourceProvider("high", 200);
				ProgrammaticResourceProvider lowPriority = new ProgrammaticResourceProvider("low", 10);

				assertEquals(200, highPriority.priority());
				assertEquals(10, lowPriority.priority());
			}

			@Test
			@DisplayName("default priority is 100")
			void defaultPriorityIs100() {
				ProgrammaticResourceProvider defaultProvider = new ProgrammaticResourceProvider("default");

				assertEquals(100, defaultProvider.priority());
			}

			@Test
			@DisplayName("name includes provider name")
			void nameIncludesProviderName() {
				assertTrue(provider.name().contains("test-provider"));
			}
		}
	}

	@Nested
	@DisplayName("CompositeResourceProvider")
	class CompositeResourceProviderTests {

		@Nested
		@DisplayName("Provider Composition")
		class ProviderComposition {

			@Test
			@DisplayName("reads from highest priority provider first")
			void readsFromHighestPriorityProviderFirst() throws IOException {
				ProgrammaticResourceProvider high = new ProgrammaticResourceProvider("high", 100);
				ProgrammaticResourceProvider low = new ProgrammaticResourceProvider("low", 50);

				ResourcePath path = ResourcePath.file("forgero", "test", "file", "json");
				high.register(path, "high-content");
				low.register(path, "low-content");

				CompositeResourceProvider composite = CompositeResourceProvider.of(high, low);

				String content = new String(composite.read(path).get().readAllBytes());
				assertEquals("high-content", content);
			}

			@Test
			@DisplayName("falls back to lower priority if not found")
			void fallsBackToLowerPriorityIfNotFound() throws IOException {
				ProgrammaticResourceProvider high = new ProgrammaticResourceProvider("high", 100);
				ProgrammaticResourceProvider low = new ProgrammaticResourceProvider("low", 50);

				ResourcePath path = ResourcePath.file("forgero", "test", "file", "json");
				low.register(path, "low-content");

				CompositeResourceProvider composite = CompositeResourceProvider.of(high, low);

				String content = new String(composite.read(path).get().readAllBytes());
				assertEquals("low-content", content);
			}

			@Test
			@DisplayName("aggregates namespaces from all providers")
			void aggregatesNamespacesFromAllProviders() {
				ProgrammaticResourceProvider p1 = new ProgrammaticResourceProvider("p1");
				ProgrammaticResourceProvider p2 = new ProgrammaticResourceProvider("p2");

				p1.registerJson("namespace1", "test", "{}");
				p2.registerJson("namespace2", "test", "{}");

				CompositeResourceProvider composite = CompositeResourceProvider.of(p1, p2);

				assertTrue(composite.getNamespaces().contains("namespace1"));
				assertTrue(composite.getNamespaces().contains("namespace2"));
			}
		}

		@Nested
		@DisplayName("Conflict Strategies")
		class ConflictStrategies {

			@Test
			@DisplayName("FIRST_WINS deduplicates by highest priority")
			void firstWinsDeduplicatesByHighestPriority() {
				ProgrammaticResourceProvider high = new ProgrammaticResourceProvider("high", 100);
				ProgrammaticResourceProvider low = new ProgrammaticResourceProvider("low", 50);

				ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");
				high.register(path, "high");
				low.register(path, "low");

				CompositeResourceProvider composite = CompositeResourceProvider.of(
						ConflictStrategy.FIRST_WINS, high, low);

				List<ResourcePath> results = composite
						.list(ResourcePath.directory("forgero", "materials"), false, ResourceFilter.JSON)
						.toList();

				assertEquals(1, results.size());
			}

			@Test
			@DisplayName("LAST_WINS allows later providers to override")
			void lastWinsAllowsLaterProvidersToOverride() {
				ProgrammaticResourceProvider high = new ProgrammaticResourceProvider("high", 100);
				ProgrammaticResourceProvider low = new ProgrammaticResourceProvider("low", 50);

				ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");
				high.register(path, "high");
				low.register(path, "low");

				CompositeResourceProvider composite = CompositeResourceProvider.of(
						ConflictStrategy.LAST_WINS, high, low);

				List<ResourcePath> results = composite
						.list(ResourcePath.directory("forgero", "materials"), false, ResourceFilter.JSON)
						.toList();

				assertEquals(1, results.size());
			}

			@Test
			@DisplayName("INCLUDE_ALL returns all duplicates")
			void includeAllReturnsAllDuplicates() {
				ProgrammaticResourceProvider p1 = new ProgrammaticResourceProvider("p1", 100);
				ProgrammaticResourceProvider p2 = new ProgrammaticResourceProvider("p2", 50);

				ResourcePath path = ResourcePath.file("forgero", "materials", "iron", "json");
				p1.register(path, "p1");
				p2.register(path, "p2");

				CompositeResourceProvider composite = CompositeResourceProvider.of(
						ConflictStrategy.INCLUDE_ALL, p1, p2);

				List<ResourcePath> results = composite
						.list(ResourcePath.directory("forgero", "materials"), false, ResourceFilter.JSON)
						.toList();

				assertEquals(2, results.size());
			}
		}

		@Nested
		@DisplayName("Builder Pattern")
		class BuilderPattern {

			@Test
			@DisplayName("builder creates composite with default strategy")
			void builderCreatesCompositeWithDefaultStrategy() {
				ProgrammaticResourceProvider p = new ProgrammaticResourceProvider("test");
				p.registerJson("forgero", "test", "{}");

				CompositeResourceProvider composite = CompositeResourceProvider.builder()
						.add(p)
						.build();

				assertEquals(ConflictStrategy.LAST_WINS, composite.getConflictStrategy());
			}

			@Test
			@DisplayName("builder allows custom strategy")
			void builderAllowsCustomStrategy() {
				ProgrammaticResourceProvider p = new ProgrammaticResourceProvider("test");
				p.registerJson("forgero", "test", "{}");

				CompositeResourceProvider composite = CompositeResourceProvider.builder()
						.add(p)
						.withConflictStrategy(ConflictStrategy.FIRST_WINS)
						.build();

				assertEquals(ConflictStrategy.FIRST_WINS, composite.getConflictStrategy());
			}

			@Test
			@DisplayName("builder throws on empty providers")
			void builderThrowsOnEmptyProviders() {
				assertThrows(IllegalStateException.class, () -> {
					CompositeResourceProvider.builder().build();
				});
			}

			@Test
			@DisplayName("addAll adds multiple providers")
			void addAllAddsMultipleProviders() {
				ProgrammaticResourceProvider p1 = new ProgrammaticResourceProvider("p1");
				ProgrammaticResourceProvider p2 = new ProgrammaticResourceProvider("p2");

				CompositeResourceProvider composite = CompositeResourceProvider.builder()
						.addAll(List.of(p1, p2))
						.build();

				assertEquals(2, composite.getProviders().size());
			}
		}

		@Nested
		@DisplayName("Existence Checking")
		class ExistenceChecking {

			@Test
			@DisplayName("exists returns true if any provider has resource")
			void existsReturnsTrueIfAnyProviderHasResource() {
				ProgrammaticResourceProvider p1 = new ProgrammaticResourceProvider("p1");
				ProgrammaticResourceProvider p2 = new ProgrammaticResourceProvider("p2");

				ResourcePath path = ResourcePath.file("forgero", "test", "file", "json");
				p2.register(path, "content");

				CompositeResourceProvider composite = CompositeResourceProvider.of(p1, p2);

				assertTrue(composite.exists(path));
			}

			@Test
			@DisplayName("exists returns false if no provider has resource")
			void existsReturnsFalseIfNoProviderHasResource() {
				ProgrammaticResourceProvider p1 = new ProgrammaticResourceProvider("p1");
				ProgrammaticResourceProvider p2 = new ProgrammaticResourceProvider("p2");

				ResourcePath path = ResourcePath.file("forgero", "missing", "file", "json");

				CompositeResourceProvider composite = CompositeResourceProvider.of(p1, p2);

				assertFalse(composite.exists(path));
			}
		}

		@Nested
		@DisplayName("Priority Handling")
		class PriorityHandling {

			@Test
			@DisplayName("returns highest provider priority")
			void returnsHighestProviderPriority() {
				ProgrammaticResourceProvider p1 = new ProgrammaticResourceProvider("p1", 50);
				ProgrammaticResourceProvider p2 = new ProgrammaticResourceProvider("p2", 150);

				CompositeResourceProvider composite = CompositeResourceProvider.of(p1, p2);

				assertEquals(150, composite.priority());
			}

			@Test
			@DisplayName("providers sorted by priority descending")
			void providersSortedByPriorityDescending() {
				ProgrammaticResourceProvider low = new ProgrammaticResourceProvider("low", 10);
				ProgrammaticResourceProvider high = new ProgrammaticResourceProvider("high", 100);
				ProgrammaticResourceProvider mid = new ProgrammaticResourceProvider("mid", 50);

				CompositeResourceProvider composite = CompositeResourceProvider.of(low, high, mid);

				List<Integer> priorities = composite.getProviders().stream()
						.map(p -> p.priority())
						.toList();

				assertEquals(List.of(100, 50, 10), priorities);
			}
		}
	}
}
