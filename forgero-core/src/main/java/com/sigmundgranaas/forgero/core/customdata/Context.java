package com.sigmundgranaas.forgero.core.customdata;

/**
 * The context of a custom data field.
 * Local means that the field is only available in the resource it was declared in.
 * <p>
 * Transitive means that the field is available in all resources that inherit from the resource it was declared in.
 */
public enum Context {
	LOCAL,
	TRANSITIVE
}
