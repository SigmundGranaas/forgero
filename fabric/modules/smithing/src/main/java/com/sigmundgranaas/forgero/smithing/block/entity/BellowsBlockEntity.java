package com.sigmundgranaas.forgero.smithing.block.entity;

import com.sigmundgranaas.forgero.smithing.block.custom.BellowsBlock;
import com.sigmundgranaas.forgero.smithing.block.custom.BloomeryBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Block entity for the bellows that provides temporary temperature boosts to connected bloomeries.
 */
public class BellowsBlockEntity extends BlockEntity {

    // Bellows operation constants
    private static final int BOOST_DURATION = 200; // 10 seconds (200 ticks) - full boost phase
    private static final int DECAY_DURATION = 400; // 20 seconds (400 ticks) - gradual decay phase
    private static final int COOLDOWN_DURATION = 100; // 5 seconds before can be used again
    private static final int TEMPERATURE_BOOST = 200; // Maximum additional temperature provided

    // State tracking
    private int boostTicks = 0; // Remaining ticks of full temperature boost
    private int decayTicks = 0; // Remaining ticks of gradual decay
    private int cooldownTicks = 0; // Remaining cooldown before next use
    private boolean wasActive = false; // For visual state management

    public BellowsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELLOWS, pos, state);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, BellowsBlockEntity entity) {
        boolean stateChanged = false;

        // Update boost timer
        if (entity.boostTicks > 0) {
            entity.boostTicks--;
            if (entity.boostTicks == 0) {
                // Start decay phase when boost ends
                entity.decayTicks = DECAY_DURATION;
                stateChanged = true;
            }
        }

        // Update decay timer
        if (entity.decayTicks > 0) {
            entity.decayTicks--;
            if (entity.decayTicks == 0) {
                stateChanged = true;
            }
        }

        // Update cooldown timer
        if (entity.cooldownTicks > 0) {
            entity.cooldownTicks--;
        }

        // Update visual state - bellows appears active during boost and decay phases
        boolean shouldBeActive = entity.boostTicks > 0 || entity.decayTicks > 0;
        boolean currentlyActive = state.get(BellowsBlock.ACTIVE);

        if (shouldBeActive != currentlyActive) {
            world.setBlockState(pos, state.with(BellowsBlock.ACTIVE, shouldBeActive), Block.NOTIFY_ALL);
            stateChanged = true;
        }

        if (stateChanged) {
            entity.markDirty();
        }
    }

    /**
     * Attempts to activate the bellows (blow air into connected bloomery).
     * @return true if successfully activated, false if on cooldown or no bloomery connected
     */
    public boolean tryActivate() {
        // Check if on cooldown
        if (cooldownTicks > 0) {
            return false;
        }

        // Check if there's a connected bloomery that's lit
        if (!hasConnectedLitBloomery()) {
            return false;
        }

        // Activate bellows - clear any existing decay and start fresh boost
        boostTicks = BOOST_DURATION;
        decayTicks = 0; // Clear any existing decay
        cooldownTicks = COOLDOWN_DURATION;
        markDirty();

        return true;
    }

    /**
     * Gets the current temperature boost provided by this bellows.
     * Only provides boost during the boost phase, not during decay
     * @return temperature boost amount, 0 if not in boost phase
     */
    public int getTemperatureBoost() {
        if (boostTicks > 0) {
            // Full boost during boost phase only
            return TEMPERATURE_BOOST;
        }
        // During decay phase or inactive, return 0 so bloomery handles its own decay
        return 0;
    }

    /**
     * Checks if the bellows is currently providing any temperature boost.
     */
    public boolean isActive() {
        return boostTicks > 0 || decayTicks > 0;
    }

    /**
     * Gets remaining boost time in ticks.
     */
    public int getRemainingBoostTicks() {
        return boostTicks;
    }

    /**
     * Gets remaining decay time in ticks.
     */
    public int getRemainingDecayTicks() {
        return decayTicks;
    }

    /**
     * Gets remaining cooldown time in ticks.
     */
    public int getRemainingCooldownTicks() {
        return cooldownTicks;
    }

    /**
     * Checks if there's a connected bloomery that is currently lit.
     */
    private boolean hasConnectedLitBloomery() {
        if (world == null) return false;

        // Check all horizontal directions for a lit bloomery
        for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos bloomeryPos = pos.offset(direction);
            BlockState bloomeryState = world.getBlockState(bloomeryPos);

            if (bloomeryState.getBlock() instanceof BloomeryBlock bloomeryBlock) {
                if (bloomeryState.get(BloomeryBlock.LIT)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets all connected bloomery block entities for temperature boosting.
     */
    public java.util.List<BloomeryBlockEntity> getConnectedBloomeries() {
        java.util.List<BloomeryBlockEntity> bloomeries = new java.util.ArrayList<>();

        if (world == null) return bloomeries;

        // Check all horizontal directions for bloomery entities
        for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos bloomeryPos = pos.offset(direction);
            BlockEntity blockEntity = world.getBlockEntity(bloomeryPos);

            if (blockEntity instanceof BloomeryBlockEntity bloomery) {
                bloomeries.add(bloomery);
            }
        }

        return bloomeries;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        boostTicks = nbt.getInt("BoostTicks");
        decayTicks = nbt.getInt("DecayTicks");
        cooldownTicks = nbt.getInt("CooldownTicks");
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("BoostTicks", boostTicks);
        nbt.putInt("DecayTicks", decayTicks);
        nbt.putInt("CooldownTicks", cooldownTicks);
    }

    @Override
    public net.minecraft.nbt.NbtCompound toInitialChunkDataNbt() {
        net.minecraft.nbt.NbtCompound nbt = new net.minecraft.nbt.NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    @Override
    public net.minecraft.network.packet.Packet<net.minecraft.network.listener.ClientPlayPacketListener> toUpdatePacket() {
        return net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket.create(this);
    }
}
