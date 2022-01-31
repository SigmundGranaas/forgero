package com.sigmundgranaas.forgero.core.texture.template;

import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.TemplateTextureIdentifier;

public interface TemplateTextureService {
    TemplateTexture getTemplate(TemplateTextureIdentifier id);
}
