package com.sigmundgranaas.forgero.testutil;

import static com.sigmundgranaas.forgero.testutil.Utils.createTool;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;

public class Items {
	// Ore miner heads
	public static String VEIN_MINING_PICKAXE_HEAD = "forgero:netherite-ore_miner_pickaxe_head";
	public static String STONE_VEIN_MINING_PICKAXE_HEAD = "forgero:stone-ore_miner_pickaxe_head";
	public static String IRON_ORE_MINER_PICKAXE_HEAD = "forgero:iron-ore_miner_pickaxe_head";

	// Path mining heads
	public static String PATH_MINING_PICKAXE_HEAD = "forgero:netherite-path_mining_pickaxe_head";

	// Grave digger heads
	public static String NETHERITE_GRAVE_DIGGER_HEAD = "forgero:netherite-grave_digger_head";

	// Path digger heads
	public static String NETHERITE_PATH_DIGGER_HEAD = "forgero:netherite-path_digger_shovel_head";

	// Reaper heads
	public static String NETHERITE_REAPER_HEAD = "forgero:netherite-reaper_head";

	// Tree chopper heads
	public static String TREE_CHOPPER_AXE_HEAD = "forgero:iron-tree_chopper_head";

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
	public static Supplier<ItemStack> NETHERITE_GRAVE_DIGGER_SHOVEL = () -> createTool(NETHERITE_GRAVE_DIGGER_HEAD, OAK_HANDLE, NETHERITE_SHOVEL);

	// Hoes
	public static String NETHERITE_HOE = "forgero:netherite-hoe";

	// Reaper hoe
	public static Supplier<ItemStack> NETHERITE_REAPER_HOE = () -> createTool(NETHERITE_REAPER_HEAD, OAK_HANDLE, NETHERITE_HOE);

	// Axes
	public static String NETHERITE_AXE = "forgero:netherite-axe";

	// Tree chopper axe
	public static Supplier<ItemStack> NETHERITE_TREE_CHOPPER_AXE = () -> createTool(TREE_CHOPPER_AXE_HEAD, OAK_HANDLE, NETHERITE_AXE);

	// Path miner pickaxe
	public static Supplier<ItemStack> NETHERITE_PATH_MINING_PICKAXE = () -> createTool(PATH_MINING_PICKAXE_HEAD, OAK_HANDLE, NETHERITE_PICKAXE);

	// Path digger shovels
	public static Supplier<ItemStack> NETHERITE_PATH_DIGGER_SHOVEL = () -> createTool(NETHERITE_PATH_DIGGER_HEAD, OAK_HANDLE, NETHERITE_SHOVEL);

}
