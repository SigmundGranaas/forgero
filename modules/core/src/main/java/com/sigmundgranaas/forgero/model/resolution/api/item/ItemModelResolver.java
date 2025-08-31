package com.sigmundgranaas.forgero.model.resolution.api.item;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.RenderableTexture;

import java.util.List;
import java.util.Optional;

public interface ItemModelResolver { Optional<List<RenderableTexture>> resolve(Component component); }
