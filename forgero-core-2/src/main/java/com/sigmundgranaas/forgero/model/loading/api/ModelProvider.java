package com.sigmundgranaas.forgero.model.loading.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.Model;

import java.util.Optional;

public interface ModelProvider { Optional<Model> get(OpenIdentifier id); }
