package com.sigmundgranaas.forgero.client.forgerotool.texture.material.factory;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureIdentifier;
import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureManager.Function;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPalette;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPaletteFactory;
import com.sigmundgranaas.forgero.item.forgerotool.material.ForgeroToolMaterial;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MaterialPaletteFactoryImpl implements MaterialPaletteFactory {
    private final HashMap<String, MaterialPalette> materialPalettes = new HashMap<>();

    @Override
    public Optional<MaterialPalette> getPalette(Function<Identifier, Resource> getResource, ForgeroTextureIdentifier textureIdentifier) {
        Optional<String> materialResult = textureIdentifier.getMaterial();

        if (materialResult.isEmpty()) {
            return Optional.empty();
        }

        String material = materialResult.get();

        if (materialPalettes.containsKey(material)) {
            return Optional.of(materialPalettes.get(material));
        }
        List<Identifier> materialDependencyIdentifiers = ForgeroToolMaterial.getMaterialRepresentations(material);
        List<Identifier> materialExclusionDependencies = ForgeroToolMaterial.getMaterialExclusions(material);

        try {
            List<BufferedImage> materialReferenceImages = new ArrayList<>();
            List<BufferedImage> materialExclusionImages = new ArrayList<>();

            for (Identifier identifier : materialDependencyIdentifiers) {
                Resource materialTexture = getResource.apply(identifier);
                BufferedInputStream materialTextureStream = new BufferedInputStream(materialTexture.getInputStream());
                BufferedImage materialTextureImage = ImageIO.read(materialTextureStream);
                materialReferenceImages.add(materialTextureImage);
            }
            for (Identifier identifier : materialExclusionDependencies) {
                Resource materialTexture = getResource.apply(identifier);
                BufferedInputStream materialTextureStream = new BufferedInputStream(materialTexture.getInputStream());
                BufferedImage materialTextureImage = ImageIO.read(materialTextureStream);
                materialExclusionImages.add(materialTextureImage);
            }
            MaterialPalette palette = MaterialPalette.createColourPalette(materialReferenceImages, materialExclusionImages);
            savePalette(material, palette);

            return Optional.of(palette);
        } catch (Exception e) {
            Forgero.LOGGER.warn(e);
            return Optional.empty();
        }
    }


    protected void savePalette(String material, MaterialPalette palette) {
        materialPalettes.put(material, palette);
    }

    @Override
    public void clearCache() {
        materialPalettes.clear();
    }
}
