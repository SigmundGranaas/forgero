package com.sigmundgranaas.forgero.client.texture;

import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.models.JModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.util.Identifier;

import static com.sigmundgranaas.forgero.client.ForgeroClient.TEXTURES;

public class Generator {
    public static final RuntimeResourcePack RESOURCE_PACK_CLIENT = RuntimeResourcePack.create("forgero:builtin");

    public static void generateModels() {
        TEXTURES.values().forEach(texture -> {
            Identifier id = new Identifier(texture.nameSpace(), "item/" + texture.name().replace(".png", ""));
            if (MinecraftClient.getInstance().getTextureManager() != null) {
                MinecraftClient.getInstance().getTextureManager().registerTexture(id, new ResourceTexture(id));
            }

            JModel model = new JModel();
            model.parent("item/generated");
            model.textures(JModel.textures().layer0(new Identifier(texture.nameSpace(), "item/" + texture.name().replace(".png", "")).toString()));
            RESOURCE_PACK_CLIENT.addModel(model, new Identifier(texture.nameSpace(), "item/" + texture.name().replace(".png", "")));
        });
    }
}
