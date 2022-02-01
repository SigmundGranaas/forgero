package com.sigmundgranaas.forgero.client.forgerotool.model.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticSecondaryMaterial;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.function.Function;

public class SecondaryMaterial2DModel extends Unbaked2DToolPartModel {
    private final ToolPartModelType type;
    private final RealisticSecondaryMaterial secondaryMaterial;

    public SecondaryMaterial2DModel(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, RealisticSecondaryMaterial material, ToolPartModelType modelType) {
        super(loader, textureGetter);
        this.type = modelType;
        this.secondaryMaterial = material;
    }

    @Override
    public String getIdentifier() {
        return secondaryMaterial.getName() + "_" + type.toFileName() + "_secondary";
    }

    @Override
    public int getModelLayerIndex() {
        return 4;
    }
}
