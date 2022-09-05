package com.sigmundgranaas.forgerocore.texture.template;

import com.sigmundgranaas.forgerocore.identifier.texture.toolpart.TemplateTextureIdentifier;

/**
 * Service for either fetching or generating a TemplateTexture.
 * <p>
 * I don't care how it's done, just give me a template.
 */
public interface TemplateTextureService {
    TemplateTexture getTemplate(TemplateTextureIdentifier id);

    void clearCache();
}
