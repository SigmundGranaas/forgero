package com.sigmundgranaas.forgero.smithing.mixins;

import com.sigmundgranaas.forgero.smithing.campfire.ExtraHeatSlotConfig;
import com.sigmundgranaas.forgero.smithing.campfire.ExtraHeatSlotProvider;
import com.sigmundgranaas.forgero.smithing.networking.S2C.CampfireSyncS2CPacket;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin implements ExtraHeatSlotProvider {
    @Shadow @Final private DefaultedList<ItemStack> itemsBeingCooked;
    @Shadow @Final private int[] cookingTimes;
    @Shadow @Final private int[] cookingTotalTimes;

    // Extra slot that only accepts TemperatureUtils.hasMaxTemperature items
    @Unique private ItemStack forgero$extraHeatSlot = ItemStack.EMPTY;
    @Unique private static final String FORGERO_EXTRA_SLOT_KEY = "forgero_extra_heat_slot";
    @Unique private int forgero$syncCooldown = 0;
    @Unique private static final int SYNC_COOLDOWN_TICKS = 2;
    @Unique private boolean forgero$shouldSyncImmediately = false;

    // Persist the extra slot to NBT
    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void forgero$writeExtraSlot(NbtCompound nbt, CallbackInfo ci) {
        if (!forgero$extraHeatSlot.isEmpty()) {
            nbt.put(FORGERO_EXTRA_SLOT_KEY, forgero$extraHeatSlot.writeNbt(new NbtCompound()));
        }
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void forgero$readExtraSlot(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(FORGERO_EXTRA_SLOT_KEY)) {
            forgero$extraHeatSlot = ItemStack.fromNbt(nbt.getCompound(FORGERO_EXTRA_SLOT_KEY));
        } else {
            forgero$extraHeatSlot = ItemStack.EMPTY;
        }
    }

    // Heat the extra slot when lit. Do not touch vanilla cooking arrays.
    @Inject(method = "litServerTick", at = @At("HEAD"))
    private static void forgero$heatUp(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        if (!state.get(CampfireBlock.LIT)) {
            return;
        }
        CampfireBlockEntityMixin accessor = (CampfireBlockEntityMixin)(Object)campfire;
        ItemStack stack = accessor.forgero$extraHeatSlot;
        if (stack.isEmpty() || !TemperatureUtils.hasMaxTemperature(stack)) {
            return;
        }
        int max = TemperatureUtils.getMaxTemp(stack);
        if (max <= 0) {
            return;
        }
        int current = TemperatureUtils.getTemperature(stack);
        int next = Math.min(max, current + ExtraHeatSlotConfig.HEAT_PER_TICK);
        if (next != current) {
            TemperatureUtils.setTemperature(stack, next);
            ((BlockEntity)campfire).markDirty();

            // Sync on player interaction flag or after cooldown
            boolean shouldSync = accessor.forgero$shouldSyncImmediately || accessor.forgero$syncCooldown <= 0;
            if (shouldSync && !world.isClient && world instanceof ServerWorld server) {
                accessor.forgero$syncCooldown = SYNC_COOLDOWN_TICKS;
                accessor.forgero$shouldSyncImmediately = false;
                ChunkPos chunkPos = new ChunkPos(pos);
                for (ServerPlayerEntity player : server.getPlayers()) {
                    if (server.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) {
                        CampfireSyncS2CPacket.send(player, pos, stack);
                    }
                }
            }
        }
    }

    // Decrement the sync cooldown each tick
    @Inject(method = "litServerTick", at = @At("TAIL"))
    private static void forgero$decrementSyncCooldown(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        CampfireBlockEntityMixin accessor = (CampfireBlockEntityMixin)(Object)campfire;
        if (accessor.forgero$syncCooldown > 0) {
            accessor.forgero$syncCooldown--;
        }
    }

    // Add a small particle at the extra slot position on client when lit and slot has item.
    @Inject(method = "clientTick", at = @At("TAIL"))
    private static void forgero$extraSlotParticles(World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        if (!world.isClient) return;
        if (!state.get(CampfireBlock.LIT)) return;
        CampfireBlockEntityMixin accessor = (CampfireBlockEntityMixin)(Object)campfire;
        ItemStack stack = accessor.forgero$extraHeatSlot;
        if (stack.isEmpty()) return;

        Random rand = world.getRandom();
        double vx = (rand.nextDouble() - 0.5D) * 0.02D;
        double vy = 0.03D + rand.nextDouble() * 0.02D;
        double vz = (rand.nextDouble() - 0.5D) * 0.02D;

        // Get campfire facing direction
        Direction facing = Direction.NORTH;
        if (state.contains(Properties.HORIZONTAL_FACING)) {
            facing = state.get(Properties.HORIZONTAL_FACING);
        }

        // Rotate particle offset based on facing direction
        double offsetX = ExtraHeatSlotConfig.PARTICLE_X;
        double offsetZ = ExtraHeatSlotConfig.PARTICLE_Z;
        double rotatedX = offsetX;
        double rotatedZ = offsetZ;

        switch (facing) {
            case SOUTH:
                rotatedX = -offsetX;
                rotatedZ = -offsetZ;
                break;
            case EAST:
                rotatedX = offsetZ;
                rotatedZ = offsetX;
                break;
            case WEST:
                rotatedX = -offsetZ;
                rotatedZ = -offsetX;
                break;
            case NORTH:
            default:
                rotatedX = offsetX;
                rotatedZ = offsetZ;
                break;
        }

        double x = pos.getX() + 0.5D + rotatedX;
        double y = pos.getY() + ExtraHeatSlotConfig.PARTICLE_Y;
        double z = pos.getZ() + 0.5D + rotatedZ;
        world.addParticle(ParticleTypes.SMOKE, x, y, z, vx, vy, vz);
    }

    // Accessor impl
    @Override
    public ItemStack forgero$getExtraHeatSlot() {
        return forgero$extraHeatSlot;
    }

    @Override
    public void forgero$setExtraHeatSlot(ItemStack stack) {
        this.forgero$extraHeatSlot = stack == null ? ItemStack.EMPTY : stack;
        BlockEntity be = (BlockEntity)(Object)this;
        if (be.getWorld() != null && !be.getWorld().isClient) {
            be.markDirty();
            BlockPos pos = be.getPos();
            be.getWorld().updateListeners(pos, be.getCachedState(), be.getCachedState(), 3);
            // Always sync immediately (from player interaction or direct setting)
            if (be.getWorld() instanceof ServerWorld server) {
                ChunkPos chunkPos = new ChunkPos(pos);
                for (ServerPlayerEntity player : server.getPlayers()) {
                    if (server.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) {
                        CampfireSyncS2CPacket.send(player, pos, this.forgero$extraHeatSlot);
                    }
                }
            }
            // Force sync on next heat tick as well
            forgero$shouldSyncImmediately = true;
        }
    }

    @Override
    public void forgero$setExtraHeatSlotClient(ItemStack stack) {
        this.forgero$extraHeatSlot = stack == null ? ItemStack.EMPTY : stack;
    }
}
