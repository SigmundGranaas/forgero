package com.sigmundgranaas.forgero.drp.api.lifecycle;

import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;

/**
 * Callback interface for hot-reload events.
 * Implementations are notified when resources need to be regenerated.
 */
@FunctionalInterface
public interface HotReloadListener {

	/**
	 * Called when resources are being reloaded.
	 * Implementations should regenerate their resources into the provided pack.
	 *
	 * @param pack The resource pack being reloaded (cleared and ready for new resources)
	 */
	void onReload(DynamicResourcePack pack);
}
