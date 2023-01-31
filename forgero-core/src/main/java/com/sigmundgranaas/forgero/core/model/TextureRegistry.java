package com.sigmundgranaas.forgero.core.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TextureRegistry {
    private final Set<String> registeredTextures;
    private final Set<String> loadedTextures;

    public TextureRegistry() {
        this.registeredTextures = new HashSet<>();
        this.loadedTextures = new HashSet<>();
    }

    public TextureRegistry registerTexture(String texture) {
        registeredTextures.add(texture);
        return this;
    }

    public TextureRegistry registerLoadedTexture(String texture) {
        loadedTextures.add(texture);
        return this;
    }

    List<String> registeredTextures() {
        return registeredTextures.stream().toList();
    }

    List<String> loadedTextures() {
        return loadedTextures.stream().toList();
    }
}
