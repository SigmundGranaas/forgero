package com.sigmundgranaas.forgero.core.api.v0.entrypoint;

/**
 * This entrypoint is called before Forgero is initialized.
 * <p>
 * Use this entrypoint to execute code that does not rely on Forgero or Minecraft's states.
 */
public interface ForgeroPreInitializedEntryPoint {
	/**
	 * Called before Forgero is initialized.
	 *
	 * @see ForgeroPreInitializedEntryPoint
	 */
	void onPreInitialized();
}
