package com.sigmundgranaas.forgero.smithing.resource;

import java.util.Map;

import com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator;
import com.sigmundgranaas.forgero.core.texture.palette.Palette;
import com.sigmundgranaas.forgero.fabric.client.ForgeroClient;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

public class LiquidGeneratorResourceReloadMixin implements SimpleSynchronousResourceReloadListener {
    @Override
    public Identifier getFabricId() {
        return new Identifier("forgero", "liquid_generator");
    }

    @Override
    public void reload(ResourceManager manager) {
        // Use the same palette map as TextureGenerator
        var generator = TextureGenerator.getInstance(null, ForgeroClient.PALETTE_REMAP);
        // Suppress unchecked warning as V2.Palette implements the Palette interface
        @SuppressWarnings("unchecked")
        Map<String, Palette> palettes = (Map<String, Palette>) (Map<?, ?>) generator.getPaletteMap();
        try {
            LiquidGenerator.generateAndExportAllFluidFlowVariants(manager, palettes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
