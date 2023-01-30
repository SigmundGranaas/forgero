package com.sigmundgranaas.forgero.client.kuruk;

import com.sigmundgranaas.forgero.creatures.kuruk.KurukEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;


public class KurukEntityRenderer extends GeoEntityRenderer<KurukEntity> {

    public KurukEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new KurukEntityModel());
    }
}
