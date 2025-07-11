package com.sigmundgranaas.forgero.model.resolution.api;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.RenderableTexture; // New import

import java.util.List; // Changed from Optional<LayeredTexture> to List<RenderableTexture>
import java.util.Optional;

public interface ModelResolver { Optional<List<RenderableTexture>> resolve(Component component); } // Changed return type
