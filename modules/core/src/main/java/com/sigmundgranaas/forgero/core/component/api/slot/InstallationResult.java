package com.sigmundgranaas.forgero.core.component.api.slot;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.Optional;

/**
 * Result of an upgrade installation operation, providing detailed information
 * about success, failure, and which slot was used.
 * <p>
 * This provides richer error information than returning {@code Optional<Component>}.
 * It includes:
 * <ul>
 *   <li>Success/failure status</li>
 *   <li>The resulting component (if successful)</li>
 *   <li>Which slot was filled (if successful)</li>
 *   <li>Error message (if failed)</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>{@code
 * InstallationResult result = slotManager.install(tool, gemUpgrade);
 * if (result.success()) {
 *     Component upgraded = result.component().orElseThrow();
 *     System.out.println("Installed in slot: " + result.slotId().orElse(null));
 * } else {
 *     System.err.println("Failed: " + result.errorMessage().orElse("Unknown error"));
 * }
 *
 * // Or for simple cases:
 * Component upgraded = result.orElseThrow();
 * }</pre>
 *
 * @param success      Whether installation succeeded
 * @param component    The resulting component if successful
 * @param slotId       The ID of the slot that was filled (if successful)
 * @param errorMessage Error message if failed
 */
public record InstallationResult(
		boolean success,
		Optional<Component> component,
		Optional<OpenIdentifier> slotId,
		Optional<String> errorMessage
) {

	/**
	 * Creates a successful installation result.
	 *
	 * @param component The resulting component
	 * @param slotId    The ID of the slot that was filled
	 * @return A successful installation result
	 */
	public static InstallationResult success(Component component, OpenIdentifier slotId) {
		return new InstallationResult(true, Optional.of(component),
				Optional.of(slotId), Optional.empty());
	}

	/**
	 * Creates a failed installation result.
	 *
	 * @param error The error message describing why installation failed
	 * @return A failed installation result
	 */
	public static InstallationResult failure(String error) {
		return new InstallationResult(false, Optional.empty(),
				Optional.empty(), Optional.of(error));
	}

	/**
	 * Throws an exception if installation failed, otherwise returns the component.
	 * <p>
	 * This provides a convenient way to handle results when you expect success
	 * and want to throw on failure.
	 *
	 * @return The resulting component
	 * @throws InstallationException if installation failed
	 */
	public Component orElseThrow() {
		if (!success) {
			throw new InstallationException(errorMessage.orElse("Installation failed"));
		}
		return component.orElseThrow(() ->
				new InstallationException("Success flag is true but component is empty"));
	}

	/**
	 * Returns the component if successful, otherwise returns the provided default.
	 *
	 * @param defaultComponent The component to return if installation failed
	 * @return The resulting component if successful, otherwise the default
	 */
	public Component orElse(Component defaultComponent) {
		return success ? component.orElse(defaultComponent) : defaultComponent;
	}

	/**
	 * Exception thrown when installation fails.
	 */
	public static class InstallationException extends RuntimeException {
		public InstallationException(String message) {
			super(message);
		}

		public InstallationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
