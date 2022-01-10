package com.sigmundgranaas.forgero.client.forgerotool.texture.material;

import com.sigmundgranaas.forgero.client.ClientConfiguration;
import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureManager.Function;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.factory.MaterialPaletteFactoryImpl;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.factory.OverridableMaterialPaletteFactory;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.factory.PersistMaterialPaletteFactory;
import com.sigmundgranaas.forgero.core.identifier.model.texture.TextureIdentifier;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.util.Optional;

/**
 * A factory for creating Palettes from Materials.
 * <p>
 * Will determine which type of factory is appropriate according the configuration settings of the mod.
 * Currently, this only decides if the templates should be loaded from files, persisted or overwritten.
 */
public interface MaterialPaletteFactory {

    /**
     * Factory method for creating the correct Material Palette factory.
     * This will be decided
     *
     * @return - An instance of the proper Material Factory
     */
    static MaterialPaletteFactory createMaterialPaletteFactory() {
        if (ClientConfiguration.INSTANCE.isDev()) {
            if (ClientConfiguration.INSTANCE.shouldOverWriteOldPalettes()) {
                return new OverridableMaterialPaletteFactory();
            } else {
                return new PersistMaterialPaletteFactory();
            }
        } else {
            return new MaterialPaletteFactoryImpl();
        }
    }


    /**
     * @param getResource A preloaded method for fetching existing Minecraft resources
     * @param identifier  - identifier to find the correct Material to create the palette from.
     * @return - Return an Optional in case the factory is unable to create a Palette.
     */
    Optional<MaterialPalette> getPalette(Function<Identifier, Resource> getResource, TextureIdentifier identifier);

    /**
     * Method for clearing the cache of the factory. This will force the factory to recreate or reload the palettes.
     */
    void clearCache();
}
