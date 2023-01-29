package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.minecraft.common.toolhandler.SoulLevelUpHandler;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.TotemEffectHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.EntityStatuses.ENTITY_STATUS_SOUL_LEVEL_UP;
import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.EntityStatuses.ENTITY_STATUS_TOTEM;


@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkGetTotemMixin {

    @Shadow
    private ClientWorld world;

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(at = @At("HEAD"), method = "onEntityStatus")
    private void getTotem(EntityStatusS2CPacket packet, CallbackInfo indo) {
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, client);
        Entity entity = packet.getEntity(world);
        if (entity instanceof PlayerEntity player) {
            if (packet.getStatus() == ENTITY_STATUS_TOTEM) {
                TotemEffectHandler.of(client, player, world).run();
            } else if (packet.getStatus() == ENTITY_STATUS_SOUL_LEVEL_UP) {
                SoulLevelUpHandler.of(client, player, world).run();
            }
        }
    }
}
