package com.sigmundgranaas.forgero.model.resolution.api;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.RenderableTexture;

import java.util.List;
import java.util.Optional;

public interface ModelResolver { Optional<List<RenderableTexture>> resolve(Component component); }
