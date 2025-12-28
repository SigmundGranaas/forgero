package com.sigmundgranaas.forgero.recipegen.unit;

import com.sigmundgranaas.forgero.recipegen.api.operation.OperationFactory;
import com.sigmundgranaas.forgero.recipegen.api.operation.OperationRegistry;
import com.sigmundgranaas.forgero.recipegen.api.operation.VariableOperation;
import com.sigmundgranaas.forgero.recipegen.impl.operation.OperationRegistryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OperationRegistry functionality.
 */
class OperationRegistryTest {

	private OperationRegistryImpl registry;

	@BeforeEach
	void setUp() {
		registry = new OperationRegistryImpl();
	}

	// ============================================================
	// Registration Tests
	// ============================================================

	@Test
	void testRegisterAndRetrieve() {
		VariableOperation op = value -> value.toString().toUpperCase();
		registry.register("test:string", "upper", op);

		Optional<VariableOperation> retrieved = registry.get("test:string", "upper");

		assertTrue(retrieved.isPresent());
		assertEquals("HELLO", retrieved.get().apply("hello"));
	}

	@Test
	void testRegisterReturnsReference() {
		VariableOperation op = Object::toString;
		OperationRegistry.OperationReference ref = registry.register("test:type", "op", op);

		assertEquals("test:type", ref.group());
		assertEquals("op", ref.operation());
		assertSame(op, ref.handler());
	}

	@Test
	void testGetUnregisteredReturnsEmpty() {
		Optional<VariableOperation> result = registry.get("nonexistent:group", "operation");

		assertFalse(result.isPresent());
	}

	@Test
	void testMultipleOperationsInSameGroup() {
		registry.register("test:string", "upper", v -> v.toString().toUpperCase());
		registry.register("test:string", "lower", v -> v.toString().toLowerCase());
		registry.register("test:string", "length", v -> String.valueOf(v.toString().length()));

		assertEquals("HELLO", registry.get("test:string", "upper").get().apply("hello"));
		assertEquals("hello", registry.get("test:string", "lower").get().apply("HELLO"));
		assertEquals("5", registry.get("test:string", "length").get().apply("hello"));
	}

	@Test
	void testSameOperationNameDifferentGroups() {
		registry.register("test:string", "id", v -> "string:" + v);
		registry.register("test:number", "id", v -> "number:" + v);

		assertEquals("string:test", registry.get("test:string", "id").get().apply("test"));
		assertEquals("number:42", registry.get("test:number", "id").get().apply(42));
	}

	// ============================================================
	// Global Operations Tests
	// ============================================================

	@Test
	void testRegisterGlobalOperation() {
		registry.registerGlobal("toString", Object::toString);

		// Should be retrievable with any group
		Optional<VariableOperation> fromGroupA = registry.get("group:a", "toString");
		Optional<VariableOperation> fromGroupB = registry.get("group:b", "toString");

		assertTrue(fromGroupA.isPresent());
		assertTrue(fromGroupB.isPresent());
	}

	@Test
	void testGroupOperationOverridesGlobal() {
		registry.registerGlobal("format", v -> "global:" + v);
		registry.register("test:special", "format", v -> "special:" + v);

		// Group-specific should take precedence
		Optional<VariableOperation> specific = registry.get("test:special", "format");
		assertTrue(specific.isPresent());
		assertEquals("special:value", specific.get().apply("value"));

		// Other groups should use global
		Optional<VariableOperation> global = registry.get("other:group", "format");
		assertTrue(global.isPresent());
		assertEquals("global:value", global.get().apply("value"));
	}

	// ============================================================
	// Apply Tests
	// ============================================================

	@Test
	void testApplyWithGroupAndOperation() {
		registry.register("test:string", "reverse", v -> {
			String s = v.toString();
			return new StringBuilder(s).reverse().toString();
		});

		String result = registry.apply("test:string", "reverse", "hello");

		assertEquals("olleh", result);
	}

	@Test
	void testApplyWithNonexistentOperationThrows() {
		assertThrows(IllegalArgumentException.class,
				() -> registry.apply("nonexistent:group", "op", "value"));
	}

	@Test
	void testApplyWithoutGroupSearchesAll() {
		registry.register("test:string", "format", v -> "formatted:" + v);

		String result = registry.apply("format", "test");

		assertEquals("formatted:test", result);
	}

	@Test
	void testApplyWithoutGroupFallsBackToString() {
		// No operations registered
		String result = registry.apply("nonexistent", 42);

		assertEquals("42", result);
	}

	@Test
	void testApplyUsesMatchingOperation() {
		registry.register("test:string", "process",
				OperationFactory.forClass(String.class, s -> "string:" + s));
		registry.register("test:number", "process",
				OperationFactory.forClass(Integer.class, n -> "number:" + n));

		// Should find matching operation based on value type
		assertEquals("string:hello", registry.apply("process", "hello"));
		assertEquals("number:42", registry.apply("process", 42));
	}

	// ============================================================
	// OperationFactory Tests
	// ============================================================

	@Test
	void testOperationFactoryForClass() {
		VariableOperation op = OperationFactory.forClass(String.class, String::toUpperCase);

		assertTrue(op.matches("hello"));
		assertFalse(op.matches(42));
		assertEquals("HELLO", op.apply("hello"));
	}

	@Test
	void testOperationFactoryForClassFallsBack() {
		VariableOperation op = OperationFactory.forClass(String.class, String::toUpperCase);

		// Non-matching type falls back to toString
		assertEquals("42", op.apply(42));
	}

	@Test
	void testOperationFactoryAsString() {
		VariableOperation op = OperationFactory.asString();

		assertEquals("42", op.apply(42));
		assertEquals("hello", op.apply("hello"));
		assertEquals("[1, 2, 3]", op.apply(java.util.List.of(1, 2, 3)));
	}

	@Test
	void testOperationFactoryWithPriority() {
		VariableOperation base = value -> "result";
		VariableOperation withPriority = OperationFactory.withPriority(100, base);

		assertEquals(100, withPriority.priority());
		assertEquals("result", withPriority.apply("test"));
	}

	// ============================================================
	// Convenience Registration Tests
	// ============================================================

	@Test
	void testRegisterWithClassAndFunction() {
		registry.register("test:item", "namespace",
				String.class, s -> s.split(":")[0]);

		String result = registry.apply("test:item", "namespace", "minecraft:diamond");

		assertEquals("minecraft", result);
	}

	// ============================================================
	// Thread Safety Tests
	// ============================================================

	@Test
	void testConcurrentRegistration() throws InterruptedException {
		int threadCount = 10;
		Thread[] threads = new Thread[threadCount];

		for (int i = 0; i < threadCount; i++) {
			final int index = i;
			threads[i] = new Thread(() -> {
				registry.register("test:group" + index, "operation",
						value -> "result" + index);
			});
		}

		for (Thread t : threads) {
			t.start();
		}

		for (Thread t : threads) {
			t.join();
		}

		// All operations should be registered
		for (int i = 0; i < threadCount; i++) {
			assertTrue(registry.get("test:group" + i, "operation").isPresent());
		}
	}

	@Test
	void testConcurrentApply() throws InterruptedException {
		// Pre-register operations
		for (int i = 0; i < 5; i++) {
			final int index = i;
			registry.register("test:group" + i, "op",
					value -> "result" + index);
		}

		int threadCount = 20;
		Thread[] threads = new Thread[threadCount];
		boolean[] success = new boolean[threadCount];

		for (int i = 0; i < threadCount; i++) {
			final int index = i;
			threads[i] = new Thread(() -> {
				try {
					String result = registry.apply("test:group" + (index % 5), "op", "test");
					success[index] = result.startsWith("result");
				} catch (Exception e) {
					success[index] = false;
				}
			});
		}

		for (Thread t : threads) {
			t.start();
		}

		for (Thread t : threads) {
			t.join();
		}

		// All applies should succeed
		for (boolean s : success) {
			assertTrue(s);
		}
	}
}
