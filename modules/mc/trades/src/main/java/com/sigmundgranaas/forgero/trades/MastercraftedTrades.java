package com.sigmundgranaas.forgero.trades;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

import net.minecraft.village.VillagerProfession;

/**
 * Mastercrafted-quality part schematics, sold by max-level (5) Weaponsmiths and Toolsmiths.
 */
final class MastercraftedTrades {
	private MastercraftedTrades() {
	}

	static void register() {
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.WEAPONSMITH, 5, factories -> {
			factories.add(SchematicTrade.sell(47, "forgero:mastercrafted_axe_head-schematic", 3, 40, 0.2f));
			factories.add(SchematicTrade.sell(42, "forgero:mastercrafted_sword_blade-schematic", 3, 40, 0.2f));
			factories.add(SchematicTrade.sell(31, "forgero:mastercrafted_sword_guard-schematic", 3, 40, 0.2f));
		});

		TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 5, factories -> {
			factories.add(SchematicTrade.sell(44, "forgero:mastercrafted_hoe_head-schematic", 3, 40, 0.2f));
			factories.add(SchematicTrade.sell(49, "forgero:mastercrafted_pickaxe_head-schematic", 3, 40, 0.2f));
			factories.add(SchematicTrade.sell(40, "forgero:mastercrafted_shovel_head-schematic", 3, 40, 0.2f));
		});
	}
}
