package com.sigmundgranaas.forgero.smithing.networking.S2C;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

public class SchematicSelectionS2CPacket {
	@Environment(EnvType.CLIENT)
	public static void receive(net.minecraft.client.MinecraftClient client, net.minecraft.client.network.ClientPlayNetworkHandler handler, net.minecraft.network.PacketByteBuf buf, PacketSender responseSender) {
		BlockPos pos = buf.readBlockPos();
		int count = buf.readInt();
		List<Identifier> options = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			options.add(buf.readIdentifier());
		}
		client.execute(() -> {
			if (client.player == null) return;
			client.setScreen(new SimpleSchematicSelectionScreen(pos, options));
		});
	}

	@Environment(EnvType.CLIENT)
	private static class SimpleSchematicSelectionScreen extends Screen {
		private final BlockPos anvilPos;
		private final List<Identifier> options;

		// Scroll state and layout
		private final List<ButtonWidget> optionButtons = new ArrayList<>();
		private int buttonWidth, buttonHeight, spacing;
		private int x, startY, viewportHeight, contentHeight, maxScroll, trackX, trackW;
		private boolean scrollable, draggingScrollbar;
		private float scrollOffset;
		private ButtonWidget cancelButton; // render after clipping

		protected SimpleSchematicSelectionScreen(BlockPos anvilPos, List<Identifier> options) {
			super(Text.literal("Select mold"));
			this.anvilPos = anvilPos;
			this.options = options;
		}

		@Override
		protected void init() {
			super.init();
			if (this.client == null) return;

			// Layout
			this.buttonWidth = Math.min(220, this.width - 40);
			this.buttonHeight = 20;
			this.spacing = 4;
			this.x = (this.width - buttonWidth) / 2;

			this.contentHeight = Math.max(0, options.size() * (buttonHeight + spacing) - spacing);
			this.scrollable = options.size() > 3;
			this.viewportHeight = scrollable ? (3 * (buttonHeight + spacing) - spacing) : contentHeight;

			// Center the viewport area; reserve some extra space for title/cancel
			this.startY = Math.max(40, (this.height - (viewportHeight + 30)) / 2);

			this.maxScroll = Math.max(0, contentHeight - viewportHeight);
			this.trackW = 6;
			this.trackX = this.x + this.buttonWidth + 4;

			// Create option buttons (store and manage positions/visibility via scroll)
			this.optionButtons.clear();
			for (int i = 0; i < options.size(); i++) {
				Identifier id = options.get(i);
				Text label = Text.literal(id.getPath().replace('_', ' '));
				ButtonWidget btn = ButtonWidget.builder(label, b -> {
					var data = PacketByteBufs.create();
					data.writeBlockPos(anvilPos);
					data.writeIdentifier(id);
					ClientPlayNetworking.send(com.sigmundgranaas.forgero.smithing.networking.ModMessages.SCHEMATIC_SELECTED, data);
					close();
				}).dimensions(this.x, 0, this.buttonWidth, this.buttonHeight).build();
				this.optionButtons.add(this.addDrawableChild(btn));
			}
			updateOptionButtons();

			// Cancel button stays below the viewport
			this.cancelButton = this.addDrawableChild(
					ButtonWidget.builder(Text.literal("Cancel"), btn -> close())
							.dimensions(this.x, this.startY + this.viewportHeight + 8, this.buttonWidth, this.buttonHeight)
							.build()
			);
		}

		private void updateOptionButtons() {
			for (int i = 0; i < optionButtons.size(); i++) {
				ButtonWidget btn = optionButtons.get(i);
				int y = this.startY + i * (this.buttonHeight + this.spacing) - (int)this.scrollOffset;
				btn.setX(this.x);
				btn.setY(y);
				boolean visible = y + this.buttonHeight > this.startY && y < this.startY + this.viewportHeight;
				btn.visible = !this.scrollable || visible;
				btn.active = btn.visible;
			}
		}

		private int getThumbHeight() {
			if (!scrollable || contentHeight <= 0) return 0;
			int min = 12;
			return Math.max(min, (int)((float)this.viewportHeight * this.viewportHeight / (float)this.contentHeight));
		}

		private int getThumbY() {
			if (!scrollable || maxScroll <= 0) return this.startY;
			int thumbH = getThumbHeight();
			int trackH = this.viewportHeight - thumbH;
			return this.startY + (int)((this.scrollOffset / (float)this.maxScroll) * trackH);
		}

		private void setScrollFromMouse(double mouseY) {
			int thumbH = getThumbHeight();
			int trackH = Math.max(1, this.viewportHeight - thumbH);
			double rel = MathHelper.clamp(mouseY - this.startY - thumbH / 2.0, 0.0, trackH);
			this.scrollOffset = (float)(rel / trackH) * this.maxScroll;
			updateOptionButtons();
		}

		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
			if (this.scrollable) {
				boolean overList = mouseX >= this.x && mouseX <= this.x + this.buttonWidth
						&& mouseY >= this.startY && mouseY <= this.startY + this.viewportHeight;
				boolean overTrack = mouseX >= this.trackX && mouseX <= this.trackX + this.trackW
						&& mouseY >= this.startY && mouseY <= this.startY + this.viewportHeight;
				if (overList || overTrack) {
					this.scrollOffset = MathHelper.clamp(this.scrollOffset - (float)(amount * 12), 0, this.maxScroll);
					updateOptionButtons();
					return true;
				}
			}
			return super.mouseScrolled(mouseX, mouseY, amount);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (this.scrollable) {
				boolean overTrack = mouseX >= this.trackX && mouseX <= this.trackX + this.trackW
						&& mouseY >= this.startY && mouseY <= this.startY + this.viewportHeight;
				if (overTrack) {
					this.draggingScrollbar = true;
					setScrollFromMouse(mouseY);
					return true;
				}
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
			if (this.draggingScrollbar && this.scrollable) {
				setScrollFromMouse(mouseY);
				return true;
			}
			return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}

		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			this.draggingScrollbar = false;
			return super.mouseReleased(mouseX, mouseY, button);
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, float delta) {
			// Draw world background
			this.renderBackground(context);

			// Compute box bounds
			int padding = 4;
			int left = this.x - padding - 1;
			int right = Math.max(this.x + this.buttonWidth, this.trackX + this.trackW) + padding + 1;
			int top = this.startY - padding - 1;
			int bottom = this.startY + this.viewportHeight + 8 + this.buttonHeight + padding + 1;

			// Panel background inside the border (fitting color)
			int boxBg = 0xB0202020; // semi-transparent dark panel
			context.fill(left, top, right, bottom, boxBg);

			// Clip list area so buttons never render outside the box
			context.enableScissor(this.x, this.startY, this.x + this.buttonWidth, this.startY + this.viewportHeight);
			for (ButtonWidget btn : this.optionButtons) {
				if (btn.visible) {
					btn.render(context, mouseX, mouseY, delta);
				}
			}
			context.disableScissor();

			// Scrollbar: make track slightly larger than the thumb range
			if (this.scrollable) {
				int trackPad = 2;
				int trackTop = this.startY - trackPad;
				int trackBottom = this.startY + this.viewportHeight + trackPad;
				// Track
				context.fill(this.trackX, trackTop, this.trackX + this.trackW, trackBottom, 0x66000000);
				// Thumb (still constrained to viewport)
				int thumbY = getThumbY();
				int thumbH = getThumbHeight();
				context.fill(this.trackX + 1, thumbY, this.trackX + this.trackW - 1, thumbY + thumbH, 0xCCFFFFFF);
			}

			// Cancel button (outside of scissor)
			if (this.cancelButton != null) {
				this.cancelButton.render(context, mouseX, mouseY, delta);
			}

			// 1px border
			int borderColor = 0xCCFFFFFF;
			context.fill(left, top, right, top + 1, borderColor);           // Top
			context.fill(left, bottom - 1, right, bottom, borderColor);     // Bottom
			context.fill(left, top, left + 1, bottom, borderColor);         // Left
			context.fill(right - 1, top, right, bottom, borderColor);       // Right
		}

		@Override
		public void close() {
			if (this.client != null) {
				this.client.setScreen(null);
			}
		}
	}
}
