package com.sigmundgranaas.forgero.utility.resource.loader.api;

/**
 * Interface for resource providers that can notify listeners when resources change.
 * <p>
 * This is typically implemented by file-watching providers that detect filesystem
 * changes during development.
 *
 * @see ResourceReloadListener
 */
public interface ResourceReloadNotifier {

	/**
	 * Adds a listener to be notified when resources change.
	 *
	 * @param listener The listener to add
	 */
	void addListener(ResourceReloadListener listener);

	/**
	 * Removes a previously added listener.
	 *
	 * @param listener The listener to remove
	 */
	void removeListener(ResourceReloadListener listener);

	/**
	 * Listener interface for resource change notifications.
	 */
	@FunctionalInterface
	interface ResourceReloadListener {
		/**
		 * Called when a resource has changed.
		 *
		 * @param path The path of the changed resource
		 * @param type The type of change
		 */
		void onResourceChanged(ResourcePath path, ChangeType type);
	}

	/**
	 * Types of resource changes.
	 */
	enum ChangeType {
		/** A new resource was created */
		CREATED,
		/** An existing resource was modified */
		MODIFIED,
		/** A resource was deleted */
		DELETED
	}
}
