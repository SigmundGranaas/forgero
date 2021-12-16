package com.sigmundgranaas.forgero.client.forgerotool.texture.material.factory;

import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPalette;

public class OverridableMaterialPaletteFactory extends PersistMaterialPaletteFactory {

    @Override
    protected void persistPalette(String material, MaterialPalette palette) {
        persistPaletteAsImage(material, palette);
    }
}
