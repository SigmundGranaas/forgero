package com.sigmundgranaas.forgero.trademodule.util;


import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;

public class BowTrades {

	public static void registerCustomTrades() {
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.FLETCHER, 3,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 5),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_arrow_head-schematic")), 1),
							12, 10, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 30),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_bow_limb-schematic")), 1),
							5, 20, 0.2f));

				});

		TradeOfferHelper.registerVillagerOffers(VillagerProfession.FLETCHER, 5,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 12),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_arrow_head-schematic")), 1),
							12, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 54),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_bow_limb-schematic")), 1),
							3, 40, 0.2f));

				});
	}
}
