package com.sigmundgranaas.forgero.texture;

import com.sigmundgranaas.forgero.deprecated.ToolPartModelType;
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