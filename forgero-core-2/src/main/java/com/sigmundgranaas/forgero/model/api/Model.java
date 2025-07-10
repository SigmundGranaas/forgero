package com.sigmundgranaas.forgero.model.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

public sealed interface Model permits StaticModel, CompositeModel { OpenIdentifier getIdentifier(); }
