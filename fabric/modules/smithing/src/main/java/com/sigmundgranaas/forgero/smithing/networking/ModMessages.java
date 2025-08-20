package com.sigmundgranaas.forgero.smithing.networking;


import java.util.List;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.smithing.block.entity.SmithingAnvilBlockEntity;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
public class ModMessages {

    public static final Identifier ITEM_SYNC = new Identifier(Forgero.NAMESPACE, "item_sync");
    public static final Identifier OPEN_MOLD_SELECTION = new Identifier(Forgero.NAMESPACE, "open_mold_selection");
    public static final Identifier MOLD_SELECTED = new Identifier(Forgero.NAMESPACE, "mold_selected");


    // Register C2S on class load (server + client). Safe-guard with a flag to avoid duplicate registrations.
    private static volatile boolean C2S_REGISTERED = false;
    static {
        registerC2SPackets();
        registerClientPackets();
    }

    public static void registerC2SPackets() {
        if (C2S_REGISTERED) return;
        C2S_REGISTERED = true;

        ServerPlayNetworking.registerGlobalReceiver(MOLD_SELECTED, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            Identifier selected = buf.readIdentifier();
            server.execute(() -> {
                var be = player.getWorld().getBlockEntity(pos);
                if (be instanceof SmithingAnvilBlockEntity anvil) {
                    anvil.setPlannedProduct(selected);
                }
            });
        });
    }

    public static void registerClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ITEM_SYNC, (client, handler, buf, responseSender) -> {
            // No-op handler for now. Add logic here if needed.
        });
    }

    @Environment(EnvType.CLIENT)
    private static class SimpleMoldSelectionScreen extends Screen {
        private final BlockPos anvilPos;
        private final List<Identifier> options;

        protected SimpleMoldSelectionScreen(BlockPos anvilPos, List<Identifier> options) {
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
                            ClientPlayNetworking.send(MOLD_SELECTED, data);
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
