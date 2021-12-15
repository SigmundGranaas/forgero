package com.sigmundgranaas.forgero.client.forgerotool.texture.template;

import com.sigmundgranaas.forgero.client.ClientConfiguration;
import com.sigmundgranaas.forgero.client.forgerotool.texture.ForgeroTextureIdentifier;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.factory.OverridableTemplateTextureFactory;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.factory.RecreateTemplateTextureFactory;
import com.sigmundgranaas.forgero.client.forgerotool.texture.template.factory.TemplateTextureFactoryImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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

    @NotNull
    Optional<TemplateTexture> getTexture(ForgeroTextureIdentifier id);

}
