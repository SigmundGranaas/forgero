package com.sigmundgranaas.forgero.client.forgerotool.texture;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.ClientConfiguration;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPalette;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.MaterialPaletteFactory;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTexture;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.TemplateTextureFactory;
import com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart.ForgeroToolPartTexture;
import com.sigmundgranaas.forgero.client.forgerotool.texture.toolpart.ForgeroToolPartTextureFactory;
import com.sigmundgranaas.forgero.utils.ResourceUtils;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class ForgeroTextureManagerImpl implements ForgeroTextureManager {
    private static ForgeroTextureManagerImpl INSTANCE;
    private final TemplateTextureFactory baseTextureFactory;
    private final MaterialPaletteFactory paletteFactory;
    private final ForgeroToolPartTextureFactory textureFactory;

    private ForgeroTextureManagerImpl() {
        baseTextureFactory = TemplateTextureFactory.createFactory();
        paletteFactory = MaterialPaletteFactory.createMaterialPaletteFactory();
        textureFactory = ForgeroToolPartTextureFactory.createFactory();
    }

    public static ForgeroTextureManagerImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ForgeroTextureManagerImpl();
        }
        return INSTANCE;
    }

    @Override
    public boolean isManagedByForgeroTextureManager(Identifier resourceId) {
        if (ResourceUtils.isNotForgeroResource(resourceId) || !ResourceUtils.isTextureIdentifier(resourceId)) {
            return false;
        }

        ForgeroTextureIdentifierImpl textureIdentifier = new ForgeroTextureIdentifierImpl(resourceId);

        boolean fileExists = textureIdentifier.fileExists();
        if (fileExists && ClientConfiguration.INSTANCE.shouldCreateNewTextures()) {
            return true;
        } else if (!fileExists) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @NotNull
    public Resource getResource(Identifier resourceId, Function<Identifier, Resource> getResource) throws IOException {

        ForgeroTextureIdentifierImpl textureIdentifier = new ForgeroTextureIdentifierImpl(resourceId);

        Optional<TemplateTexture> baseTextureResult = baseTextureFactory.getTexture(textureIdentifier);

        Optional<MaterialPalette> paletteResult = paletteFactory.getPalette(getResource, textureIdentifier);

        if (baseTextureResult.isEmpty()) {
            Forgero.LOGGER.warn("Unable to create base texture for: {}", textureIdentifier);
        }
        if (paletteResult.isEmpty()) {
            Forgero.LOGGER.warn("Unable to get palette from material: {}", textureIdentifier.getMaterial());
        }

        if (baseTextureResult.isPresent() && paletteResult.isPresent()) {
            ForgeroToolPartTexture texture = textureFactory.createToolPartTexture(baseTextureResult.get(), paletteResult.get(), textureIdentifier);
            return new ResourceImpl(Forgero.MOD_NAMESPACE, resourceId, texture.getToolPartTexture(), null);
        } else {
            throw new IOException("Unable to create/find " + resourceId);
        }
    }
}
