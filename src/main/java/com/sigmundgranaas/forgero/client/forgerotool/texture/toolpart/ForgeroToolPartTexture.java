package com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public interface ForgeroToolPartTexture {
    @NotNull InputStream getToolPartTexture() throws IOException;
}
