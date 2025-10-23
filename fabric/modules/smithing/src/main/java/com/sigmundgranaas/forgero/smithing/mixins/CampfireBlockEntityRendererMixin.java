package com.sigmundgranaas.forgero.smithing.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.CampfireBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;

@Mixin(CampfireBlockEntityRenderer.class)
public class CampfireBlockEntityRendererMixin {
    @Unique
    private static final int INGOT_SLOT = 0;

    @Inject(
        method = "render(Lnet/minecraft/block/entity/CampfireBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;I)V"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void forgero$offsetIngotSlot(CampfireBlockEntity campfire, float f, MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay, CallbackInfo ci,
                                         Direction facing, DefaultedList<ItemStack> items, int seedBase, int slotIndex,
                                         ItemStack stack, Direction rotated, float g) {
        if (slotIndex == INGOT_SLOT) {
            matrices.translate(0.85F, 0.85F, 0);
            matrices.scale(1.6F, 1.6F, 2F);
        }
    }
}
