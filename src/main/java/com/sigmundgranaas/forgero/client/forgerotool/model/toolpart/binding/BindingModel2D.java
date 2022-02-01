package com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.binding;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.Unbaked2DToolPartModel;
import com.sigmundgranaas.forgero.core.tool.toolpart.binding.ToolPartBinding;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.function.Function;

public class BindingModel2D extends Unbaked2DToolPartModel {
    private final ToolPartModelType type;
    private final ToolPartBinding part;

    public BindingModel2D(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ToolPartBinding toolPart, ToolPartModelType type) {
        super(loader, textureGetter);
        this.type = type;
        this.part = toolPart;
    }

    @Override
    public String getIdentifier() {
        return part.getPrimaryMaterial().getName() + "_" + type.toFileName();
    }

    @Override
    public int getModelLayerIndex() {
        return 2;
    }
}
