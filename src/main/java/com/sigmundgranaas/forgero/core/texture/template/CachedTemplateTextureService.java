package com.sigmundgranaas.forgero.core.texture.template;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.TemplateTextureIdentifier;
import com.sigmundgranaas.forgero.core.texture.Texture;
import com.sigmundgranaas.forgero.core.texture.TextureLoader;

import java.util.HashMap;
import java.util.Map;

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

    private TemplateTexture createTemplateTexture(TemplateTextureIdentifier id) {
        Texture RawTemplate = loader.getResource(id);
        TemplateTexture template = factory.createTemplateTexture(RawTemplate, id);
        templateCache.put(id.getIdentifier(), template);
        return template;

    }
}
