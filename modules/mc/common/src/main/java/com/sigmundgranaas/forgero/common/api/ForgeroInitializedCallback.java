package com.sigmundgranaas.forgero.common.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Optional;

/**
 * Callback fired when Forgero has completed initialization and all services are available.
 * <p>
 * This is the recommended way for internal code and plugins to receive the {@link ForgeroServices}
 * instance for dependency injection. Register a listener during your mod's initialization phase
 * to receive the services when they become available.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * public class MyMod implements ModInitializer {
 *     private ForgeroServices forgeroServices;
 *
 *     @Override
 *     public void onInitialize() {
 *         ForgeroInitializedCallback.EVENT.register(services -> {
 *             this.forgeroServices = services;
 *             initializeMyForgeroIntegration(services);
 *         });
 *     }
 *
 *     private void initializeMyForgeroIntegration(ForgeroServices services) {
 *         TagResolver resolver = services.tagResolver();
 *         // Use the resolver to set up your systems...
 *     }
 * }
 * }</pre>
 *
 * <h2>Alternative: Direct Access</h2>
 * <p>
 * For code that may run after Forgero initialization (e.g., GameTests), use the
 * static accessor:
 * <pre>{@code
 * Optional<ForgeroServices> services = ForgeroInitializedCallback.getServices();
 * services.ifPresent(s -> {
 *     // Use the services...
 * });
 * }</pre>
 *
 * <h2>Timing</h2>
 * <p>
 * This event is fired during Forgero's mod initialization, after all data has been loaded
 * and all registries are populated. It is safe to query any service at this point.
 * <p>
 * The event is fired synchronously on the main thread during mod initialization.
 *
 * @see ForgeroServices for the services interface
 */
@FunctionalInterface
public interface ForgeroInitializedCallback {

	/**
	 * Stores the services after initialization for late access.
	 */
	class ServicesHolder {
		private static volatile ForgeroServices services;

		static void setServices(ForgeroServices services) {
			ServicesHolder.services = services;
		}

		static ForgeroServices getServices() {
			return services;
		}
	}

	/**
	 * The event instance. Register listeners using {@code EVENT.register(callback)}.
	 */
	Event<ForgeroInitializedCallback> EVENT = EventFactory.createArrayBacked(
			ForgeroInitializedCallback.class,
			callbacks -> services -> {
				// Store services for late access
				ServicesHolder.setServices(services);
				for (ForgeroInitializedCallback callback : callbacks) {
					callback.onForgeroInitialized(services);
				}
			}
	);

	/**
	 * Called when Forgero initialization is complete and all services are available.
	 *
	 * @param services The fully initialized Forgero services container
	 */
	void onForgeroInitialized(ForgeroServices services);

	/**
	 * Returns the Forgero services if initialization is complete.
	 * <p>
	 * This is useful for code that may run after Forgero's initialization event,
	 * such as GameTests or late-loading plugins.
	 *
	 * @return The services if available, empty otherwise
	 */
	static Optional<ForgeroServices> getServices() {
		return Optional.ofNullable(ServicesHolder.getServices());
	}

	/**
	 * Returns true if Forgero has completed initialization.
	 *
	 * @return true if services are available
	 */
	static boolean isInitialized() {
		return ServicesHolder.getServices() != null;
	}

	/**
	 * Registers a callback and immediately invokes it if services are already available.
	 * <p>
	 * This is useful for code that may be initialized after Forgero's initialization event
	 * has already fired. The callback will be:
	 * - Called immediately if services are already available
	 * - Registered for future invocation if services are not yet available
	 *
	 * @param callback The callback to register and potentially invoke immediately
	 */
	static void registerAndReplay(ForgeroInitializedCallback callback) {
		// Register for future events
		EVENT.register(callback);

		// If already initialized, replay immediately
		ForgeroServices services = ServicesHolder.getServices();
		if (services != null) {
			callback.onForgeroInitialized(services);
		}
	}
}
