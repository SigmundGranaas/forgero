package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;

import java.io.IOException;
import java.net.URISyntaxException;

public interface TextureLoader {
    Texture getResource(TextureIdentifier id) throws IOException, URISyntaxException;
}
