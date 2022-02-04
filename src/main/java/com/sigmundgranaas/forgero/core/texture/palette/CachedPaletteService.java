package com.sigmundgranaas.forgero.core.texture.palette;

import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteTemplateIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.texture.Texture;
import com.sigmundgranaas.forgero.core.texture.TextureLoader;

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
        try {
            List<Texture> inclusions = getPalettes(MaterialCollection.INSTANCE.getMaterialsAsMap().get(id.getIdentifier()).getPaletteIdentifiers().stream().map(identifier -> new PaletteTemplateIdentifier(identifier.getResource().replace(".png", ""))).collect(Collectors.toList()));
            List<Texture> exclusions = getPalettes(MaterialCollection.INSTANCE.getMaterialsAsMap().get(id.getIdentifier()).getPaletteExclusionIdentifiers().stream().map(identifier -> new PaletteTemplateIdentifier(identifier.getResource().replace(".png", ""))).collect(Collectors.toList()));
            UnbakedPalette unbakedPalette = new UnbakedMaterialPalette(id, inclusions, exclusions);
            Palette palette = factory.createPalette(unbakedPalette);
            paletteCache.put(id.getIdentifier(), palette);
            return palette;
        } catch (IOException | URISyntaxException e) {
            //TODO Proper exception handling
            throw new IllegalArgumentException();
        }
    }


    private List<Texture> getPalettes(List<TextureIdentifier> templatePalettes) throws IOException, URISyntaxException {
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
