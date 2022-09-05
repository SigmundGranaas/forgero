package com.sigmundgranaas.forgerocore.texture;


import com.sigmundgranaas.forgerocore.deprecated.ModelLayer;

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
