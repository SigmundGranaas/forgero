package com.sigmundgranaas.forgero.smithing.util;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class RuntimeModelUtil {
    public static BufferedImage getFirstQuadTextureImage(ItemStack itemStack, MinecraftClient client) {
        if (itemStack.isEmpty() || itemStack.getItem() == Items.AIR) {
            return null;
        }
        try {
            BakedModel model = client.getItemRenderer().getModel(itemStack, null, null, 0);
            net.minecraft.util.math.random.Random random = (client.world != null) ? client.world.getRandom() : net.minecraft.util.math.random.Random.create();
            List<?> quads = model.getQuads(null, null, random);
            Sprite textureSprite = null;
            for (Object quadObj : quads) {
                var quad = (net.minecraft.client.render.model.BakedQuad) quadObj;
                Sprite quadSprite = quad.getSprite();
                if (textureSprite == null) {
                    textureSprite = quadSprite;
                }
            }
            if (textureSprite == null) {
                textureSprite = model.getParticleSprite();
            }
            Identifier spriteId = textureSprite.getContents().getId();
            Identifier resourceId = new Identifier(spriteId.getNamespace(), "textures/" + spriteId.getPath() + ".png");
            Optional<Resource> resourceOpt = client.getResourceManager().getResource(resourceId);
            if (resourceOpt.isPresent()) {
                try (InputStream stream = resourceOpt.get().getInputStream()) {
                    return ImageIO.read(stream);
                }
            } else {
                return null;
            }
        } catch (Exception e) {

            return null;
        }
    }
}
