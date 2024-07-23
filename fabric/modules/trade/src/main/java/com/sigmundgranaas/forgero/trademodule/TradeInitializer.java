package com.sigmundgranaas.forgero.trademodule;

import com.sigmundgranaas.forgero.fabric.initialization.ForgeroPostInit;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.trademodule.util.ForgeroCustomTrades;


public class TradeInitializer extends ForgeroPostInit {


	@Override
	public void onInitialized(StateService stateService) {
		ForgeroCustomTrades.registerCustomTrades();
	}
}
