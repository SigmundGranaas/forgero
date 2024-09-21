package com.sigmundgranaas.forgero.api.v0.entrypoint;

import com.sigmundgranaas.forgero.service.StateService;
import org.jetbrains.annotations.NotNull;

/**
 * This entrypoint is called when Forgero is initialized.
 * <p>
 * Use this entrypoint to execute code that relies on {@link StateService}.
 */
public interface ForgeroInitializedEntryPoint {
	void onInitialized(@NotNull StateService service);
}
