package com.sigmundgranaas.forgero.trademodule.util;


import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;

public class MastercraftedTrades {

	public static void registerCustomTrades() {
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.WEAPONSMITH, 5,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 47),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_axe_head-schematic")), 1),
							3, 40, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 42),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_sword_blade-schematic")), 1),
							3, 40, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 31),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_sword_guard-schematic")), 1),
							3, 40, 0.2f));
				});

		TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 5,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 44),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_hoe_head-schematic")), 1),
							3, 40, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 49),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_pickaxe_head-schematic")), 1),
							3, 40, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 40),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_shovel_head-schematic")), 1),
							3, 40, 0.2f));

				});
	}
}
