package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public interface Texture {
    InputStream getStream() throws IOException;

    TextureIdentifier getId();

    BufferedImage getImage();
}
