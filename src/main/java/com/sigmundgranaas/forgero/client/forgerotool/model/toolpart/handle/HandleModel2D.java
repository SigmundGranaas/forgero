package com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.handle;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.Unbaked2DToolPartModel;
import com.sigmundgranaas.forgero.core.tool.toolpart.handle.ToolPartHandle;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.function.Function;

public class HandleModel2D extends Unbaked2DToolPartModel {
    private final ToolPartModelType type;
    private final ToolPartHandle handle;

    public HandleModel2D(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ToolPartHandle handle, ToolPartModelType type) {
        super(loader, textureGetter);
        this.type = type;
        this.handle = handle;
    }

    @Override
    public String getIdentifier() {
        return handle.getPrimaryMaterial().getName() + "_" + type.toFileName();
    }

    @Override
    public int getModelLayerIndex() {
        return 0;
    }
}
