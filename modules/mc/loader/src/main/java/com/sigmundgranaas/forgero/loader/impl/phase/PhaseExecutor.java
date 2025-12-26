package com.sigmundgranaas.forgero.loader.impl.phase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Utility for executing initialization phases with error handling and logging.
 * <p>
 * PhaseExecutor provides a standardized way to run phases that:
 * <ul>
 *   <li>Logs phase start and completion</li>
 *   <li>Catches exceptions and converts them to PhaseResult failures</li>
 *   <li>Provides fallback values for graceful degradation</li>
 *   <li>Logs warnings and errors appropriately</li>
 * </ul>
 */
public class PhaseExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(PhaseExecutor.class);

	private final String moduleName;
	private final boolean failFast;

	/**
	 * Creates a PhaseExecutor for the given module.
	 *
	 * @param moduleName The name of the module (for logging)
	 * @param failFast   If true, failures will be rethrown; if false, graceful degradation is used
	 */
	public PhaseExecutor(String moduleName, boolean failFast) {
		this.moduleName = moduleName;
		this.failFast = failFast;
	}

	/**
	 * Creates a PhaseExecutor with graceful degradation enabled.
	 */
	public PhaseExecutor(String moduleName) {
		this(moduleName, false);
	}

	/**
	 * Executes a phase with automatic error handling.
	 *
	 * @param phaseName The name of the phase (for logging)
	 * @param operation The operation to execute
	 * @param fallback  The fallback value if the operation fails
	 * @param <T>       The result type
	 * @return A PhaseResult containing the result or fallback
	 */
	public <T> PhaseResult<T> execute(String phaseName, Supplier<T> operation, T fallback) {
		LOGGER.debug("[{}] Starting phase: {}", moduleName, phaseName);
		long start = System.currentTimeMillis();

		try {
			T result = operation.get();
			long duration = System.currentTimeMillis() - start;
			LOGGER.debug("[{}] Phase '{}' completed in {}ms", moduleName, phaseName, duration);
			return PhaseResult.success(result);
		} catch (Exception e) {
			long duration = System.currentTimeMillis() - start;
			LOGGER.error("[{}] Phase '{}' failed after {}ms: {}", moduleName, phaseName, duration, e.getMessage());

			if (failFast) {
				throw new PhaseExecutionException(phaseName, e);
			}

			LOGGER.warn("[{}] Using fallback value for phase '{}'", moduleName, phaseName);
			return PhaseResult.failure(phaseName, e, fallback);
		}
	}

	/**
	 * Executes a phase that returns a PhaseResult directly.
	 * <p>
	 * Use this when the operation itself may produce warnings or partial results.
	 *
	 * @param phaseName The name of the phase
	 * @param operation The operation to execute
	 * @param fallback  The fallback value if the operation throws
	 * @param <T>       The result type
	 * @return The PhaseResult from the operation, or a failure result
	 */
	public <T> PhaseResult<T> executeWithResult(String phaseName, Supplier<PhaseResult<T>> operation, T fallback) {
		LOGGER.debug("[{}] Starting phase: {}", moduleName, phaseName);
		long start = System.currentTimeMillis();

		try {
			PhaseResult<T> result = operation.get();
			long duration = System.currentTimeMillis() - start;

			if (result.isFailed()) {
				LOGGER.error("[{}] Phase '{}' failed after {}ms", moduleName, phaseName, duration);
				if (failFast) {
					result.getError().ifPresent(e -> {
						throw new PhaseExecutionException(phaseName, e);
					});
				}
			} else if (result.hasWarnings()) {
				LOGGER.warn("[{}] Phase '{}' completed with {} warning(s) in {}ms",
						moduleName, phaseName, result.warnings().size(), duration);
				for (PhaseResult.Warning warning : result.warnings()) {
					LOGGER.warn("[{}] Warning: {}", moduleName, warning.message());
				}
			} else {
				LOGGER.debug("[{}] Phase '{}' completed in {}ms", moduleName, phaseName, duration);
			}

			return result;
		} catch (Exception e) {
			long duration = System.currentTimeMillis() - start;
			LOGGER.error("[{}] Phase '{}' threw exception after {}ms: {}", moduleName, phaseName, duration, e.getMessage());

			if (failFast) {
				throw new PhaseExecutionException(phaseName, e);
			}

			return PhaseResult.failure(phaseName, e, fallback);
		}
	}

	/**
	 * Logs a summary of all phase results.
	 *
	 * @param results The results to summarize
	 */
	public void logSummary(PhaseResult<?>... results) {
		int successful = 0;
		int failed = 0;
		int withWarnings = 0;

		for (PhaseResult<?> result : results) {
			if (result.isFailed()) {
				failed++;
			} else {
				successful++;
			}
			if (result.hasWarnings()) {
				withWarnings++;
			}
		}

		if (failed > 0) {
			LOGGER.warn("[{}] Initialization summary: {} successful, {} failed, {} with warnings",
					moduleName, successful, failed, withWarnings);
		} else if (withWarnings > 0) {
			LOGGER.info("[{}] Initialization summary: {} successful, {} with warnings",
					moduleName, successful, withWarnings);
		} else {
			LOGGER.info("[{}] Initialization summary: {} phases completed successfully", moduleName, successful);
		}
	}

	/**
	 * Exception thrown when a phase fails in fail-fast mode.
	 */
	public static class PhaseExecutionException extends RuntimeException {
		private final String phaseName;

		public PhaseExecutionException(String phaseName, Throwable cause) {
			super("Phase '" + phaseName + "' failed: " + cause.getMessage(), cause);
			this.phaseName = phaseName;
		}

		public String getPhaseName() {
			return phaseName;
		}
	}
}
