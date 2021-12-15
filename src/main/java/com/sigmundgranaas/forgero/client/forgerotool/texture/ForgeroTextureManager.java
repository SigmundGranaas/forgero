package com.sigmundgranaas.forgero.client.forgerotool.texture;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface ForgeroTextureManager {
    public static ForgeroTextureManager INSTANCE = ForgeroTextureManagerImpl.getInstance();

    /**
     * Method to check if resource should be loaded by ForgeroTextureManager.
     *
     * @param resourceId the Identifier of the requested resource
     * @return Will return True if the Texture manager is required to load this texture
     */
    boolean isManagedByForgeroTextureManager(Identifier resourceId);

    /**
     * Method the get resource managed by ForgeroTextureManager
     * These textures are generated on the fly.
     * This method should only be called after checking if the requested resource is managed by the Texture Manager.
     *
     * @param resourceId resourceId the Identifier of the requested resource
     * @return Returns a Resource containing the requested texture
     * @throws IOException if the TextureManager is unable to find the necessary resources or create the requested texture.
     */
    @NotNull
    Resource getResource(Identifier resourceId, Function<Identifier, Resource> getResource) throws IOException;

    @FunctionalInterface
    public interface Function<T, K> {
        K apply(T id) throws IOException;
    }
}
