package com.sigmundgranaas.forgero.core.texture;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface TextureModelContainer {

    @NotNull
    TextureModel getModel(String id);

    @NotNull
    TextureModel getModel(ToolPartModelType id);

    @NotNull
    Collection<TextureModel> getModels();

    @NotNull
    TextureModel getModel();
}