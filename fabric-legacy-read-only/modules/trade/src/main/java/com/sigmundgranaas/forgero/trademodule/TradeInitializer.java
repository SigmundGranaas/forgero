package com.sigmundgranaas.forgero.trademodule;

import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.trademodule.util.BowTrades;
import com.sigmundgranaas.forgero.trademodule.util.MastercraftedTrades;
import com.sigmundgranaas.forgero.trademodule.util.RefinedTrades;
import com.sigmundgranaas.forgero.trademodule.util.WanderingTrades;


/**
 * Register trades for Forgero schematics.
 * <p></p>
 * Vanilla and refined/mastercrafted are available via normal profession trades.
 * <p></p>
 * Extended weapons and tools are only available via the wandering trader.
 */
public class TradeInitializer implements ForgeroPreInitializationEntryPoint {
	@Override
	public void onPreInitialization() {
		// Bow module
		BowTrades.registerCustomTrades();

		// Mastercrafted / refined
		RefinedTrades.registerCustomTrades();
		MastercraftedTrades.registerCustomTrades();

		// Extended
		WanderingTrades.registerCustomTrades();
	}
}
