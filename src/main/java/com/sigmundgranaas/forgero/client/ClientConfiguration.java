package com.sigmundgranaas.forgero.client;

/**
 * Interface containing boolean switches and configuration items which needs to be available to other modules.
 * Should be replaced by a proper Setting solution.
 */
public interface ClientConfiguration {
    ClientConfiguration INSTANCE = ClientConfigurationImpl.getInstance();

    boolean shouldCreateNewTextures();

    boolean shouldOverWriteOldTextures();

    boolean shouldCreateNewPalettes();

    boolean shouldOverWriteOldPalettes();

    boolean isDev();

    boolean shouldCreateNewTextureBases();

    boolean shouldOverWriteOldTextureBases();
}
