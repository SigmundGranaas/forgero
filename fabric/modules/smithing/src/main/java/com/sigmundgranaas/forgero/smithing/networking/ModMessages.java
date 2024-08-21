package com.sigmundgranaas.forgero.smithing.networking;


import com.sigmundgranaas.forgero.core.Forgero;

import com.sigmundgranaas.forgero.smithing.networking.packet.ItemStackSyncS2CPacket;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ModMessages {

    public static final Identifier ITEM_SYNC = new Identifier(Forgero.NAMESPACE, "item_sync");
    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ITEM_SYNC, ItemStackSyncS2CPacket::receive);
    }
}
