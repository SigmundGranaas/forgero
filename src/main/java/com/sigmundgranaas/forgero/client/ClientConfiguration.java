package com.sigmundgranaas.forgero.client;

/**
 * Interface containing boolean switches and configuration items which needs to be available to other modules.
 * Should be replaced by a proper Setting solution.
 */
public interface ClientConfiguration {
    public static ClientConfiguration INSTANCE = ClientConfigurationImpl.getInstance();

    public boolean shouldCreateNewTextures();

    public boolean shouldOverWriteOldTextures();

    public boolean shouldCreateNewPalettes();

    public boolean shouldOverWriteOldPalettes();

    public boolean shouldCreateNewTextureBases();

    public boolean shouldOverWriteOldTextureBases();
}
