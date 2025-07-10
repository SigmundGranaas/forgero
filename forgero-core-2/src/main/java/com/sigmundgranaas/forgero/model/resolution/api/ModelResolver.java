package com.sigmundgranaas.forgero.model.resolution.api;

import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.Optional;

public interface ModelResolver { Optional<LayeredTexture> resolve(Component component); }
