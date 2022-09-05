package com.sigmundgranaas.forgero.client.texture;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgerocore.identifier.texture.TextureIdentifier;
import com.sigmundgranaas.forgerocore.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgerocore.identifier.texture.toolpart.PaletteTemplateIdentifier;
import com.sigmundgranaas.forgero.resources.FileService;
import com.sigmundgranaas.forgerocore.texture.RawTexture;
import com.sigmundgranaas.forgerocore.texture.Texture;
import com.sigmundgranaas.forgerocore.texture.TextureLoader;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Class for loading textures from filesystem.
 * This class is used to load template textures and palettes as well as a method for fetching Minecraft's assets.
 */
public class FabricTextureLoader implements TextureLoader {
    private final Function<Identifier, Optional<Resource>> getResource;
    private final FileService fileService;

    public FabricTextureLoader(Function<Identifier, Optional<Resource>> getResource) {
        this.getResource = getResource;
        this.fileService = new FileService();
    }

    @Override
    public Texture getResource(TextureIdentifier id) {
        try {
            if (id instanceof PaletteTemplateIdentifier) {
                return RawTexture.createRawTexture(id, getResource.apply((new Identifier(id.getFileNameWithExtension()))).get().getInputStream());
            } else {
                return RawTexture.createRawTexture(id, fileService.getStream(id));
            }
        } catch (IOException | NoSuchElementException | IllegalArgumentException e) {
            ForgeroInitializer.LOGGER.error("Unable to load {} due to {}, Falling back to default image", id.getFileNameWithExtension(), e);
            return new RawTexture(id, new BufferedImage(32, 32, BufferedImage.TYPE_INT_BGR));
        }
    }

    @Override
    public Texture getResource(PaletteIdentifier id) {
        try {
            return RawTexture.createRawTexture(id, (fileService.getStream(id)));
        } catch (IOException e) {
            ForgeroInitializer.LOGGER.error("Unable to load {} palette due to {}, Falling back to default image", id.getIdentifier(), e);
            return new RawTexture(id, new BufferedImage(32, 32, BufferedImage.TYPE_INT_BGR));
        }
    }

    @FunctionalInterface
    public interface Function<T, K> {
        K apply(T id) throws IOException;
    }
}
