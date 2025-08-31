package com.sigmundgranaas.forgero.model.loading.api.item;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.item.Model;

import java.util.Optional;

public interface ItemModelProvider { Optional<Model> get(OpenIdentifier id); }
