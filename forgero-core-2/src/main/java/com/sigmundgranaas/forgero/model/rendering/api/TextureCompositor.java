package com.sigmundgranaas.forgero.model.rendering.api;

import com.sigmundgranaas.forgero.model.resolution.api.LayeredTexture;

import java.awt.image.BufferedImage;

public interface TextureCompositor { BufferedImage render(LayeredTexture model); }
