package com.sigmundgranaas.forgero.mc.testcommon.helpers;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.test.TestContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameMode;

import java.util.UUID;

/**
 * Factory for creating test players in GameTests with a fluent builder API.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Simple player at origin
 * ServerPlayer player = PlayerFactory.create(context).build();
 *
 * // Player with item and position
 * ServerPlayer player = PlayerFactory.create(context)
 *     .withStack(new ItemStack(Items.DIAMOND_PICKAXE))
 *     .at(new BlockPos(1, 2, 3))
 *     .facing(Direction.SOUTH)
 *     .build();
 *
 * // Survival mode player
 * ServerPlayer player = PlayerFactory.create(context)
 *     .survival()
 *     .build();
 * }</pre>
 */
public class PlayerFactory {

    private final TestContext context;
    private String playerName = "test-player";
    private UUID uuid = UUID.randomUUID();
    private GameMode gameMode = GameMode.CREATIVE;
    private ItemStack mainHandStack = ItemStack.EMPTY;
    private ItemStack offHandStack = ItemStack.EMPTY;
    private BlockPos position = BlockPos.ORIGIN;
    private Direction facing = Direction.NORTH;
    private float pitch = 0.0f;

    private PlayerFactory(TestContext context) {
        this.context = context;
    }

    // ========== Factory Methods ==========

    /**
     * Creates a new PlayerFactory for the given test context.
     *
     * @param context the TestContext
     * @return a new PlayerFactory builder
     */
    public static PlayerFactory create(TestContext context) {
        return new PlayerFactory(context);
    }

    /**
     * Creates a simple test player at the origin with default settings.
     *
     * @param context the TestContext
     * @return a ServerPlayerEntity
     */
    public static ServerPlayerEntity simple(TestContext context) {
        return create(context).build();
    }

    /**
     * Creates a test player at the specified position.
     *
     * @param context the TestContext
     * @param pos the position for the player
     * @return a ServerPlayerEntity
     */
    public static ServerPlayerEntity at(TestContext context, BlockPos pos) {
        return create(context).at(pos).build();
    }

    // ========== Builder Methods ==========

    /**
     * Sets the player's name.
     *
     * @param name the player name
     * @return this builder for chaining
     */
    public PlayerFactory withName(String name) {
        this.playerName = name;
        return this;
    }

    /**
     * Sets the player's UUID.
     *
     * @param uuid the player UUID
     * @return this builder for chaining
     */
    public PlayerFactory withUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * Sets the item in the player's main hand.
     *
     * @param stack the ItemStack to hold
     * @return this builder for chaining
     */
    public PlayerFactory withStack(ItemStack stack) {
        this.mainHandStack = stack;
        return this;
    }

    /**
     * Sets the item in the player's main hand.
     * Convenience alias for {@link #withStack(ItemStack)}.
     *
     * @param stack the ItemStack to hold
     * @return this builder for chaining
     */
    public PlayerFactory holding(ItemStack stack) {
        return withStack(stack);
    }

    /**
     * Sets the item in the player's off hand.
     *
     * @param stack the ItemStack to hold in off hand
     * @return this builder for chaining
     */
    public PlayerFactory withOffHand(ItemStack stack) {
        this.offHandStack = stack;
        return this;
    }

    /**
     * Sets items in both hands.
     *
     * @param mainHand the ItemStack for main hand
     * @param offHand the ItemStack for off hand
     * @return this builder for chaining
     */
    public PlayerFactory withStacks(ItemStack mainHand, ItemStack offHand) {
        this.mainHandStack = mainHand;
        this.offHandStack = offHand;
        return this;
    }

    /**
     * Sets the player's position.
     *
     * @param pos the block position
     * @return this builder for chaining
     */
    public PlayerFactory at(BlockPos pos) {
        this.position = pos;
        return this;
    }

    /**
     * Sets the player's position.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return this builder for chaining
     */
    public PlayerFactory at(int x, int y, int z) {
        this.position = new BlockPos(x, y, z);
        return this;
    }

    /**
     * Sets the direction the player is facing.
     *
     * @param direction the facing direction
     * @return this builder for chaining
     */
    public PlayerFactory facing(Direction direction) {
        this.facing = direction;
        return this;
    }

    /**
     * Sets the player's pitch (vertical look angle).
     *
     * @param pitch the pitch angle in degrees
     * @return this builder for chaining
     */
    public PlayerFactory withPitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    /**
     * Sets the player to creative mode.
     *
     * @return this builder for chaining
     */
    public PlayerFactory creative() {
        this.gameMode = GameMode.CREATIVE;
        return this;
    }

    /**
     * Sets the player to survival mode.
     *
     * @return this builder for chaining
     */
    public PlayerFactory survival() {
        this.gameMode = GameMode.SURVIVAL;
        return this;
    }

    /**
     * Sets the player to adventure mode.
     *
     * @return this builder for chaining
     */
    public PlayerFactory adventure() {
        this.gameMode = GameMode.ADVENTURE;
        return this;
    }

    /**
     * Sets the player to spectator mode.
     *
     * @return this builder for chaining
     */
    public PlayerFactory spectator() {
        this.gameMode = GameMode.SPECTATOR;
        return this;
    }

    /**
     * Sets the player's game mode.
     *
     * @param gameMode the game mode
     * @return this builder for chaining
     */
    public PlayerFactory withGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
        return this;
    }

    // ========== Build Method ==========

    /**
     * Builds and spawns the ServerPlayerEntity with the configured settings.
     *
     * @return the created ServerPlayerEntity
     */
    public ServerPlayerEntity build() {
        var level = context.getWorld();
        var server = level.getServer();

        // Disable demo mode
        server.setDemo(false);

        // Create the player entity
        ServerPlayerEntity player = new ServerPlayerEntity(
                server,
                level,
                new GameProfile(uuid, playerName)
        );

        // Set up network handler (required for the player to function properly)
        player.networkHandler = new ServerPlayNetworkHandler(
                server,
                new ClientConnection(NetworkSide.SERVERBOUND),
                player
        );

        // Set position and orientation
        BlockPos absolutePos = context.getAbsolutePos(position);
        player.setPos(absolutePos.getX() + 0.5, absolutePos.getY(), absolutePos.getZ() + 0.5);
        player.setYaw(facing.asRotation());
        player.setPitch(pitch);
        player.headYaw = facing.asRotation();

        // Set items in hands
        player.setStackInHand(Hand.MAIN_HAND, mainHandStack);
        player.setStackInHand(Hand.OFF_HAND, offHandStack);

        // Connect the player to the server
        server.getPlayerManager().onPlayerConnect(
                new ClientConnection(NetworkSide.CLIENTBOUND),
                player
        );

        // Tick the player once to initialize
        player.tick();

        // Set game mode (must be done after connection)
        player.changeGameMode(gameMode);

        return player;
    }
}
