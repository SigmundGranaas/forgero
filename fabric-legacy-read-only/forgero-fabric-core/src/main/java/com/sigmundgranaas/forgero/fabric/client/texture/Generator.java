package com.sigmundgranaas.forgero.fabric.client.texture;

import com.sigmundgranaas.forgero.core.model.ModelTemplate;
import com.sigmundgranaas.forgero.core.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.core.model.TextureModel;
import com.sigmundgranaas.forgero.drp.api.DRPApi;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.lifecycle.ResourcePackPhase;
import com.sigmundgranaas.forgero.drp.api.texture.AtlasBuilder;
import net.minecraft.util.Identifier;

import static com.sigmundgranaas.forgero.fabric.client.ForgeroClient.TEXTURES;


public class Generator {
    private static DynamicResourcePack clientPack;

    public static DynamicResourcePack getClientPack() {
        if (clientPack == null) {
            clientPack = DRPApi.getInstance()
                    .createPack("forgero:builtin_client")
                    .description("Forgero client-side generated resources")
                    .build();
        }
        return clientPack;
    }

    public static void generate() {
        new Generator().generateModels();
        DRPApi.getInstance().register(getClientPack(), ResourcePackPhase.BEFORE_VANILLA);
    }

    public void generateModels() {
        AtlasBuilder atlasBuilder = AtlasBuilder.create();

        TEXTURES.values().stream()
                .map(this::texture)
                .distinct()
                .forEach(atlasBuilder::addSingle);

        getClientPack().addAtlas(new Identifier("minecraft", "blocks"), atlasBuilder);
    }

    private Identifier texture(ModelTemplate texture) {
        if (texture instanceof PaletteTemplateModel paletteTemplateModel) {
            return new Identifier(paletteTemplateModel.nameSpace(), "item/" + paletteTemplateModel.name().replace(".png", ""));
        } else if (texture instanceof TextureModel model) {
            return new Identifier(model.nameSpace(), "item/" + model.name().replace(".png", ""));
        }
        throw new RuntimeException("Unknown texture type");
    }
}
