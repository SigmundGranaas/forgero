package com.sigmundgranaas.forgero.trades;

import com.sigmundgranaas.forgero.common.api.ForgeroInitializedCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

/**
 * Registers villager and wandering-trader trades for Forgero schematics.
 * <p>
 * Vanilla/refined/mastercrafted schematics are sold via profession trades (Toolsmith, Weaponsmith,
 * Fletcher); extended weapon, tool and guard schematics are sold by the Wandering Trader.
 * <p>
 * Trades are registered from {@link ForgeroInitializedCallback} so that every referenced schematic
 * item is guaranteed to be present in {@code Registries.ITEM} — registration fails fast (throws)
 * if a referenced schematic is missing, rather than silently selling air.
 */
public class TradesInitializer implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Forgero Trades");

	@Override
	public void onInitialize() {
		// registerAndReplay: runs immediately if Forgero already initialized, otherwise once when it does.
		ForgeroInitializedCallback.registerAndReplay(services -> register());
	}

	private static void register() {
		RefinedTrades.register();
		MastercraftedTrades.register();
		BowTrades.register();
		WanderingTrades.register();
		LOGGER.info("Registered Forgero schematic trades (profession + wandering trader).");
	}
}
