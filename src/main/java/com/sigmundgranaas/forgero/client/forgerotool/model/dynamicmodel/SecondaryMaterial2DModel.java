package com.sigmundgranaas.forgero.client.forgerotool.model.dynamicmodel;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.function.Function;

public class SecondaryMaterial2DModel extends Unbaked2DToolPartModel {
    private final ToolPartModelType type;
    private final SecondaryMaterial secondaryMaterial;

    public SecondaryMaterial2DModel(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ForgeroToolPart toolPart, ToolPartModelType modelType) {
        super(loader, textureGetter);
        this.type = modelType;
        this.secondaryMaterial = toolPart.getSecondaryMaterial();
    }

    @Override
    public String getIdentifier() {
        return secondaryMaterial.getName() + "_" + type.toFileName() + "secondary";
    }

    @Override
    public int getModelLayerIndex() {
        return 4;
    }
}
