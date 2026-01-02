package com.sigmundgranaas.forgero.mc.testcommon.scenario;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

/**
 * Builder for configuring the player in a gameplay scenario.
 * <p>
 * Provides a fluent API for setting:
 * <ul>
 *   <li>Position and facing direction</li>
 *   <li>Held items (main hand and off-hand)</li>
 *   <li>Game mode (survival/creative)</li>
 *   <li>Health and status effects</li>
 *   <li>Armor equipment</li>
 * </ul>
 */
public class PlayerBuilder {

	private final TestContext context;
	private final ScenarioBuilder.ScenarioState state;

	private BlockPos position = new BlockPos(1, 64, 1);
	private Direction facing = Direction.SOUTH;
	private ItemStack mainHand = ItemStack.EMPTY;
	private ItemStack offHand = ItemStack.EMPTY;
	private GameMode gameMode = GameMode.SURVIVAL;
	private float health = 20.0f;
	private String name = "test-player";
	private ItemStack helmet = ItemStack.EMPTY;
	private ItemStack chestplate = ItemStack.EMPTY;
	private ItemStack leggings = ItemStack.EMPTY;
	private ItemStack boots = ItemStack.EMPTY;

	PlayerBuilder(TestContext context, ScenarioBuilder.ScenarioState state) {
		this.context = context;
		this.state = state;
	}

	/**
	 * Sets the player's position.
	 *
	 * @param pos The position
	 * @return This builder
	 */
	public PlayerBuilder at(BlockPos pos) {
		this.position = pos;
		return this;
	}

	/**
	 * Sets the player's position.
	 *
	 * @param x X coordinate (relative to test structure)
	 * @param y Y coordinate
	 * @param z Z coordinate (relative to test structure)
	 * @return This builder
	 */
	public PlayerBuilder at(int x, int y, int z) {
		return at(new BlockPos(x, y, z));
	}

	/**
	 * Sets the direction the player is facing.
	 *
	 * @param direction The direction
	 * @return This builder
	 */
	public PlayerBuilder facing(Direction direction) {
		this.facing = direction;
		return this;
	}

	/**
	 * Sets the item in the player's main hand.
	 *
	 * @param stack The ItemStack to hold
	 * @return This builder
	 */
	public PlayerBuilder holding(ItemStack stack) {
		this.mainHand = stack;
		return this;
	}

	/**
	 * Sets the item in the player's off-hand.
	 *
	 * @param stack The ItemStack to hold
	 * @return This builder
	 */
	public PlayerBuilder offHand(ItemStack stack) {
		this.offHand = stack;
		return this;
	}

	/**
	 * Sets the player's game mode to survival.
	 *
	 * @return This builder
	 */
	public PlayerBuilder survival() {
		this.gameMode = GameMode.SURVIVAL;
		return this;
	}

	/**
	 * Sets the player's game mode to creative.
	 *
	 * @return This builder
	 */
	public PlayerBuilder creative() {
		this.gameMode = GameMode.CREATIVE;
		return this;
	}

	/**
	 * Sets the player's health.
	 *
	 * @param health Health value (20.0 = full health)
	 * @return This builder
	 */
	public PlayerBuilder health(float health) {
		this.health = health;
		return this;
	}

	/**
	 * Sets the player's name.
	 *
	 * @param name The player name
	 * @return This builder
	 */
	public PlayerBuilder name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Equips the player with a full armor set.
	 *
	 * @param helmet The helmet (can be null for empty slot)
	 * @param chestplate The chestplate (can be null for empty slot)
	 * @param leggings The leggings (can be null for empty slot)
	 * @param boots The boots (can be null for empty slot)
	 * @return This builder
	 */
	public PlayerBuilder wearing(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
		this.helmet = helmet != null ? helmet : ItemStack.EMPTY;
		this.chestplate = chestplate != null ? chestplate : ItemStack.EMPTY;
		this.leggings = leggings != null ? leggings : ItemStack.EMPTY;
		this.boots = boots != null ? boots : ItemStack.EMPTY;
		return this;
	}

	/**
	 * Equips the player with a helmet.
	 *
	 * @param helmet The helmet ItemStack
	 * @return This builder
	 */
	public PlayerBuilder helmet(ItemStack helmet) {
		this.helmet = helmet;
		return this;
	}

	/**
	 * Equips the player with a chestplate.
	 *
	 * @param chestplate The chestplate ItemStack
	 * @return This builder
	 */
	public PlayerBuilder chestplate(ItemStack chestplate) {
		this.chestplate = chestplate;
		return this;
	}

	/**
	 * Equips the player with leggings.
	 *
	 * @param leggings The leggings ItemStack
	 * @return This builder
	 */
	public PlayerBuilder leggings(ItemStack leggings) {
		this.leggings = leggings;
		return this;
	}

	/**
	 * Equips the player with boots.
	 *
	 * @param boots The boots ItemStack
	 * @return This builder
	 */
	public PlayerBuilder boots(ItemStack boots) {
		this.boots = boots;
		return this;
	}

	/**
	 * Completes player configuration and moves to target setup.
	 *
	 * @return A TargetBuilder for configuring the target
	 */
	public TargetBuilder target() {
		// Build the player
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();

		// Configure player
		BlockPos absolutePos = context.getAbsolutePos(position);
		player.refreshPositionAndAngles(absolutePos, facing.asRotation(), 0);
		player.setStackInHand(Hand.MAIN_HAND, mainHand);
		player.setStackInHand(Hand.OFF_HAND, offHand);
		player.changeGameMode(gameMode);
		player.setHealth(health);

		// Equip armor
		player.equipStack(EquipmentSlot.HEAD, helmet);
		player.equipStack(EquipmentSlot.CHEST, chestplate);
		player.equipStack(EquipmentSlot.LEGS, leggings);
		player.equipStack(EquipmentSlot.FEET, boots);

		// Store in state
		state.player = new ScenarioPlayer(player);

		return new TargetBuilder(context, state);
	}
}
