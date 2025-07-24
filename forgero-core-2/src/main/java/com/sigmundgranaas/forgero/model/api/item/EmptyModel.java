package com.sigmundgranaas.forgero.model.api.item;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;

import java.util.Optional;

/**
 * A special model type that represents nothing.
 * Any component or layer resolving to this model will not be rendered.
 */
public non-sealed class EmptyModel implements Model {
	public static final EmptyModel INSTANCE = new EmptyModel(new OpenIdentifier("forgero", "common/empty"));

	private final OpenIdentifier identifier;

	private EmptyModel(OpenIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	public OpenIdentifier getIdentifier() {
		return identifier;
	}

	@Override
	public Optional<OpenIdentifier> getTarget() {
		return Optional.empty();
	}

	@Override
	public Optional<String> getContext() {
		return Optional.empty();
	}

	@Override
	public Optional<OpenIdentifier> getParent() {
		return Optional.empty();
	}

	@Override
	public Optional<JsonElement> getDisplay() {
		return Optional.empty();
	}

	@Override
	public Model apply(ModelResolutionContext context) {
		return this;
	}
}
