package com.sigmundgranaas.forgero.trademodule.util;


import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

public class RefinedTrades {

	public static void registerCustomTrades() {
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 3,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 26),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_hoe_head-schematic")), 1),
							5, 20, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 32),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_pickaxe_head-schematic")), 1),
							5, 20, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 29),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_shovel_head-schematic")), 1),
							5, 20, 0.2f));

				});

		TradeOfferHelper.registerVillagerOffers(VillagerProfession.WEAPONSMITH, 3,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 23),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_axe_head-schematic")), 1),
							5, 20, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 19),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_sword_blade-schematic")), 1),
							5, 20, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 14),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_sword_guard-schematic")), 1),
							5, 20, 0.2f));
				});
	}
}
