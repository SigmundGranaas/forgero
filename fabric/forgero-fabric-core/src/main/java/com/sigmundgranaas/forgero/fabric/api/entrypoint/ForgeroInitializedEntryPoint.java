package com.sigmundgranaas.forgero.fabric.api.entrypoint;

import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

/**
 * This entrypoint is called when Forgero is initialized.
 * <p>
 * Use this entry point to execute code that relies on the Forgero state service.
 *
 * @see com.sigmundgranaas.forgero.fabric.initialization.ForgeroPostInit
 */
public interface ForgeroInitializedEntryPoint {
	void onInitialized(StateService service);
}
