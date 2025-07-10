package com.sigmundgranaas.forgero.model.api;

import java.awt.image.BufferedImage;
import java.util.Optional;

public interface TextureProvider { Optional<BufferedImage> getTexture(String identifier); }
