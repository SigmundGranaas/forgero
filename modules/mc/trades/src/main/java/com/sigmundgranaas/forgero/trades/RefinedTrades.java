package com.sigmundgranaas.forgero.trades;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

import net.minecraft.village.VillagerProfession;

/**
 * Refined-quality part schematics, sold by master-level (3) Toolsmiths and Weaponsmiths.
 */
final class RefinedTrades {
	private RefinedTrades() {
	}

	static void register() {
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 3, factories -> {
			factories.add(SchematicTrade.sell(26, "forgero:refined_hoe_head-schematic", 5, 20, 0.2f));
			factories.add(SchematicTrade.sell(32, "forgero:refined_pickaxe_head-schematic", 5, 20, 0.2f));
			factories.add(SchematicTrade.sell(29, "forgero:refined_shovel_head-schematic", 5, 20, 0.2f));
		});

		TradeOfferHelper.registerVillagerOffers(VillagerProfession.WEAPONSMITH, 3, factories -> {
			factories.add(SchematicTrade.sell(23, "forgero:refined_axe_head-schematic", 5, 20, 0.2f));
			factories.add(SchematicTrade.sell(19, "forgero:refined_sword_blade-schematic", 5, 20, 0.2f));
			factories.add(SchematicTrade.sell(14, "forgero:refined_sword_guard-schematic", 5, 20, 0.2f));
		});
	}
}
