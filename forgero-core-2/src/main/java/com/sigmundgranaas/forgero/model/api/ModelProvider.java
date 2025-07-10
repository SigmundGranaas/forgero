package com.sigmundgranaas.forgero.model.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import java.util.Optional;

public interface ModelProvider { Optional<Model> get(OpenIdentifier id); }
