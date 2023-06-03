package com.sigmundgranaas.forgero.minecraft.common.block.upgradestation;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;


public class UpgradeStationScreen extends HandledScreen<UpgradeStationScreenHandler> {
	private static final Identifier TEXTURE = new Identifier("forgero", "textures/gui/container/upgrade_table_ui.png");
	private static final Identifier textureId = new Identifier("minecraft", "textures/gui/advancements/widgets.png");
	private final ItemRenderer itemRenderer;
	private int tickCounter;

	public UpgradeStationScreen(UpgradeStationScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, Text.translatable("block.forgero.assembly_station"));
		this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
	}


	public void renderCustomTooltip(MatrixStack matrices, List<Text> lines, int mouseX, int mouseY) {
		for (UpgradeStationScreenHandler.PositionedSlot slot : this.handler.slots.stream().filter(UpgradeStationScreenHandler.PositionedSlot.class::isInstance).map(UpgradeStationScreenHandler.PositionedSlot.class::cast).toList()) {
			if (isPointWithinBounds(slot.x, slot.y, 16, 16, mouseX, mouseY) && !slot.hasStack()) {
				lines.add(Text.literal("This slot accepts...")); // Here you need to add the description based on the slot
				renderCycledSlot(slot, lines, mouseX, mouseY);
			}
		}
	}

	public void renderCycledSlot(UpgradeStationScreenHandler.PositionedSlot slot, List<Text> tooltip, int mouseX, int mouseY) {
		if (slot.getSlot() != null) {
			com.sigmundgranaas.forgero.core.state.Slot slot1 = slot.getSlot();

			Identifier id = new Identifier("forgero", slot1.typeName().toLowerCase());

			TagKey<Item> key = TagKey.of(Registry.ITEM_KEY, id);
			List<RegistryEntry<Item>> entries = new ArrayList<>();
			// List of items that this slot accepts
			Registry.ITEM.iterateEntries(key).forEach(entries::add);
			List<Item> acceptedItems = entries.stream().map(RegistryEntry::value).toList();

			// Select an item to display based on the current game tick
			Item itemToShow = acceptedItems.get((this.tickCounter / 40) % acceptedItems.size());

			this.itemRenderer.renderInGuiWithOverrides(itemToShow.getDefaultStack(), mouseX, mouseY);
		}
	}


	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);

		for (Slot slot : handler.slots) {
			// Render your slots here
			if (slot instanceof UpgradeStationScreenHandler.PositionedSlot slot1) {
				this.drawSlot(matrices, slot1);
			}
		}

	}

	private void drawSlot(MatrixStack matrices, UpgradeStationScreenHandler.PositionedSlot slot) {
		// You'll want to render your slot here. I'll use default slot rendering as an example,
		// but you might have your own custom slot rendering.

		int x = slot.x - 1;
		int y = slot.y - 1;

		int centerX = (this.width - this.backgroundWidth) / 2;
		int centerY = (this.height - this.backgroundHeight) / 2;
		RenderSystem.setShaderTexture(0, TEXTURE);

		this.drawTexture(matrices, centerX + x, centerY + y, 194, 55, 18, 18);

		if (!slot.hasStack()) {
			ItemStack stack = new ItemStack(Items.IRON_INGOT);
			Sprite sprite = itemRenderer.getModels().getModel(stack).getParticleSprite();

			//RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
			//drawSprite(matrices, centerX + x + 1, centerY + y + 1, this.getZOffset(), 16, 16, sprite);
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		this.tickCounter++;

		MinecraftClient.getInstance().getTextureManager().bindTexture(textureId);

		for (Slot slot : handler.slots) {
			if (!(slot instanceof UpgradeStationScreenHandler.PositionedSlot positionedSlot)) {
				continue;
			}
			for (Slot otherSlot : handler.slots) {
				if (!(otherSlot instanceof UpgradeStationScreenHandler.PositionedSlot otherPositionedSlot)) {
					continue;
				}

				// Draw a line if the slots are on subsequent layers
				if (Math.abs(positionedSlot.yPosition - otherPositionedSlot.yPosition) == handler.verticalSpacing
						&& Math.abs(positionedSlot.xPosition - otherPositionedSlot.xPosition) < handler.horizontalSpacing) {

					Vector2f startPos = new Vector2f(positionedSlot.xPosition + 8, positionedSlot.yPosition + 8);
					Vector2f endPos = new Vector2f(otherPositionedSlot.xPosition + 8, otherPositionedSlot.yPosition + 8);
					float distance = (float) Math.sqrt(Math.pow(endPos.getX() - startPos.getX(), 2) + Math.pow(endPos.getY() - startPos.getY(), 2));
					float angle = (float) Math.atan2(endPos.getY() - startPos.getY(), endPos.getX() - startPos.getX());

					for (float i = 0; i < 1; i += 1.0f / distance) {
						Vector2f pos = new Vector2f(
								startPos.getX() + (endPos.getX() - startPos.getX()) * i,
								startPos.getY() + (endPos.getY() - startPos.getY()) * i
						);

						matrices.push();
						matrices.translate(pos.getX(), pos.getY(), 0);
						matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(angle * (180F / (float) Math.PI)));

						DrawableHelper.drawTexture(
								matrices,
								0, 0,
								0, 0,
								16, 16,
								256, 256
						);

						matrices.pop();
					}
				}
			}
		}
		drawMouseoverTooltip(matrices, mouseX, mouseY);
		renderCustomTooltip(matrices, new ArrayList<>(), mouseX, mouseY);
	}


	@Override
	protected void init() {
		super.init();
		// Center the title
		titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
	}

}
