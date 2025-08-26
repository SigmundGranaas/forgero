package com.sigmundgranaas.forgero.smithing.networking;

import com.sigmundgranaas.forgero.smithing.networking.S2C.TemperatureSyncS2CPacket;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class NetworkingInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(TemperatureSyncS2CPacket.ID, (client, handler, buf, responseSender) -> {
            TemperatureSyncS2CPacket.handle(buf);
        });
    }
}

