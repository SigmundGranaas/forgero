package com.sigmundgranaas.forgero.blocks.registry;

import com.sigmundgranaas.forgero.blocks.assembly.AssemblyStationBlock;
import com.sigmundgranaas.forgero.blocks.assembly.AssemblyStationScreen;
import com.sigmundgranaas.forgero.blocks.assembly.AssemblyStationScreenHandler;
import com.sigmundgranaas.forgero.blocks.upgrade.UpgradeStationBlock;
import com.sigmundgranaas.forgero.blocks.upgrade.UpgradeStationScreen;
import com.sigmundgranaas.forgero.blocks.upgrade.UpgradeStationScreenHandler;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registry for blocks, items, and screen handlers in the blocks module.
 * <p>
 * This class centralizes all registration logic for station blocks.
 */
public class BlockRegistry {

	public static final String NAMESPACE = "forgero";

	// Block instances
	public static final UpgradeStationBlock UPGRADE_STATION_BLOCK = new UpgradeStationBlock();
	public static final AssemblyStationBlock ASSEMBLY_STATION_BLOCK = new AssemblyStationBlock();

	// Block item instances
	public static final BlockItem UPGRADE_STATION_ITEM = new BlockItem(UPGRADE_STATION_BLOCK, new Item.Settings());
	public static final BlockItem ASSEMBLY_STATION_ITEM = new BlockItem(ASSEMBLY_STATION_BLOCK, new Item.Settings());

	// Identifiers
	public static final Identifier UPGRADE_STATION_ID = new Identifier(NAMESPACE, "upgrade_station");
	public static final Identifier ASSEMBLY_STATION_ID = new Identifier(NAMESPACE, "assembly_station");

	/**
	 * Registers all blocks and items.
	 * <p>
	 * Should be called during mod initialization.
	 */
	public static void registerBlocks() {
		// Register blocks
		Registry.register(Registries.BLOCK, UPGRADE_STATION_ID, UPGRADE_STATION_BLOCK);
		Registry.register(Registries.BLOCK, ASSEMBLY_STATION_ID, ASSEMBLY_STATION_BLOCK);

		// Register block items
		Registry.register(Registries.ITEM, UPGRADE_STATION_ID, UPGRADE_STATION_ITEM);
		Registry.register(Registries.ITEM, ASSEMBLY_STATION_ID, ASSEMBLY_STATION_ITEM);
	}

	/**
	 * Registers all screen handlers.
	 * <p>
	 * Should be called during mod initialization.
	 */
	public static void registerScreenHandlers() {
		Registry.register(
				Registries.SCREEN_HANDLER,
				new Identifier(NAMESPACE, "upgrade_station"),
				UpgradeStationScreenHandler.TYPE
		);
		Registry.register(
				Registries.SCREEN_HANDLER,
				new Identifier(NAMESPACE, "assembly_station"),
				AssemblyStationScreenHandler.TYPE
		);
	}

	/**
	 * Registers client-side screens.
	 * <p>
	 * Should be called during client mod initialization.
	 */
	public static void registerScreensClient() {
		// This should be called from a client-side initializer
		// HandledScreens.register(UpgradeStationScreenHandler.TYPE, UpgradeStationScreen::new);
		// HandledScreens.register(AssemblyStationScreenHandler.TYPE, AssemblyStationScreen::new);
	}

	/**
	 * Gets the upgrade station block.
	 */
	public static Block getUpgradeStation() {
		return UPGRADE_STATION_BLOCK;
	}

	/**
	 * Gets the assembly station block.
	 */
	public static Block getAssemblyStation() {
		return ASSEMBLY_STATION_BLOCK;
	}
}
