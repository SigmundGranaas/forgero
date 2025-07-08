package com.sigmundgranaas.forgero.smithing.util;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class RuntimeModelUtil {
    private static final Logger LOGGER = LogManager.getLogger("ForgeroRuntimeModelUtil");

    /**
     * Attempts to extract the first quad's texture as a BufferedImage from the given ItemStack.
     * Falls back to the particle sprite if no quads are found.
     * @param itemStack the item stack
     * @param client the Minecraft client
     * @return BufferedImage of the texture, or null if not found or error
     */
    public static BufferedImage getFirstQuadTextureImage(ItemStack itemStack, MinecraftClient client) {
        try {
            BakedModel model = client.getItemRenderer().getModel(itemStack, null, null, 0);
            List<?> quads = model.getQuads(null, null, client.world.getRandom());
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
            LOGGER.debug("[RuntimeModelUtil] Using texture resource: {} for itemStack: {}", resourceId, itemStack);
            try (InputStream stream = client.getResourceManager().getResource(resourceId).get().getInputStream()) {
                return ImageIO.read(stream);
            }
        } catch (Exception e) {
            LOGGER.error("[RuntimeModelUtil] Error extracting quad texture image: ", e);
            return null;
        }
    }
}
