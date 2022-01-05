package com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.head;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel.Unbaked2DToolPartModel;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.function.Function;

public class HeadModel2D extends Unbaked2DToolPartModel {
    private final ToolPartHead head;

    public HeadModel2D(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ToolPartHead toolPart) {
        super(loader, textureGetter);
        this.head = toolPart;
    }

    @Override
    public String getIdentifier() {
        return head.getPrimaryMaterial().getName() + "_" + ToolPartModelType.getModelType(head).toFileName();
    }

    @Override
    public int getModelLayerIndex() {
        return 1;
    }
}
