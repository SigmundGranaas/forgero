package com.sigmundgranaas.forgero.client.forgerotool.texture.template.factory;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureIdentifier;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTextureFactory;
import com.sigmundgranaas.forgero.client.forgerotool.texture.utils.TextureLoader;
import com.sigmundgranaas.forgero.item.forgerotool.Constants;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Optional;

//TODO Need to clean the hierarchy of classes

/**
 *
 */
public class TemplateTextureFactoryImpl implements TemplateTextureFactory {

    protected final HashMap<ToolPartModelType, TemplateTexture> templateTexturesCache;

    public TemplateTextureFactoryImpl() {
        this.templateTexturesCache = new HashMap<>();
    }

    @Override
    public @NotNull
    Optional<TemplateTexture> getTexture(ForgeroTextureIdentifier textureIdentifier) {
        if (textureIdentifier.getModelType().isEmpty()) {
            return Optional.empty();
        }
        ToolPartModelType toolPartModelType = textureIdentifier.getModelType().get();

        Optional<TemplateTexture> baseTexture;

        if (templateTexturesCache.containsKey(toolPartModelType)) {
            baseTexture = Optional.of(templateTexturesCache.get(toolPartModelType));
        } else {
            baseTexture = getOrCreateTexture(textureIdentifier);
            baseTexture.ifPresent(templateTexture -> saveTemplateTexture(textureIdentifier, templateTexture));
        }
        return baseTexture;
    }

    protected Optional<TemplateTexture> getOrCreateTexture(ForgeroTextureIdentifier textureIdentifier) {
        Optional<BufferedImage> textureImageResult = loadTemplateFromFile(textureIdentifier);
        if (textureImageResult.isEmpty()) {
            return Optional.empty();
        }
        TemplateTexture baseTexture = TemplateTexture.createBaseTexture(textureImageResult.get());
        Optional<ToolPartModelType> modelType = textureIdentifier.getModelType();
        if (modelType.isPresent()) {
            templateTexturesCache.put(modelType.get(), baseTexture);
            return Optional.of(baseTexture);
        } else {
            return Optional.empty();
        }

    }

    protected void saveTemplateTexture(ForgeroTextureIdentifier textureIdentifier, TemplateTexture baseTexture) {
        textureIdentifier.getModelType().ifPresent(model -> templateTexturesCache.put(model, baseTexture));

    }

    protected Optional<BufferedImage> loadTemplateFromFile(ForgeroTextureIdentifier textureIdentifier) {
        return TextureLoader.loadTextureFromFile(getTemplateTextureFolder() + textureIdentifier.getBaseTextureFileNameWithExtension());
    }


    public @NotNull String getTemplateTextureFolder() {
        return Constants.CONFIG_PATH + "forgero/templates/textures/";
    }

    protected void WriteTemplateTexturePaletteAsImage(ForgeroTextureIdentifier textureIdentifier, TemplateTexture baseTexture) {
        BufferedImage paletteImage = new BufferedImage(baseTexture.getGreyScaleValues().size(), 1, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < baseTexture.getGreyScaleValues().size(); x++) {
            paletteImage.setRGB(x, 0, baseTexture.getGreyScaleValues().get(x).getRgb());
        }
        try {
            TextureLoader.saveTextureToFile(getTemplateTextureFolder() + "palette_" + textureIdentifier.getBaseTextureFileNameWithExtension(), paletteImage);
        } catch (Exception e) {
            Forgero.LOGGER.warn("Unable to save Template texture palette: {}", e, e);
        }
    }
}
