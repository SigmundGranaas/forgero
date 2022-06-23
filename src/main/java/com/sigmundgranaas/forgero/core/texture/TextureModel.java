package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.client.forgerotool.model.ModelLayer;

public record TextureModel(String primary, String secondary, String gem) {
    public String getModel(ModelLayer layer) {
        return switch (layer) {
            case PRIMARY -> primary;
            case SECONDARY -> secondary;
            case GEM -> gem;
            case MISC -> primary;
        };
    }
}
