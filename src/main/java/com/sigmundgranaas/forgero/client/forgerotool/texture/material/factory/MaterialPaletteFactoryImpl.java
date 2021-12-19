package com.sigmundgranaas.forgero.client.forgerotool.texture.material.factory;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureIdentifier;
import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureManager.Function;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPalette;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPaletteFactory;
import com.sigmundgranaas.forgero.client.forgerotool.texture.utils.TextureLoader;
import com.sigmundgranaas.forgero.item.forgerotool.Constants;
import com.sigmundgranaas.forgero.material.ForgeroToolMaterial;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.net.URISyntaxException;
import java.util.*;

public class MaterialPaletteFactoryImpl implements MaterialPaletteFactory {
    private final HashMap<String, MaterialPalette> materialPalettes = new HashMap<>();

    @Override
    public Optional<MaterialPalette> getPalette(Function<Identifier, Resource> getResource, @NotNull ForgeroTextureIdentifier textureIdentifier) {
        Optional<String> materialResult = textureIdentifier.getMaterial();

        if (materialResult.isEmpty()) {
            return Optional.empty();
        }

        String material = materialResult.get();

        if (materialPalettes.containsKey(material)) {
            return Optional.of(materialPalettes.get(material));
        } else {
            return getOrCreatePalette(material, getResource);
        }
    }

    private Optional<MaterialPalette> getOrCreatePalette(String material, Function<Identifier, Resource> getResource) {
        Optional<MaterialPalette> palette;

        if (paletteExists(material)) {
            palette = getPaletteFromFile(material);
        } else {
            palette = generatePaletteFromTextures(material, getResource);
        }
        palette.ifPresent(materialPalette -> savePalette(material, materialPalette));

        return palette;
    }


    private Optional<MaterialPalette> getPaletteFromFile(String material) {
        Optional<BufferedImage> paletteResult = TextureLoader.loadTextureFromFile(Constants.CONFIG_PATH + "forgero/templates/materials/" + getPaletteFileNameWithExtension(material));
        return paletteResult.map(MaterialPalette::createColourPaletteFromExistingPalette);
    }

    protected Optional<MaterialPalette> generatePaletteFromTextures(String material, Function<Identifier, Resource> getResource) {
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
            return Optional.of(palette);
        } catch (Exception e) {
            Forgero.LOGGER.warn(e);
            return Optional.empty();
        }
    }


    protected void savePalette(String material, MaterialPalette palette) {
        materialPalettes.put(material, palette);
    }

    protected boolean paletteExists(String material) {
        try {
            return TextureLoader.textureExists(TextureLoader.getFileFromResource(Constants.CONFIG_PATH + "forgero/templates/materials/" + getPaletteFileNameWithExtension(material)).getAbsolutePath());
        } catch (URISyntaxException | IllegalArgumentException e) {
            Forgero.LOGGER.warn("Palette for material: {} not found, will generate new palettes", material);
            return false;
        }
    }

    protected String getPaletteFileNameWithExtension(String material) {
        return material.toLowerCase(Locale.ROOT) + "_palette.png";
    }

    @Override
    public void clearCache() {
        materialPalettes.clear();
    }
}
