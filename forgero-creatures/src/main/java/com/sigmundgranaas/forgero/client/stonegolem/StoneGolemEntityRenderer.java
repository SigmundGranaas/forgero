package com.sigmundgranaas.forgero.client.stonegolem;

import com.sigmundgranaas.forgero.creatures.stonegolem.StoneGolemEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;


public class StoneGolemEntityRenderer extends GeoEntityRenderer<StoneGolemEntity> {

    public StoneGolemEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new StoneGolemEntityModel());
    }
}
