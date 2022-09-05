package com.sigmundgranaas.forgerocore.texture.template;

import com.sigmundgranaas.forgerocore.identifier.texture.toolpart.TemplateTextureIdentifier;
import com.sigmundgranaas.forgerocore.texture.Texture;
import com.sigmundgranaas.forgerocore.texture.TextureLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * Cached version of the Template Texture Service
 * <p>
 * This service will fetch Template textures from file or from its internal cache.
 */
public class CachedTemplateTextureService implements TemplateTextureService {
    private final TextureLoader loader;
    private final Map<String, TemplateTexture> templateCache;
    private final TemplateTextureFactory factory;

    public CachedTemplateTextureService(TextureLoader loader, TemplateTextureFactory factory) {
        templateCache = new HashMap<>();
        this.factory = factory;
        this.loader = loader;
    }

    @Override
    public TemplateTexture getTemplate(TemplateTextureIdentifier id) {
        if (this.templateCache.containsKey(id.getIdentifier())) {
            return templateCache.get(id.getIdentifier());
        } else {
            return createTemplateTexture(id);
        }
    }

    public void clearCache() {
        templateCache.clear();
    }

    private TemplateTexture createTemplateTexture(TemplateTextureIdentifier id) {
        Texture RawTemplate = loader.getResource(id);
        TemplateTexture template = factory.createTemplateTexture(RawTemplate, id);
        templateCache.put(id.getIdentifier(), template);
        return template;

    }
}
