package com.sigmundgranaas.forgero.smithing.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BloomeryScreen extends HandledScreen<BloomeryScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("forgero", "textures/gui/container/bloomery.png");

    public BloomeryScreen(BloomeryScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        renderFuelIndicator(context, x, y);
        renderProgressArrow(context, x, y);
    }

    private void renderProgressArrow(DrawContext context, int x, int y) {
        if (handler.isSmelting()) {
            int progress = handler.getProgress();
            int maxProgress = handler.getMaxProgress();
            int arrowWidth = 24; // Standard furnace arrow width
            int drawWidth = (maxProgress > 0) ? (progress * arrowWidth) / maxProgress : 0;
            if (drawWidth > 0) {
                // Texture coordinates: (u, v) = (176, 0), size = (drawWidth, 16)
                context.drawTexture(TEXTURE, x + 79, y + 35, 176, 0, drawWidth, 16);
            }
        }
    }

    private void renderFuelIndicator(DrawContext context, int x, int y) {
        int maxFuel = handler.getMaxFuelProgress();
        int currentFuel = handler.getFuelProgress();
        int fuelHeight = 14;
        int fuelBarHeight = (maxFuel > 0) ? (currentFuel * fuelHeight) / maxFuel : 0;
        
        // Always show at least 1 pixel of the fuel bar
        if (fuelBarHeight <= 0 && currentFuel > 0) {
            fuelBarHeight = 1;
        }
        
        if (fuelBarHeight > 0 && currentFuel > 0) {
            // Texture coordinates: (u, v) = (176, 14 + (fuelHeight - fuelBarHeight)), size = (14, fuelBarHeight)
            context.drawTexture(TEXTURE, x + 32, y + 36 + (fuelHeight - fuelBarHeight),
                    176, 17 + (fuelHeight - fuelBarHeight), 14, fuelBarHeight);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }
}
