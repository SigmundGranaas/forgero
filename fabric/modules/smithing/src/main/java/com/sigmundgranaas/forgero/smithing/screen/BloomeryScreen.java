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
        int fuelTime = handler.getFuelProgress();
        int fuelTotal = handler.getMaxFuelProgress();
        if (fuelTotal <= 0) return;
        
        // Vanilla furnace uses a 14-pixel tall flame texture
        int fuelHeight = 14;
        // Calculate the height of the filled portion of the fuel bar
        int filledHeight = (fuelTime * fuelHeight) / fuelTotal;
        
        // Ensure at least 1 pixel is shown if there's any fuel
        if (fuelTime > 0 && filledHeight == 0) {
            filledHeight = 1;
        } else if (fuelTime <= 0) {
            return; // Don't render if no fuel
        }
        
        // Adjust for the flame texture's position in the texture file
        int vOffset = fuelHeight - filledHeight;
        
        // Draw the fuel bar (vanilla flame texture is at u=176, v=0 with size 14x14)
        // The y position is adjusted to draw from bottom to top
        context.drawTexture(TEXTURE, 
            x + 32, // x position of the fuel bar
            y + 36 + vOffset, // y position (adjusted for height)
            176, // u (texture x)
            14 + vOffset, // v (texture y, adjusted for height)
            14, // width
            filledHeight // height of the filled portion
        );
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
