package com.sigmundgranaas.forgero.client.forgerotool.model.toolpart;

import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.core.gem.Gem;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.function.Function;

public class Gem2DModel extends Unbaked2DToolPartModel {
    private final ToolPartModelType type;
    private final Gem gem;

    public Gem2DModel(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, Gem gem, ToolPartModelType modelType) {
        super(loader, textureGetter);
        this.type = modelType;
        this.gem = gem;
    }

    @Override
    public String getIdentifier() {
        return type.toFileName() + "_gem";
    }


    @Override
    public int getModelLayerIndex() {
        return 4;
    }
}
