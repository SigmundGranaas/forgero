package com.sigmundgranaas.forgero.trades;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

/**
 * Extended tool, weapon and guard schematics, sold by the Wandering Trader (level 2 pool).
 * <p>
 * Ported from the legacy trade module. Two legacy entries — {@code pick_mattock_head} and
 * {@code cutter_mattock_head} — are intentionally omitted because no mattock schematic exists in the
 * current content; including them would reference unregistered items. The legacy {@code scythe_blade}
 * id is now {@code scythe_head} (the scythe schematic is classified as a head).
 */
final class WanderingTrades {
	private WanderingTrades() {
	}

	static void register() {
		tools();
		weapons();
		guards();
	}

	private static void tools() {
		TradeOfferHelper.registerWanderingTraderOffers(2, factories -> {
			factories.add(SchematicTrade.sell(38, "forgero:felling_axe_head-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(41, "forgero:hammer_head-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(38, "forgero:entrenching_shovel_head-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(45, "forgero:mandrill_pickaxe_head-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(27, "forgero:spade_head-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(41, "forgero:scythe_head-schematic", 1, 30, 0.2f));
		});
	}

	private static void weapons() {
		TradeOfferHelper.registerWanderingTraderOffers(2, factories -> {
			factories.add(SchematicTrade.sell(29, "forgero:club_head-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(33, "forgero:cutlass_blade-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(36, "forgero:katana_blade-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(37, "forgero:battle_axe_head-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(32, "forgero:broadsword_blade-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(16, "forgero:kunai_blade-schematic", 5, 30, 0.2f));
			factories.add(SchematicTrade.sell(32, "forgero:mace_head-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(23, "forgero:rapier_blade-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(28, "forgero:sickle_blade-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(43, "forgero:spear_head-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(37, "forgero:war_hammer_head-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(18, "forgero:knife_blade-schematic", 5, 30, 0.2f));
		});
	}

	private static void guards() {
		TradeOfferHelper.registerWanderingTraderOffers(2, factories -> {
			factories.add(SchematicTrade.sell(18, "forgero:cruciform_sword_guard-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(22, "forgero:half_basket_sword_guard-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(16, "forgero:mechanized_sword_guard-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(19, "forgero:rounded_sword_guard-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(27, "forgero:shell_sword_guard-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(21, "forgero:swept_sword_guard-schematic", 1, 30, 0.2f));
			factories.add(SchematicTrade.sell(25, "forgero:tsuba_sword_guard-schematic", 1, 30, 0.2f));
		});
	}
}
