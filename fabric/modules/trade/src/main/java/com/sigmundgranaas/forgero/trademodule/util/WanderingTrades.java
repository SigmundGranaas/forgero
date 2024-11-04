package com.sigmundgranaas.forgero.trademodule.util;


import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

public class WanderingTrades {

	/**
	 * Registers the following schematics as possible trades for the Wandering trader.
	 * <p></p>
	 *
	 * Tools
	 * <ul>
	 *     <li>Felling axe</li>
	 *     <li>Hammer</li>
	 *      <li>Entrenching shovel</li>
	 *      <li>Mandrill pickaxe</li>
	 *      <li>Spade</li>
	 *      <li>Scythe</li>
	 *      <li>Pick mattock</li>
	 *      <li>Cutter mattock</li>
	 * </ul>
	 *
	 * Weapons
	 * <ul>
	 *     <li>Club</li>
	 *     <li>Cutlass</li>
	 *      <li>Katana</li>
	 *      <li>Battle axe</li>
	 *      <li>Broadsword</li>
	 *      <li>Kunai</li>
	 *      <li>Mace</li>
	 *      <li>Rapier</li>
	 *      <li>Sicle</li>
	 *      <li>Spear</li>
	 *      <li>War hammer</li>
	 *      <li>Knife</li>
	 * </ul>
	 *
	 * Guards
	 * <ul>
	 *     <li>Cruciform</li>
	 *     <li>Half basket</li>
	 *      <li>Mechanized</li>
	 *      <li>Rounded</li>
	 *      <li>Shell</li>
	 *      <li>Swept</li>
	 *      <li>Tsuba</li>
	 * </ul>
	 */
	public static void registerCustomTrades() {

	}

	public static void tools(){
		TradeOfferHelper.registerWanderingTraderOffers(2,
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
							new ItemStack(Items.EMERALD, 38),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:entrenching_shovel_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 45),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mandrill_pickaxe_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 26),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:pick_mattock_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 27),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:spade_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 41),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:scythe_blade-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 48),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:cutter_mattock_head-schematic")), 1),
							1, 30, 0.2f));
				});
	}

	public static void weapons(){
		TradeOfferHelper.registerWanderingTraderOffers(2,
				factories -> {
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
							new ItemStack(Items.EMERALD, 37),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:battle_axe_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 32),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:broadsword_blade-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 16),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:kunai_blade-schematic")), 1),
							5, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 32),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:mace_head-schematic")), 1),
							1, 30, 0.2f));

					factories.add((entity, random) -> new TradeOffer(
							new ItemStack(Items.EMERALD, 23),
							new ItemStack(Registries.ITEM.get(new Identifier("forgero:rapier_blade-schematic")), 1),
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

				});
	}

	public static void guards(){
		TradeOfferHelper.registerWanderingTraderOffers(2,
				factories -> {
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
				});
	}
}
