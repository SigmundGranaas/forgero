package com.sigmundgranaas.forgero.client.forgerotool.texture.template;

import com.sigmundgranaas.forgero.client.ClientConfiguration;
import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureIdentifier;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.factory.OverridableTemplateTextureFactory;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.factory.RecreateTemplateTextureFactory;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.factory.TemplateTextureFactoryImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A factory for creating TemplateTextures.
 * <p>
 * Will determine which type of factory is appropriate according the configuration settings of the mod.
 * Currently, this only decides if the templates should be loaded from files, persisted or overwritten.
 */
public interface TemplateTextureFactory {
    static TemplateTextureFactory createFactory() {
        if (ClientConfiguration.INSTANCE.shouldCreateNewPalettes()) {
            if (ClientConfiguration.INSTANCE.shouldOverWriteOldPalettes()) {
                return new OverridableTemplateTextureFactory();
            } else {
                return new RecreateTemplateTextureFactory();
            }
        } else {
            return new TemplateTextureFactoryImpl();
        }
    }

    /**
     * Will use the FactoryImplamentation for generating a TemplateTexture Object from a base texture
     *
     * @param id - identifier which will tell the factory which base texture to use
     * @return - Returns an Optional in case the Template texture could not be created.
     */
    @NotNull
    Optional<TemplateTexture> getTexture(ForgeroTextureIdentifier id);

}
