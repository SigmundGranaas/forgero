package com.sigmundgranaas.forgerocore.texture;

import com.sigmundgranaas.forgerocore.deprecated.ToolPartModelType;
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