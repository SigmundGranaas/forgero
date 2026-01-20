package com.sigmundgranaas.forgero.blocks.api;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.common.api.ForgeroServices;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Dependency injection container for station operations.
 * <p>
 * This record encapsulates all services needed by station screen handlers,
 * replacing the singleton pattern (StateService.INSTANCE) with constructor injection.
 * <p>
 * Usage:
 * <pre>{@code
 * // Create context when opening station
 * StationContext context = StationContext.create(services, world, pos);
 *
 * // Use in screen handler constructor
 * public UpgradeStationScreenHandler(int syncId, PlayerInventory inv, StationContext context) {
 *     this.context = context;
 *     // Use context.converter(), context.slotManager(), etc.
 * }
 * }</pre>
 *
 * @param services  The main Forgero services facade
 * @param converter Component to ItemStack converter
 * @param slotManager Slot operations service
 * @param world     The world where the station is located
 * @param pos       The position of the station block
 */
public record StationContext(
		ForgeroServices services,
		ComponentConverter converter,
		SlotManager slotManager,
		World world,
		BlockPos pos
) {

	/**
	 * Creates a StationContext with all services derived from ForgeroServices.
	 *
	 * @param services The Forgero services facade
	 * @param world    The world where the station is located
	 * @param pos      The position of the station block
	 * @return A fully initialized StationContext
	 */
	public static StationContext create(ForgeroServices services, World world, BlockPos pos) {
		return new StationContext(
				services,
				services.converter(),
				services.slotManager(),
				world,
				pos
		);
	}

	/**
	 * Checks if this context is on the server side.
	 *
	 * @return true if on server, false if on client
	 */
	public boolean isServer() {
		return world != null && !world.isClient();
	}

	/**
	 * Checks if this context is on the client side.
	 *
	 * @return true if on client, false if on server
	 */
	public boolean isClient() {
		return world != null && world.isClient();
	}
}
