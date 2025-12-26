package com.sigmundgranaas.forgero.loader.impl.phase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PhaseResult error recovery system.
 */
class PhaseResultTest {

	@Nested
	@DisplayName("Success results")
	class SuccessTests {

		@Test
		@DisplayName("success should create a successful result")
		void successCreatesSuccessfulResult() {
			PhaseResult<String> result = PhaseResult.success("hello");

			assertTrue(result.isSuccess());
			assertFalse(result.isFailed());
			assertFalse(result.hasWarnings());
			assertEquals("hello", result.value());
			assertTrue(result.getError().isEmpty());
			assertTrue(result.failedPhase().isEmpty());
		}

		@Test
		@DisplayName("success should have empty warnings list")
		void successHasEmptyWarnings() {
			PhaseResult<String> result = PhaseResult.success("hello");
			assertTrue(result.warnings().isEmpty());
		}

		@Test
		@DisplayName("map should transform value")
		void mapTransformsValue() {
			PhaseResult<Integer> result = PhaseResult.success(5)
					.map(n -> n * 2);

			assertEquals(10, result.value());
			assertTrue(result.isSuccess());
		}

		@Test
		@DisplayName("flatMap should chain results")
		void flatMapChainsResults() {
			PhaseResult<Integer> result = PhaseResult.success(5)
					.flatMap(n -> PhaseResult.success(n * 2));

			assertEquals(10, result.value());
			assertTrue(result.isSuccess());
		}

		@Test
		@DisplayName("onSuccess should execute action")
		void onSuccessExecutesAction() {
			AtomicBoolean called = new AtomicBoolean(false);
			AtomicReference<String> captured = new AtomicReference<>();

			PhaseResult.success("hello")
					.onSuccess(value -> {
						called.set(true);
						captured.set(value);
					});

			assertTrue(called.get());
			assertEquals("hello", captured.get());
		}

		@Test
		@DisplayName("onFailure should not execute action for success")
		void onFailureNotExecutedForSuccess() {
			AtomicBoolean called = new AtomicBoolean(false);

			PhaseResult.success("hello")
					.onFailure(failure -> called.set(true));

			assertFalse(called.get());
		}

		@Test
		@DisplayName("orElseGet should return value for success")
		void orElseGetReturnsValueForSuccess() {
			String result = PhaseResult.success("hello")
					.orElseGet(() -> "fallback");

			assertEquals("hello", result);
		}
	}

	@Nested
	@DisplayName("Failure results")
	class FailureTests {

		@Test
		@DisplayName("failure should create a failed result with fallback")
		void failureCreatesFailedResult() {
			Exception error = new RuntimeException("test error");
			PhaseResult<String> result = PhaseResult.failure("testPhase", error, "fallback");

			assertFalse(result.isSuccess());
			assertTrue(result.isFailed());
			assertEquals("fallback", result.value());
			assertTrue(result.getError().isPresent());
			assertSame(error, result.getError().get());
			assertEquals("testPhase", result.failedPhase().orElse(null));
		}

		@Test
		@DisplayName("failure map should transform fallback value")
		void failureMapTransformsFallback() {
			PhaseResult<Integer> result = PhaseResult.failure("phase", new RuntimeException(), 5)
					.map(n -> n * 2);

			assertEquals(10, result.value());
			assertTrue(result.isFailed());
		}

		@Test
		@DisplayName("failure flatMap should use fallback")
		void failureFlatMapUsesFallback() {
			PhaseResult<Integer> result = PhaseResult.failure("phase", new RuntimeException(), 5)
					.flatMap(n -> PhaseResult.success(n * 2));

			assertEquals(10, result.value());
			assertTrue(result.isFailed()); // Still failed
		}

		@Test
		@DisplayName("onSuccess should not execute action for failure")
		void onSuccessNotExecutedForFailure() {
			AtomicBoolean called = new AtomicBoolean(false);

			PhaseResult.failure("phase", new RuntimeException(), "fallback")
					.onSuccess(value -> called.set(true));

			assertFalse(called.get());
		}

		@Test
		@DisplayName("onFailure should execute action for failure")
		void onFailureExecutesAction() {
			AtomicBoolean called = new AtomicBoolean(false);
			AtomicReference<String> capturedPhase = new AtomicReference<>();

			PhaseResult.failure("testPhase", new RuntimeException(), "fallback")
					.onFailure(failure -> {
						called.set(true);
						capturedPhase.set(failure.phase());
					});

			assertTrue(called.get());
			assertEquals("testPhase", capturedPhase.get());
		}

		@Test
		@DisplayName("orElseGet should return computed fallback for failure")
		void orElseGetReturnsComputedFallback() {
			String result = PhaseResult.failure("phase", new RuntimeException(), "original")
					.orElseGet(() -> "computed");

			assertEquals("computed", result);
		}
	}

	@Nested
	@DisplayName("Partial results with warnings")
	class PartialTests {

		@Test
		@DisplayName("partial should create result with warnings")
		void partialCreatesResultWithWarnings() {
			PhaseResult<String> result = PhaseResult.partial("value")
					.withWarning("warning 1")
					.withWarning("warning 2")
					.build();

			assertTrue(result.isSuccess());
			assertTrue(result.hasWarnings());
			assertEquals(2, result.warnings().size());
			assertEquals("value", result.value());
		}

		@Test
		@DisplayName("warnings should be preserved through map")
		void warningsPreservedThroughMap() {
			PhaseResult<Integer> result = PhaseResult.partial(5)
					.withWarning("original warning")
					.build()
					.map(n -> n * 2);

			assertEquals(10, result.value());
			assertEquals(1, result.warnings().size());
		}

		@Test
		@DisplayName("flatMap should combine warnings")
		void flatMapCombinesWarnings() {
			PhaseResult<Integer> first = PhaseResult.partial(5)
					.withWarning("warning 1")
					.build();

			PhaseResult<Integer> result = first.flatMap(n ->
					PhaseResult.partial(n * 2)
							.withWarning("warning 2")
							.build()
			);

			assertEquals(10, result.value());
			assertEquals(2, result.warnings().size());
		}

		@Test
		@DisplayName("warning with cause should store cause")
		void warningWithCauseStoresCause() {
			Exception cause = new RuntimeException("root cause");
			PhaseResult<String> result = PhaseResult.partial("value")
					.withWarning("something went wrong", cause)
					.build();

			PhaseResult.Warning warning = result.warnings().get(0);
			assertTrue(warning.hasCause());
			assertSame(cause, warning.cause());
		}
	}

	@Nested
	@DisplayName("Execute helper")
	class ExecuteTests {

		@Test
		@DisplayName("execute should return success on normal completion")
		void executeReturnsSuccessOnNormalCompletion() {
			PhaseResult<String> result = PhaseResult.execute(
					"testPhase",
					() -> "computed value",
					"fallback"
			);

			assertTrue(result.isSuccess());
			assertEquals("computed value", result.value());
		}

		@Test
		@DisplayName("execute should return failure on exception")
		void executeReturnsFailureOnException() {
			RuntimeException error = new RuntimeException("test error");
			PhaseResult<String> result = PhaseResult.execute(
					"testPhase",
					() -> {
						throw error;
					},
					"fallback"
			);

			assertTrue(result.isFailed());
			assertEquals("fallback", result.value());
			assertEquals("testPhase", result.failedPhase().orElse(null));
			assertSame(error, result.getError().orElse(null));
		}
	}

	@Nested
	@DisplayName("Combine helper")
	class CombineTests {

		@Test
		@DisplayName("combine should collect all values")
		void combineCollectsAllValues() {
			List<PhaseResult<Integer>> results = List.of(
					PhaseResult.success(1),
					PhaseResult.success(2),
					PhaseResult.success(3)
			);

			PhaseResult<List<Integer>> combined = PhaseResult.combine(results);

			assertTrue(combined.isSuccess());
			assertEquals(List.of(1, 2, 3), combined.value());
		}

		@Test
		@DisplayName("combine should collect all warnings")
		void combineCollectsAllWarnings() {
			List<PhaseResult<Integer>> results = List.of(
					PhaseResult.partial(1).withWarning("warning 1").build(),
					PhaseResult.success(2),
					PhaseResult.partial(3).withWarning("warning 2").build()
			);

			PhaseResult<List<Integer>> combined = PhaseResult.combine(results);

			assertTrue(combined.isSuccess());
			assertEquals(2, combined.warnings().size());
		}

		@Test
		@DisplayName("combine should return failure if any result failed")
		void combineReturnsFailureIfAnyFailed() {
			List<PhaseResult<Integer>> results = List.of(
					PhaseResult.success(1),
					PhaseResult.failure("phase2", new RuntimeException(), 0),
					PhaseResult.success(3)
			);

			PhaseResult<List<Integer>> combined = PhaseResult.combine(results);

			assertTrue(combined.isFailed());
			assertEquals("phase2", combined.failedPhase().orElse(null));
			// Should still have all values (including fallback)
			assertEquals(3, combined.value().size());
		}

		@Test
		@DisplayName("combine with empty list should succeed")
		void combineWithEmptyListSucceeds() {
			PhaseResult<List<String>> combined = PhaseResult.combine(List.of());

			assertTrue(combined.isSuccess());
			assertTrue(combined.value().isEmpty());
		}
	}

	@Nested
	@DisplayName("Chaining patterns")
	class ChainingTests {

		@Test
		@DisplayName("should chain multiple successful operations")
		void chainMultipleSuccessfulOperations() {
			PhaseResult<Integer> result = PhaseResult.success(5)
					.map(n -> n * 2)         // 10
					.flatMap(n -> PhaseResult.success(n + 3)) // 13
					.map(n -> n * 2);        // 26

			assertEquals(26, result.value());
			assertTrue(result.isSuccess());
		}

		@Test
		@DisplayName("should propagate failure through chain")
		void propagateFailureThroughChain() {
			AtomicBoolean secondCalled = new AtomicBoolean(false);

			PhaseResult<Integer> result = PhaseResult.failure("first", new RuntimeException(), 0)
					.flatMap(n -> {
						secondCalled.set(true);
						return PhaseResult.success(n * 2);
					});

			assertTrue(result.isFailed());
			assertTrue(secondCalled.get()); // flatMap still applies to fallback
			assertEquals("first", result.failedPhase().orElse(null));
		}

		@Test
		@DisplayName("should handle mixed success and warnings")
		void handleMixedSuccessAndWarnings() {
			PhaseResult<Integer> result = PhaseResult.partial(5)
					.withWarning("initial warning")
					.build()
					.flatMap(n -> PhaseResult.partial(n * 2)
							.withWarning("second warning")
							.build())
					.map(n -> n + 1);

			assertEquals(11, result.value());
			assertTrue(result.isSuccess());
			assertEquals(2, result.warnings().size());
		}
	}
}
