package com.sigmundgranaas.forgero.trademodule;

import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.trademodule.util.ForgeroCustomTrades;


public class TradeInitializer implements ForgeroPreInitializationEntryPoint {


	@Override
	public void onPreInitialization() {

		ForgeroCustomTrades.registerCustomTrades();


	}


}
