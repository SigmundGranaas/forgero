package com.sigmundgranaas.forgero.testutil;

import net.minecraft.item.ItemStack;

import java.util.function.Supplier;

import static com.sigmundgranaas.forgero.testutil.Utils.createTool;

public class Items {
	// Ore miner heads
	public static String VEIN_MINING_PICKAXE_HEAD = "forgero:netherite-mandrill_pickaxe_head";
	public static String STONE_VEIN_MINING_PICKAXE_HEAD = "forgero:stone-mandrill_pickaxe_head";
	public static String IRON_ORE_MINER_PICKAXE_HEAD = "forgero:iron-mandrill_pickaxe_head";

	// Path mining heads
	public static String ENTRENCHING_SHOVEL_HEAD = "forgero:netherite-entrenching_shovel_head";

	// Grave digger heads
	public static String NETHERITE_SPADE_HEAD = "forgero:netherite-spade_head";

	// Reaper heads
	public static String NETHERITE_SCYTHE_HEAD = "forgero:netherite-scythe_blade";

	// Tree chopper heads
	public static String TREE_FELLER_AXE_HEAD = "forgero:iron-felling_axe_head";

	// Handles
	public static String OAK_HANDLE = "forgero:oak-handle";

	// Pickaxes
	public static String IRON_PICKAXE = "forgero:netherite-pickaxe";
	public static String NETHERITE_PICKAXE = "forgero:netherite-pickaxe";
	public static String STONE_PICKAXE = "forgero:stone-pickaxe";

	// Ore miner pickaxes
	public static Supplier<ItemStack> NETHERITE_ORE_MINER_PICKAXE = () -> createTool(VEIN_MINING_PICKAXE_HEAD, OAK_HANDLE, NETHERITE_PICKAXE);
	public static Supplier<ItemStack> STONE_ORE_MINER_PICKAXE = () -> createTool(STONE_VEIN_MINING_PICKAXE_HEAD, OAK_HANDLE, STONE_PICKAXE);
	public static Supplier<ItemStack> IRON_ORE_MINER_PICKAXE = () -> createTool(IRON_ORE_MINER_PICKAXE_HEAD, OAK_HANDLE, IRON_PICKAXE);

	// Shovels
	public static String NETHERITE_SHOVEL = "forgero:netherite-shovel";

	// Grave digger shovels
	public static Supplier<ItemStack> NETHERITE_SPADE_SHOVEL = () -> createTool(NETHERITE_SPADE_HEAD, OAK_HANDLE, NETHERITE_SHOVEL);

	// Hoes
	public static String NETHERITE_HOE = "forgero:netherite-hoe";

	// Reaper hoe
	public static Supplier<ItemStack> NETHERITE_SCYTHE_HOE = () -> createTool(NETHERITE_SCYTHE_HEAD, OAK_HANDLE, NETHERITE_HOE);

	// Axes
	public static String NETHERITE_AXE = "forgero:netherite-axe";

	// Tree chopper axe
	public static Supplier<ItemStack> NETHERITE_TREE_FELLER_AXE = () -> createTool(TREE_FELLER_AXE_HEAD, OAK_HANDLE, NETHERITE_AXE);

	// Entrenching shovel
	public static Supplier<ItemStack> NETHERITE_ENTRENCHING_SHOVEL = () -> createTool(ENTRENCHING_SHOVEL_HEAD, OAK_HANDLE, NETHERITE_PICKAXE);
}
