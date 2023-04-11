package com.sigmundgranaas.forgero.client;


import com.sigmundgranaas.forgero.client.kuruk.KurukEntityRenderer;
import com.sigmundgranaas.forgero.client.stonegolem.StoneGolemEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;

import static com.sigmundgranaas.forgero.creatures.CreatureTypes.KURUK;
import static com.sigmundgranaas.forgero.creatures.CreatureTypes.STONE_GOLEM;

public class ForgeroCreaturesClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("geckolib3")) {
            EntityRendererRegistry.register(KURUK, KurukEntityRenderer::new);
            EntityRendererRegistry.register(STONE_GOLEM, StoneGolemEntityRenderer::new);
        }
    }
}
