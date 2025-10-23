package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin {
    @Shadow @Final private DefaultedList<ItemStack> itemsBeingCooked;
    @Shadow @Final private int[] cookingTimes;
    @Shadow @Final private int[] cookingTotalTimes;

    @Unique private static final int FORGERO_INGOT_SLOT = 0;
    @Unique private static final int FORGERO_HEAT_PER_TICK = 2; // Adjust as desired

    // Particle position offsets for the ingot slot (relative to block origin)
    @Unique private static final double FORGERO_INGOT_PARTICLE_X = 0.80D;
    @Unique private static final double FORGERO_INGOT_PARTICLE_Y = 0.62D;
    @Unique private static final double FORGERO_INGOT_PARTICLE_Z = 0.20D;

    // Thread-local tracking for clientTick
    @Unique private static final ThreadLocal<Integer> FORGERO_CURRENT_INDEX = ThreadLocal.withInitial(() -> -1);
    @Unique private static final ThreadLocal<BlockPos> FORGERO_CURRENT_POS = new ThreadLocal<>();

    @Inject(method = "litServerTick", at = @At("HEAD"))
    private static void forgero$heatUp(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        CampfireBlockEntityMixin accessor = (CampfireBlockEntityMixin)(Object)campfire;
        for (int i = 0; i < accessor.itemsBeingCooked.size(); i++) {
            ItemStack stack = accessor.itemsBeingCooked.get(i);
            if (stack.isEmpty() || !TemperatureUtils.hasMaxTemperature(stack)) {
                continue;
            }

            accessor.cookingTotalTimes[i] = Integer.MAX_VALUE;

            int max = TemperatureUtils.getMaxTemp(stack);
            if (max <= 0) {
                continue;
            }

            int current = TemperatureUtils.getTemperature(stack);
            int next = Math.min(max, current + FORGERO_HEAT_PER_TICK);
            if (next != current) {
                TemperatureUtils.setTemperature(stack, next);
                campfire.markDirty();
                world.updateListeners(pos, state, state, 3);
            }
        }
    }

    // Track the BlockPos for the duration of the client tick
    @Inject(method = "clientTick", at = @At("HEAD"))
    private static void forgero$trackClientTickEnter(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        FORGERO_CURRENT_POS.set(pos);
        FORGERO_CURRENT_INDEX.set(-1);
    }

    @Inject(method = "clientTick", at = @At("RETURN"))
    private static void forgero$trackClientTickExit(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        FORGERO_CURRENT_POS.remove();
        FORGERO_CURRENT_INDEX.set(-1);
    }

    // Capture the slot index used during particle generation
    @Redirect(
        method = "clientTick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;get(I)Ljava/lang/Object;")
    )
    private static Object forgero$captureSlotIndex(DefaultedList<?> list, int index) {
        FORGERO_CURRENT_INDEX.set(index);
        return list.get(index);
    }

    // Move particles for the ingot slot to a custom position
    @Redirect(
        method = "clientTick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V")
    )
    private static void forgero$redirectCookingParticles(World world, ParticleEffect effect, double x, double y, double z, double vx, double vy, double vz) {
        int idx = FORGERO_CURRENT_INDEX.get();
        BlockPos pos = FORGERO_CURRENT_POS.get();

        if (idx == FORGERO_INGOT_SLOT && pos != null) {
            double nx = pos.getX() + FORGERO_INGOT_PARTICLE_X;
            double ny = pos.getY() + FORGERO_INGOT_PARTICLE_Y;
            double nz = pos.getZ() + FORGERO_INGOT_PARTICLE_Z;
            world.addParticle(effect, nx, ny, nz, vx, vy, vz);
        } else {
            world.addParticle(effect, x, y, z, vx, vy, vz);
        }
    }
}
