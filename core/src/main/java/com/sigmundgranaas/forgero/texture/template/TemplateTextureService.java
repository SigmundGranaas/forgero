package com.sigmundgranaas.forgero.texture.template;

import com.sigmundgranaas.forgero.identifier.texture.TemplateIdentifier;
import com.sigmundgranaas.forgero.identifier.texture.toolpart.TemplateTextureIdentifier;

/**
 * Service for either fetching or generating a TemplateTexture.
 * <p>
 * I don't care how it's done, just give me a template.
 */
public interface TemplateTextureService {
    TemplateTexture getTemplate(TemplateTextureIdentifier id);

    TemplateTexture2 getTemplate(TemplateIdentifier id);

    void clearCache();
}
