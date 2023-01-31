package com.sigmundgranaas.forgero.client.stonegolem;

import com.sigmundgranaas.forgero.creatures.stonegolem.StoneGolemEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class StoneGolemEntityModel extends AnimatedGeoModel<StoneGolemEntity> {
    private static final Identifier modelResource = new Identifier("forgero", "geo/stone_golem.geo.json");
    private static final Identifier textureResource = new Identifier("forgero", "textures/entity/stone_golem.png");
    private static final Identifier animationResource = new Identifier("forgero", "animations/stone_golem.animation.json");

    @Override
    public Identifier getModelResource(StoneGolemEntity object) {
        return modelResource;
    }

    @Override
    public Identifier getTextureResource(StoneGolemEntity object) {
        return textureResource;
    }

    @Override
    public Identifier getAnimationResource(StoneGolemEntity animatable) {
        return animationResource;
    }
}