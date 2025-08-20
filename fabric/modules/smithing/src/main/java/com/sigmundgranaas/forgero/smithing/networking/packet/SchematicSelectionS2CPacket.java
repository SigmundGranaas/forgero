package com.sigmundgranaas.forgero.smithing.networking.packet;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

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

		protected SimpleSchematicSelectionScreen(BlockPos anvilPos, List<Identifier> options) {
			super(Text.literal("Select mold"));
			this.anvilPos = anvilPos;
			this.options = options;
		}

		@Override
		protected void init() {
			super.init();
			if (this.client == null) return;

			int buttonWidth = Math.min(220, this.width - 40);
			int buttonHeight = 20;
			int startY = Math.max(40, (this.height - (options.size() * (buttonHeight + 4) + 30)) / 2);
			int x = (this.width - buttonWidth) / 2;

			for (int i = 0; i < options.size(); i++) {
				Identifier id = options.get(i);
				Text label = Text.literal(id.getPath().replace('_', ' '));
				int y = startY + i * (buttonHeight + 4);
				this.addDrawableChild(
						ButtonWidget.builder(label, btn -> {
							var data = PacketByteBufs.create();
							data.writeBlockPos(anvilPos);
							data.writeIdentifier(id);
							ClientPlayNetworking.send(com.sigmundgranaas.forgero.smithing.networking.ModMessages.SCHEMATIC_SELECTED, data);
							close();
						}).dimensions(x, y, buttonWidth, buttonHeight).build()
				);
			}

			this.addDrawableChild(
					ButtonWidget.builder(Text.literal("Cancel"), btn -> close())
							.dimensions(x, startY + options.size() * (buttonHeight + 4) + 8, buttonWidth, buttonHeight)
							.build()
			);
		}

		@Override
		public void close() {
			if (this.client != null) {
				this.client.setScreen(null);
			}
		}
	}
}
