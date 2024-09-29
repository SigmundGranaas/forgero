package com.sigmundgranaas.forgero.trade.util;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;

public class ForgeroCustomTrades {
	public static void registerCustomTrades() {
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.FLETCHER, 3,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 5),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_arrow_head-schematic")), 1),
							12, 10, 0.2f
					));
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 30),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_bow_limb-schematic")), 1),
							5, 20, 0.2f
					));
				}
		);

		TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 3,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 26),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_hoe_head-schematic")), 1),
							5, 20, 0.2f
					));
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 32),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_pickaxe_head-schematic")), 1),
							5, 20, 0.2f
					));
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 29),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_shovel_head-schematic")), 1),
							5, 20, 0.2f
					));
				}
		);

		TradeOfferHelper.registerVillagerOffers(VillagerProfession.WEAPONSMITH, 3,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 23),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_axe_head-schematic")), 1),
							5, 20, 0.2f
					));
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 19),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_sword_blade-schematic")), 1),
							5, 20, 0.2f
					));
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 14),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_sword_guard-schematic")), 1),
							5, 20, 0.2f
					));
				}
		);

		TradeOfferHelper.registerVillagerOffers(VillagerProfession.WEAPONSMITH, 5,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 47),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_axe_head-schematic")), 1),
							3, 40, 0.2f
					));
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 42),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_sword_blade-schematic")), 1),
							3, 40, 0.2f
					));
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 31),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_sword_guard-schematic")), 1),
							3, 40, 0.2f
					));
				}
		);

		TradeOfferHelper.registerVillagerOffers(VillagerProfession.TOOLSMITH, 5,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 44),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_hoe_head-schematic")), 1),
							3, 40, 0.2f
					));
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 49),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_pickaxe_head-schematic")), 1),
							3, 40, 0.2f
					));
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 40),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_shovel_head-schematic")), 1),
							3, 40, 0.2f
					));
				}
		);

		TradeOfferHelper.registerVillagerOffers(VillagerProfession.FLETCHER, 5,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 12),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_arrow_head-schematic")), 1),
							12, 30, 0.2f
					));
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 54),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mastercrafted_bow_limb-schematic")), 1),
							3, 40, 0.2f
					));
				}
		);
	}
}
