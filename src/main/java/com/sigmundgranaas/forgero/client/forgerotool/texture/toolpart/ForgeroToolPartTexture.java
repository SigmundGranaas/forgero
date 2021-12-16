package com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * ForgeroToolPartTextures are objects representing a template shape, and a Material colour palette.
 * The return ToolPartTexture can either be preconfigured, or generated.
 */
public interface ForgeroToolPartTexture {

    /**
     * This method will return an Image representation of itself in the form of an InputStream.
     * Used for creating a Minecraft Resource.
     *
     * @return - A stream of a texture
     * @throws IOException - if it is unable to write the image to an InputSTream
     */
    @NotNull InputStream getToolPartTexture() throws IOException;
}
