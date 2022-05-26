package com.sigmundgranaas.forgero.core.texture.palette;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteTemplateIdentifier;
import com.sigmundgranaas.forgero.core.material.material.PaletteResourceIdentifier;
import com.sigmundgranaas.forgero.core.texture.Texture;
import com.sigmundgranaas.forgero.core.texture.TextureLoader;
import net.minecraft.util.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Cached service for generating or fetching Palettes
 * <p>
 * This service will cache all created Palettes.
 * <p>
 * Palettes are either generated from existing textures or loaded from an existing file.
 */
public class CachedPaletteService implements PaletteService {
    private final TextureLoader loader;
    private final PaletteFactory factory;
    private final Map<String, Palette> paletteCache;
    private final Map<String, Texture> cachedPaletteTemplatesTextures;

    public CachedPaletteService(TextureLoader loader, PaletteFactory factory) {
        this.loader = loader;
        this.factory = factory;
        paletteCache = new HashMap<>();
        cachedPaletteTemplatesTextures = new HashMap<>();
    }

    @Override
    public void clearCache() {
        paletteCache.clear();
        cachedPaletteTemplatesTextures.clear();
    }

    @Override
    public Palette getPalette(PaletteIdentifier id) {
        if (paletteCache.containsKey(id.getIdentifier())) {
            return paletteCache.get(id.getIdentifier());
        } else {
            return createPalette(id);
        }
    }

    private Palette createPalette(PaletteIdentifier id) {
        if (PaletteResourceRegistry.getInstance().premadePalette(id)) {
            return factory.createPalette(loader.getResource(id).getImage(), id);
        } else {
            return generatePalette(id);
        }

    }

    Pair<List<Texture>, List<Texture>> getPalettesFromMaterial(String material) {
        PaletteResourceIdentifier paletteIdentifier;
        try {
            paletteIdentifier = PaletteResourceRegistry.getInstance().getPalette(material).orElseThrow();
        } catch (NoSuchElementException e) {
            ForgeroInitializer.LOGGER.error("Unable to find Palette {} in palette registry, you have probably forgotten to add it", material);
            throw e;
        }
        List<Texture> inclusions = getPalettes(paletteIdentifier.getPaletteIdentifiers().stream().map(identifier -> new PaletteTemplateIdentifier(identifier.getResource().replace(".png", ""))).collect(Collectors.toList()));
        List<Texture> exclusions = getPalettes(paletteIdentifier.getPaletteExclusionIdentifiers().stream().map(identifier -> new PaletteTemplateIdentifier(identifier.getResource().replace(".png", ""))).collect(Collectors.toList()));
        return new Pair<>(inclusions, exclusions);
    }

    private Palette generatePalette(PaletteIdentifier id) {
        try {
            Pair<List<Texture>, List<Texture>> reference = getPalettesFromMaterial(id.material());
            UnbakedPalette unbakedPalette = new UnbakedMaterialPalette(id, reference.getLeft(), reference.getRight());
            Palette palette = factory.createPalette(unbakedPalette);
            paletteCache.put(id.getIdentifier(), palette);
            //exportPalette(palette, id);
            return palette;

        } catch (Exception e) {
            return ArrayList::new;
        }
    }

    private void exportPalette(Palette palette, PaletteIdentifier id) {
        var image = new BufferedImage(palette.getColourValues().size(), 1, BufferedImage.TYPE_INT_ARGB);
        IntStream.range(0, palette.getColourValues().size() - 1)
                .forEach(i -> image.setRGB(i, 0, palette.getColourValues().get(i).getRgb()));

        try {

            File outputFile = new File(String.format("./%s.png", id.getIdentifier()));
            if (!outputFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                outputFile.createNewFile();
            }
            ImageIO.write(image, "png", outputFile);


        } catch (IOException e) {
            ForgeroInitializer.LOGGER.error(e);
        }
    }


    private List<Texture> getPalettes(List<TextureIdentifier> templatePalettes) {
        List<Texture> paletteTemplates = new ArrayList<>();
        for (TextureIdentifier template : templatePalettes) {
            if (cachedPaletteTemplatesTextures.containsKey(template.getIdentifier())) {
                paletteTemplates.add(cachedPaletteTemplatesTextures.get(template.getIdentifier()));
            } else {
                Texture rawPalette = loader.getResource(template);
                paletteTemplates.add(rawPalette);
                cachedPaletteTemplatesTextures.put(template.getIdentifier(), rawPalette);
            }
        }
        return paletteTemplates;
    }
}
