package com.sigmundgranaas.forgero.client;

public class ClientConfigurationImpl implements ClientConfiguration {
    private static ClientConfigurationImpl INSTANCE;

    public static ClientConfigurationImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClientConfigurationImpl();
        }
        return INSTANCE;
    }


    @Override
    public boolean shouldCreateNewTextures() {
        return true;
    }

    @Override
    public boolean shouldOverWriteOldTextures() {
        return false;
    }

    @Override
    public boolean shouldCreateNewPalettes() {
        return true;
    }

    @Override
    public boolean shouldOverWriteOldPalettes() {
        return false;
    }

    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public boolean shouldCreateNewTextureBases() {
        return true;
    }

    @Override
    public boolean shouldOverWriteOldTextureBases() {
        return true;
    }
}
