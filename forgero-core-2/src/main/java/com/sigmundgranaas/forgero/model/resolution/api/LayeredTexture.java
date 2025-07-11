package com.sigmundgranaas.forgero.model.resolution.api;

import com.sigmundgranaas.forgero.model.api.RenderableTexture; // Changed import

import java.util.List;

// This record now simply wraps a list of RenderableTexture, which already contains order and offset.
public record LayeredTexture(List<RenderableTexture> layers) {} // Changed to List<RenderableTexture>
