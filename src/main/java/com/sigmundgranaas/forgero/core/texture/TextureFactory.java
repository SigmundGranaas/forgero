package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.texture.palette.Palette;
import com.sigmundgranaas.forgero.core.texture.palette.material.MaterialPalette;
import com.sigmundgranaas.forgero.core.texture.template.TemplateTexture;

import java.util.List;

public interface TextureFactory {
    Texture createTexture(MaterialPalette palette, TemplateTexture template);

    TemplateTexture createTemplate(TextureIdentifier id);

    Palette createPalette(List<PaletteIdentifier> inclusions, List<PaletteIdentifier> exclusions);
}
