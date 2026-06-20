package com.sigmundgranaas.forgero.trades;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

import net.minecraft.village.VillagerProfession;

/**
 * Bow and arrow part schematics, sold by the Fletcher at master (3) and max (5) level.
 */
final class BowTrades {
	private BowTrades() {
	}

	static void register() {
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.FLETCHER, 3, factories -> {
			factories.add(SchematicTrade.sell(5, "forgero:refined_arrow_head-schematic", 12, 10, 0.2f));
			factories.add(SchematicTrade.sell(30, "forgero:refined_bow_limb-schematic", 5, 20, 0.2f));
		});

		TradeOfferHelper.registerVillagerOffers(VillagerProfession.FLETCHER, 5, factories -> {
			factories.add(SchematicTrade.sell(12, "forgero:mastercrafted_arrow_head-schematic", 12, 30, 0.2f));
			factories.add(SchematicTrade.sell(54, "forgero:mastercrafted_bow_limb-schematic", 3, 40, 0.2f));
		});
	}
}
