package com.sigmundgranaas.forgero.texture.template;

import com.sigmundgranaas.forgero.identifier.texture.TemplateIdentifier;
import com.sigmundgranaas.forgero.identifier.texture.toolpart.TemplateTextureIdentifier;
import com.sigmundgranaas.forgero.texture.Texture;
import com.sigmundgranaas.forgero.texture.TextureLoader;

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
    private final Map<String, TemplateTexture2> templateCache2;
    private final TemplateTextureFactory factory;

    public CachedTemplateTextureService(TextureLoader loader, TemplateTextureFactory factory) {
        templateCache = new HashMap<>();
        templateCache2 = new HashMap<>();
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

    @Override
    public TemplateTexture2 getTemplate(TemplateIdentifier id) {
        if (this.templateCache2.containsKey(id.getIdentifier())) {
            return templateCache2.get(id.getIdentifier());
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

    private TemplateTexture2 createTemplateTexture(TemplateIdentifier id) {
        Texture RawTemplate = loader.getResource(id);
        TemplateTexture2 template = factory.createTemplateTexture(RawTemplate, id);
        templateCache2.put(id.getIdentifier(), template);
        return template;
    }
}
