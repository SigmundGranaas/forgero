package com.sigmundgranaas.forgero.loader.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

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
	 * The event instance. Register listeners using {@code EVENT.register(callback)}.
	 */
	Event<ForgeroInitializedCallback> EVENT = EventFactory.createArrayBacked(
			ForgeroInitializedCallback.class,
			callbacks -> services -> {
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
}
