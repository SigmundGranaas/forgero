package com.sigmundgranaas.forgero.core.texture.palette;

import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteTemplateIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.texture.Texture;
import com.sigmundgranaas.forgero.core.texture.TextureLoader;
import net.minecraft.util.Pair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        createGemPalettes();
    }

    private void createGemPalettes() {
        try {
            createPalette(new PaletteIdentifier("durability"), getPalettesFromGem("yellow_dye"));
            createPalette(new PaletteIdentifier("miningspeed"), getPalettesFromGem("blue_dye"));
            createPalette(new PaletteIdentifier("attackspeed"), getPalettesFromGem("purple_dye"));
            createPalette(new PaletteIdentifier("attackdamage"), getPalettesFromGem("red_dye"));
        } catch (Exception ignored) {

        }

    }

    @Override
    public Palette getPalette(PaletteIdentifier id) {
        if (paletteCache.containsKey(id.getIdentifier())) {
            return paletteCache.get(id.getIdentifier());
        } else {
            try {
                Pair<List<Texture>, List<Texture>> references = getPalettesFromMaterial(id.material());
                return createPalette(id, references);
            } catch (IOException | URISyntaxException e) {
                //TODO Proper exception handling
                throw new IllegalArgumentException();
            }
        }
    }

    Pair<List<Texture>, List<Texture>> getPalettesFromMaterial(String material) throws IOException, URISyntaxException {
        List<Texture> inclusions = getPalettes(MaterialCollection.INSTANCE.getMaterialsAsMap().get(material).getPaletteIdentifiers().stream().map(identifier -> new PaletteTemplateIdentifier(identifier.getResource().replace(".png", ""))).collect(Collectors.toList()));
        List<Texture> exclusions = getPalettes(MaterialCollection.INSTANCE.getMaterialsAsMap().get(material).getPaletteExclusionIdentifiers().stream().map(identifier -> new PaletteTemplateIdentifier(identifier.getResource().replace(".png", ""))).collect(Collectors.toList()));
        return new Pair<>(inclusions, exclusions);
    }

    Pair<List<Texture>, List<Texture>> getPalettesFromGem(String gem) throws IOException, URISyntaxException {
        List<Texture> inclusions = getPalettes(List.of(new PaletteTemplateIdentifier("minecraft:textures/item/" + gem)));
        List<Texture> exclusions = getPalettes(List.of());
        return new Pair<>(inclusions, exclusions);
    }


    private Palette createPalette(PaletteIdentifier id, Pair<List<Texture>, List<Texture>> reference) {
        UnbakedPalette unbakedPalette = new UnbakedMaterialPalette(id, reference.getLeft(), reference.getRight());
        Palette palette = factory.createPalette(unbakedPalette);
        paletteCache.put(id.getIdentifier(), palette);
        return palette;

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
