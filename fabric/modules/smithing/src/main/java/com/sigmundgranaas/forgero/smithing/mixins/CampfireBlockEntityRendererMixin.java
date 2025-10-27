package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.smithing.campfire.ExtraHeatSlotConfig;
import com.sigmundgranaas.forgero.smithing.campfire.ExtraHeatSlotProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.CampfireBlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

@Mixin(CampfireBlockEntityRenderer.class)
public class CampfireBlockEntityRendererMixin {
    @Unique
    private static final int EXTRA_SEED_OFFSET = 98765;

    @Inject(
        method = "render(Lnet/minecraft/block/entity/CampfireBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At("TAIL")
    )
    private void forgero$renderExtraHeatSlot(CampfireBlockEntity campfire, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay, CallbackInfo ci) {
        try {
            System.out.println("[DEBUG] renderExtraHeatSlot called");

            ExtraHeatSlotProvider provider = (ExtraHeatSlotProvider)(Object)campfire;
            System.out.println("[DEBUG] Provider cast successful");

            ItemStack extra = provider.forgero$getExtraHeatSlot();
            System.out.println("[DEBUG] Extra slot ItemStack: " + extra);
            System.out.println("[DEBUG] Extra slot isEmpty: " + (extra == null || extra.isEmpty()));

            if (extra == null || extra.isEmpty()) {
                System.out.println("[DEBUG] Extra slot is empty, returning early");
                return;
            }

            ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
            World world = MinecraftClient.getInstance().world;
            System.out.println("[DEBUG] Renderer obtained: " + (renderer != null));
            System.out.println("[DEBUG] World obtained: " + (world != null));

            if (world == null) {
                System.out.println("[DEBUG] World is null, returning");
                return;
            }

            System.out.println("[DEBUG] Starting render setup");
            matrices.push();
            System.out.println("[DEBUG] Matrix pushed");

            // Get campfire facing
            Direction facing = Direction.NORTH;
            var state = campfire.getCachedState();
            if (state != null && state.contains(Properties.HORIZONTAL_FACING)) {
                facing = state.get(Properties.HORIZONTAL_FACING);
            }
            System.out.println("[DEBUG] Campfire facing: " + facing);

            // Apply transforms in order: translate to center, rotate by facing, offset slot, rotate for ground
            matrices.translate(0.5D, 0.44921875D, 0.5D);
            System.out.println("[DEBUG] Center translate applied");

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
            System.out.println("[DEBUG] Facing rotation applied");

            matrices.translate(ExtraHeatSlotConfig.RENDER_SLOT_OFFSET_X, 0.0D, ExtraHeatSlotConfig.RENDER_SLOT_OFFSET_Z);
            System.out.println("[DEBUG] Slot offset applied: X=" + ExtraHeatSlotConfig.RENDER_SLOT_OFFSET_X + " Z=" + ExtraHeatSlotConfig.RENDER_SLOT_OFFSET_Z);

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            System.out.println("[DEBUG] X-rotation (90°) applied");

            matrices.scale(ExtraHeatSlotConfig.RENDER_SCALE, ExtraHeatSlotConfig.RENDER_SCALE, ExtraHeatSlotConfig.RENDER_SCALE);
            System.out.println("[DEBUG] Scale applied: " + ExtraHeatSlotConfig.RENDER_SCALE);

            System.out.println("[DEBUG] About to call renderItem with light=" + light + " overlay=" + overlay);
            System.out.println("[DEBUG] Item to render: " + extra.getItem().getName(extra).getString());

            renderer.renderItem(extra, ModelTransformationMode.FIXED, light, overlay, matrices, vcp, world, EXTRA_SEED_OFFSET);
            System.out.println("[DEBUG] renderItem completed");

            matrices.pop();
            System.out.println("[DEBUG] Matrix popped, render complete");

        } catch (Exception e) {
            System.err.println("[ERROR] Exception in renderExtraHeatSlot:");
            e.printStackTrace();
        }
    }
}
