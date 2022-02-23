package com.sigmundgranaas.forgero.core.texture.template;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.TemplateTextureIdentifier;

/**
 * Service for either fetching or generating a TemplateTexture.
 *
 * I don't care how it's done, just give me a template.
 */
public interface TemplateTextureService {
    TemplateTexture getTemplate(TemplateTextureIdentifier id);
}
