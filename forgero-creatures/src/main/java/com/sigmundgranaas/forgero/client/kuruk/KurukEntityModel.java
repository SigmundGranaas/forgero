package com.sigmundgranaas.forgero.client.kuruk;

import com.sigmundgranaas.forgero.creatures.kuruk.KurukEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class KurukEntityModel extends AnimatedGeoModel<KurukEntity> {
    private static final Identifier modelResource = new Identifier("forgero", "geo/kuruk_elder.geo.json");
    private static final Identifier textureResource = new Identifier("forgero", "textures/entity/kuruk_elder.png");
    private static final Identifier animationResource = new Identifier("forgero", "animations/kuruk_elder.animation.json");

    @Override
    public Identifier getModelResource(KurukEntity object) {
        return modelResource;
    }

    @Override
    public Identifier getTextureResource(KurukEntity object) {
        return textureResource;
    }

    @Override
    public Identifier getAnimationResource(KurukEntity animatable) {
        return animationResource;
    }
}