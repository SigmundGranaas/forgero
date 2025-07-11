package com.sigmundgranaas.forgero.model.rendering.api;

import com.sigmundgranaas.forgero.model.api.RenderableTexture; // Changed to RenderableTexture

import java.awt.image.BufferedImage;
import java.util.List; // Changed to List

public interface TextureCompositor { BufferedImage render(List<RenderableTexture> model); } // Changed to List<RenderableTexture>
