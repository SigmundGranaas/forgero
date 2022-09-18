package com.sigmundgranaas.forgero.client.forgerotool.model.toolpart;

import com.sigmundgranaas.forgero.identifier.texture.toolpart.ToolPartModelTextureIdentifier;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

import java.util.function.Function;

public class Basic2DToolPartModel extends Unbaked2DToolPartModel {
    private final ToolPartModelTextureIdentifier identifier;

    public Basic2DToolPartModel(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ToolPartModelTextureIdentifier identifier) {
        super(loader, textureGetter);
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier.getIdentifier();
    }

    @Override
    public String getTextureIdentifier() {
        return identifier.getIdentifier();
    }

    @Override
    public int getModelLayerIndex() {
        return switch (identifier.getTemplateTextureIdentifier().getToolPartModelLayer()) {
            case PRIMARY -> 0;
            case SECONDARY -> 1;
            case GEM -> 2;
            default -> 0;
        };
    }
}
