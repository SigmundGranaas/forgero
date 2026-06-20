package com.sigmundgranaas.forgero.tests;

import java.util.HashSet;
import java.util.Set;

import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

/**
 * Verifies that the trades module injected Forgero schematic offers into the vanilla trade pools.
 * <p>
 * Trades are registered via Fabric's {@code TradeOfferHelper}, which appends factories to
 * {@link TradeOffers#PROFESSION_TO_LEVELED_TRADE} and {@link TradeOffers#WANDERING_TRADER_TRADES}.
 * This test resolves each relevant pool, runs its factories, and asserts the expected Forgero
 * schematics are sold (and that the dropped mattock schematics are not).
 */
public class SchematicTradesTest implements ForgeroGameTest {

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void weaponsmith_sells_refined_and_mastercrafted_schematics(TestContext context) {
		Set<String> ws3 = sellIds(context, professionPool(VillagerProfession.WEAPONSMITH, 3));
		context.assertTrue(ws3.contains("forgero:refined_sword_blade-schematic"),
				"Weaponsmith (3) should sell refined_sword_blade-schematic");

		Set<String> ws5 = sellIds(context, professionPool(VillagerProfession.WEAPONSMITH, 5));
		context.assertTrue(ws5.contains("forgero:mastercrafted_axe_head-schematic"),
				"Weaponsmith (5) should sell mastercrafted_axe_head-schematic");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void toolsmith_and_fletcher_sell_schematics(TestContext context) {
		Set<String> ts5 = sellIds(context, professionPool(VillagerProfession.TOOLSMITH, 5));
		context.assertTrue(ts5.contains("forgero:mastercrafted_pickaxe_head-schematic"),
				"Toolsmith (5) should sell mastercrafted_pickaxe_head-schematic");

		Set<String> fl3 = sellIds(context, professionPool(VillagerProfession.FLETCHER, 3));
		context.assertTrue(fl3.contains("forgero:refined_bow_limb-schematic"),
				"Fletcher (3) should sell refined_bow_limb-schematic");
		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void wandering_trader_sells_extended_schematics_and_no_mattocks(TestContext context) {
		Set<String> wt = sellIds(context, TradeOffers.WANDERING_TRADER_TRADES.get(2));

		// Representative tool, weapon and guard schematics should be present.
		context.assertTrue(wt.contains("forgero:katana_blade-schematic"),
				"Wandering trader should sell katana_blade-schematic");
		context.assertTrue(wt.contains("forgero:felling_axe_head-schematic"),
				"Wandering trader should sell felling_axe_head-schematic");
		context.assertTrue(wt.contains("forgero:tsuba_sword_guard-schematic"),
				"Wandering trader should sell tsuba_sword_guard-schematic");
		// The legacy scythe id was scythe_blade; current content classifies it as a head.
		context.assertTrue(wt.contains("forgero:scythe_head-schematic"),
				"Wandering trader should sell scythe_head-schematic (renamed from scythe_blade)");

		// Mattocks have no schematic in current content and must not be offered.
		context.assertFalse(wt.contains("forgero:pick_mattock_head-schematic"),
				"pick_mattock has no schematic and must not be a trade");
		context.assertFalse(wt.contains("forgero:cutter_mattock_head-schematic"),
				"cutter_mattock has no schematic and must not be a trade");
		context.complete();
	}

	private static TradeOffers.Factory[] professionPool(VillagerProfession profession, int level) {
		Int2ObjectMap<TradeOffers.Factory[]> byLevel = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(profession);
		return byLevel == null ? new TradeOffers.Factory[0] : byLevel.get(level);
	}

	/**
	 * Runs every factory in the pool and collects the registry ids of the items being sold.
	 * A villager is spawned to satisfy type-aware vanilla factories; vanilla factories that fail or
	 * return null in the bare test world are skipped (our own factories never throw at create time —
	 * they validate their items eagerly at registration).
	 */
	private static Set<String> sellIds(TestContext context, TradeOffers.Factory[] factories) {
		Set<String> ids = new HashSet<>();
		if (factories == null) {
			return ids;
		}
		Entity entity = context.spawnEntity(EntityType.VILLAGER, 2, 2, 2);
		Random random = context.getWorld().getRandom();
		for (TradeOffers.Factory factory : factories) {
			try {
				TradeOffer offer = factory.create(entity, random);
				if (offer != null) {
					ItemStack sell = offer.getSellItem();
					ids.add(Registries.ITEM.getId(sell.getItem()).toString());
				}
			} catch (Exception ignored) {
				// Vanilla factory not satisfiable in the empty test world; not our concern.
			}
		}
		return ids;
	}
}
