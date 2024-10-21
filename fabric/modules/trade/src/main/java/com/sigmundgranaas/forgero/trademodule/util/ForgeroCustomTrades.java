package com.sigmundgranaas.forgero.trademodule.util;


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
							12, 10, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 30),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:refined_bow_limb-schematic")), 1),
							5, 20, 0.2f));

				});

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

		TradeOfferHelper.registerWanderingTraderOffers(1,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 38),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:felling_axe_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 41),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:hammer_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 29),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:club_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 33),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:cutlass_blade-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 36),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:katana_blade-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 18),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:cruciform_sword_guard-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 22),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:half_basket_sword_guard-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 16),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mechanized_sword_guard-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 19),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:rounded_sword_guard-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 37),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:battle_axe_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 32),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:broadsword_blade-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 38),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:entrenching_shovel_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 16),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:kunai_blade-schematic")), 1),
							5, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 26),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:pick_mattock_head-schematic")), 1),
							1, 30, 0.2f));
				});

		TradeOfferHelper.registerWanderingTraderOffers(2,
				factories -> {
					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 45),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mandrill_pickaxe_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 27),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:spade_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 32),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mace_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 23),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:rapier_blade-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 27),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:shell_sword_guard-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 21),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:swept_sword_guard-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 25),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:tsuba_sword_guard-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 41),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:scythe_blade-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 28),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:sickle_blade-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 43),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:spear_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 37),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:war_hammer_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 18),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:knife_blade-schematic")), 1),
							5, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 48),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:cutter_mattock_head-schematic")), 1),
							1, 30, 0.2f));
				});

    }
}
