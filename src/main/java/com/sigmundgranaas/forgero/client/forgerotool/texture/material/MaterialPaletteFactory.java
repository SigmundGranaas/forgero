package com.sigmundgranaas.forgero.client.forgerotool.texture.material;

import com.sigmundgranaas.forgero.client.ClientConfiguration;
import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureIdentifier;
import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureManager.Function;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.factory.MaterialPaletteFactoryImpl;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.factory.OverridableMaterialPaletteFactory;
import com.sigmundgranaas.forgero.client.forgerotool.texture.material.factory.RecreateMaterialPaletteFactory;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.util.Optional;

public interface MaterialPaletteFactory {
    static MaterialPaletteFactory createMaterialPaletteFactory() {
        if (ClientConfiguration.INSTANCE.shouldCreateNewPalettes()) {
            if (ClientConfiguration.INSTANCE.shouldOverWriteOldPalettes()) {
                return new OverridableMaterialPaletteFactory();
            } else {
                return new RecreateMaterialPaletteFactory();
            }
        } else {
            return new MaterialPaletteFactoryImpl();
        }
    }


    public Optional<MaterialPalette> getPalette(Function<Identifier, Resource> getResource, ForgeroTextureIdentifier identifier);

    public void clearCache();
}
