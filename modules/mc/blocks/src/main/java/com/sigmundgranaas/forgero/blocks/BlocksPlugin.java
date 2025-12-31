package com.sigmundgranaas.forgero.blocks;

import com.sigmundgranaas.forgero.blocks.assembly.AssemblyStationBlock;
import com.sigmundgranaas.forgero.blocks.registry.BlockRegistry;
import com.sigmundgranaas.forgero.blocks.upgrade.UpgradeStationBlock;
import com.sigmundgranaas.forgero.loader.api.DataLoadingContext;
import com.sigmundgranaas.forgero.loader.api.PostLoadPlugin;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin for the blocks module.
 * <p>
 * This plugin is loaded via Fabric's entrypoint system after Forgero
 * has finished loading. It:
 * <ul>
 *   <li>Registers station blocks and items</li>
 *   <li>Registers screen handlers</li>
 *   <li>Sets up service suppliers for blocks</li>
 *   <li>Adds blocks to creative tabs</li>
 * </ul>
 */
public class BlocksPlugin implements PostLoadPlugin {

	private static final Logger LOGGER = LoggerFactory.getLogger(BlocksPlugin.class);

	@Override
	public String getId() {
		return "forgero:blocks-plugin";
	}

	@Override
	public void onDataLoaded(DataLoadingContext context) {
		LOGGER.info("Initializing Forgero Blocks module...");

		// Set up service suppliers for blocks (DataLoadingContext extends ForgeroServices)
		UpgradeStationBlock.setServicesSupplier(() -> context);
		AssemblyStationBlock.setServicesSupplier(() -> context);

		// Register blocks, items, and screen handlers
		BlockRegistry.registerBlocks();
		BlockRegistry.registerScreenHandlers();

		// Add blocks to creative tabs
		addBlocksToCreativeTabs();

		LOGGER.info("Forgero Blocks module initialized successfully");
	}

	/**
	 * Adds station blocks to the Tools creative tab.
	 */
	private void addBlocksToCreativeTabs() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
			entries.add(BlockRegistry.UPGRADE_STATION_ITEM);
			entries.add(BlockRegistry.ASSEMBLY_STATION_ITEM);
		});
		LOGGER.debug("Added Upgrade Station and Assembly Station to Tools creative tab");
	}
}
