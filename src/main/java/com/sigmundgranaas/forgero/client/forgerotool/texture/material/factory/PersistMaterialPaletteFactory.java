package com.sigmundgranaas.forgero.client.forgerotool.texture.material.factory;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPalette;
import com.sigmundgranaas.forgero.client.forgerotool.texture.utils.TextureLoader;
import com.sigmundgranaas.forgero.item.Constants;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

public class PersistMaterialPaletteFactory extends MaterialPaletteFactoryImpl {

    @Override
    protected void savePalette(String material, MaterialPalette palette) {
        super.savePalette(material, palette);
        persistPalette(material, palette);
    }

    protected void persistPalette(String material, MaterialPalette palette) {
        if (!paletteExistsInSrcFiles(material, palette)) {
            persistPaletteAsImage(material, palette);
        }
    }


    protected boolean paletteExistsInSrcFiles(String material, MaterialPalette palette) {
        return TextureLoader.textureExists(Constants.DEV_SRC_RESOURCES_PATH + Constants.CONFIG_PATH + "forgero/templates/materials/" + getPaletteFileNameWithExtension(material));
    }


    protected @NotNull
    String getTemplateMaterialFolder() {
        return Constants.DEV_SRC_RESOURCES_PATH + Constants.CONFIG_PATH + "forgero/templates/materials/";
    }


    protected void persistPaletteAsImage(String material, MaterialPalette palette) {
        BufferedImage paletteImage = new BufferedImage(palette.getColourValues().size(), 1, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < palette.getColourValues().size(); x++) {
            paletteImage.setRGB(x, 0, palette.getColourValues().get(x).getRgb());
        }
        try {
            TextureLoader.saveTextureToFile(getTemplateMaterialFolder() + getPaletteFileNameWithExtension(material), paletteImage);
        } catch (Exception e) {
            Forgero.LOGGER.warn("Unable to save Template texture palette: {}", e, e);
        }
    }
}
