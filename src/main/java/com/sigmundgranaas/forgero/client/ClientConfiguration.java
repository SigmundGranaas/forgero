package com.sigmundgranaas.forgero.client;

public interface ClientConfiguration {
    public static ClientConfiguration INSTANCE = ClientConfigurationImpl.getInstance();

    public boolean shouldCreateNewTextures();

    public boolean shouldOverWriteOldTextures();

    public boolean shouldCreateNewPalettes();

    public boolean shouldOverWriteOldPalettes();

    public boolean shouldCreateNewTextureBases();

    public boolean shouldOverWriteOldTextureBases();
}
