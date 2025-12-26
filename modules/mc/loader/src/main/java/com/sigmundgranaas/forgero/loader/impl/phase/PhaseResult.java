package com.sigmundgranaas.forgero.loader.impl.phase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents the result of an initialization phase.
 * <p>
 * PhaseResult enables graceful degradation by allowing phases to partially succeed
 * or fail without crashing the entire initialization process. It captures:
 * <ul>
 *   <li>The result value (if successful)</li>
 *   <li>Any warnings that occurred during execution</li>
 *   <li>Any error that caused failure</li>
 *   <li>A fallback value for graceful degradation</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * <pre>{@code
 * // Successful result
 * PhaseResult<TagResolver> result = PhaseResult.success(loadedTags);
 *
 * // Partial success with warnings
 * PhaseResult<TagResolver> result = PhaseResult.partial(loadedTags)
 *     .withWarning("Some tags could not be loaded")
 *     .build();
 *
 * // Failure with fallback
 * PhaseResult<TagResolver> result = PhaseResult.failure(
 *     "tags", exception, TagResolver.empty()
 * );
 *
 * // Chain operations
 * PhaseResult<Config> config = loadTags()
 *     .flatMap(tags -> createConfig(tags));
 * }</pre>
 *
 * @param <T> The type of the result value
 */
public sealed interface PhaseResult<T> {

	/**
	 * Returns the result value.
	 * For failures, this returns the fallback value.
	 *
	 * @return The result value or fallback
	 */
	T value();

	/**
	 * Returns true if this result represents a complete success.
	 */
	boolean isSuccess();

	/**
	 * Returns true if this result represents a failure.
	 */
	boolean isFailed();

	/**
	 * Returns true if this result has warnings.
	 */
	boolean hasWarnings();

	/**
	 * Returns the list of warnings.
	 */
	List<Warning> warnings();

	/**
	 * Returns the error if this is a failure.
	 */
	Optional<Throwable> getError();

	/**
	 * Returns the phase name if this is a failure.
	 */
	Optional<String> failedPhase();

	/**
	 * Maps the value using the given function.
	 */
	<R> PhaseResult<R> map(Function<T, R> mapper);

	/**
	 * Flat maps the value, allowing chained phase operations.
	 */
	<R> PhaseResult<R> flatMap(Function<T, PhaseResult<R>> mapper);

	/**
	 * Executes the given action if successful.
	 */
	PhaseResult<T> onSuccess(Consumer<T> action);

	/**
	 * Executes the given action if failed.
	 */
	PhaseResult<T> onFailure(Consumer<Failure<T>> action);

	/**
	 * Returns the value or a computed fallback if failed.
	 */
	T orElseGet(Supplier<T> fallback);

	// Factory methods

	/**
	 * Creates a successful result.
	 */
	static <T> PhaseResult<T> success(T value) {
		return new Success<>(value, Collections.emptyList());
	}

	/**
	 * Creates a partial success builder.
	 */
	static <T> PartialBuilder<T> partial(T value) {
		return new PartialBuilder<>(value);
	}

	/**
	 * Creates a failure result with a fallback value.
	 */
	static <T> PhaseResult<T> failure(String phase, Throwable error, T fallback) {
		return new Failure<>(phase, error, fallback, Collections.emptyList());
	}

	/**
	 * Creates a failure result with warnings.
	 */
	static <T> PhaseResult<T> failure(String phase, Throwable error, T fallback, List<Warning> warnings) {
		return new Failure<>(phase, error, fallback, List.copyOf(warnings));
	}

	/**
	 * Executes a phase operation with automatic error handling.
	 */
	static <T> PhaseResult<T> execute(String phase, Supplier<T> operation, T fallback) {
		try {
			return success(operation.get());
		} catch (Exception e) {
			return failure(phase, e, fallback);
		}
	}

	/**
	 * Combines multiple results, collecting all warnings and returning the first failure if any.
	 */
	static <T> PhaseResult<List<T>> combine(List<PhaseResult<T>> results) {
		List<T> values = new ArrayList<>();
		List<Warning> allWarnings = new ArrayList<>();
		Failure<T> firstFailure = null;

		for (PhaseResult<T> result : results) {
			values.add(result.value());
			allWarnings.addAll(result.warnings());
			if (result.isFailed() && firstFailure == null) {
				firstFailure = (Failure<T>) result;
			}
		}

		if (firstFailure != null) {
			return new Failure<>(
					firstFailure.phase(),
					firstFailure.error,
					values,
					allWarnings
			);
		}

		if (!allWarnings.isEmpty()) {
			return new Success<>(values, allWarnings);
		}

		return success(values);
	}

	// Implementations

	/**
	 * Represents a successful phase result.
	 */
	record Success<T>(T value, List<Warning> warnings) implements PhaseResult<T> {

		public Success {
			warnings = List.copyOf(warnings);
		}

		@Override
		public boolean isSuccess() {
			return true;
		}

		@Override
		public boolean isFailed() {
			return false;
		}

		@Override
		public boolean hasWarnings() {
			return !warnings.isEmpty();
		}

		@Override
		public Optional<Throwable> getError() {
			return Optional.empty();
		}

		@Override
		public Optional<String> failedPhase() {
			return Optional.empty();
		}

		@Override
		public <R> PhaseResult<R> map(Function<T, R> mapper) {
			return new Success<>(mapper.apply(value), warnings);
		}

		@Override
		public <R> PhaseResult<R> flatMap(Function<T, PhaseResult<R>> mapper) {
			PhaseResult<R> result = mapper.apply(value);
			if (result instanceof Success<R> success) {
				List<Warning> combined = new ArrayList<>(warnings);
				combined.addAll(success.warnings);
				return new Success<>(success.value, combined);
			}
			return result;
		}

		@Override
		public PhaseResult<T> onSuccess(Consumer<T> action) {
			action.accept(value);
			return this;
		}

		@Override
		public PhaseResult<T> onFailure(Consumer<Failure<T>> action) {
			return this;
		}

		@Override
		public T orElseGet(Supplier<T> fallback) {
			return value;
		}
	}

	/**
	 * Represents a failed phase result.
	 */
	record Failure<T>(String phase, Throwable error, T fallback, List<Warning> warnings) implements PhaseResult<T> {

		public Failure {
			warnings = List.copyOf(warnings);
		}

		@Override
		public T value() {
			return fallback;
		}

		@Override
		public boolean isSuccess() {
			return false;
		}

		@Override
		public boolean isFailed() {
			return true;
		}

		@Override
		public boolean hasWarnings() {
			return !warnings.isEmpty();
		}

		@Override
		public Optional<Throwable> getError() {
			return Optional.of(error);
		}

		@Override
		public Optional<String> failedPhase() {
			return Optional.of(phase);
		}

		@Override
		public <R> PhaseResult<R> map(Function<T, R> mapper) {
			return new Failure<>(phase, error, mapper.apply(fallback), warnings);
		}

		@Override
		public <R> PhaseResult<R> flatMap(Function<T, PhaseResult<R>> mapper) {
			PhaseResult<R> fallbackResult = mapper.apply(fallback);
			return new Failure<>(phase, error, fallbackResult.value(), warnings);
		}

		@Override
		public PhaseResult<T> onSuccess(Consumer<T> action) {
			return this;
		}

		@Override
		public PhaseResult<T> onFailure(Consumer<Failure<T>> action) {
			action.accept(this);
			return this;
		}

		@Override
		public T orElseGet(Supplier<T> fallback) {
			return fallback.get();
		}
	}

	/**
	 * Builder for creating partial success results with warnings.
	 */
	class PartialBuilder<T> {
		private final T value;
		private final List<Warning> warnings = new ArrayList<>();

		PartialBuilder(T value) {
			this.value = value;
		}

		public PartialBuilder<T> withWarning(String message) {
			warnings.add(new Warning(message, null));
			return this;
		}

		public PartialBuilder<T> withWarning(String message, Throwable cause) {
			warnings.add(new Warning(message, cause));
			return this;
		}

		public PartialBuilder<T> withWarnings(List<Warning> warnings) {
			this.warnings.addAll(warnings);
			return this;
		}

		public PhaseResult<T> build() {
			return new Success<>(value, warnings);
		}
	}

	/**
	 * Represents a warning that occurred during phase execution.
	 */
	record Warning(String message, Throwable cause) {
		public Warning(String message) {
			this(message, null);
		}

		public boolean hasCause() {
			return cause != null;
		}
	}
}
