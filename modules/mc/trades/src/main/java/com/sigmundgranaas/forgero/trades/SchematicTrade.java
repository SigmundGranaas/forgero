package com.sigmundgranaas.forgero.trades;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;

/**
 * Builds a "sell schematic for emeralds" {@link TradeOffers.Factory}.
 * <p>
 * The schematic item is resolved and validated <em>eagerly</em> at registration time. If the id is
 * not registered (e.g. a content pack is missing from this runtime) registration throws instead of
 * producing a trade that sells {@link Items#AIR} — fail fast, surface the missing content.
 */
final class SchematicTrade {
	private SchematicTrade() {
	}

	static TradeOffers.Factory sell(int emeralds, String schematicId, int maxUses, int experience, float priceMultiplier) {
		Item schematic = resolveOrThrow(schematicId);
		return (entity, random) -> new TradeOffer(
				new ItemStack(Items.EMERALD, emeralds),
				new ItemStack(schematic, 1),
				maxUses, experience, priceMultiplier);
	}

	private static Item resolveOrThrow(String schematicId) {
		Item item = Registries.ITEM.get(new Identifier(schematicId));
		if (item == Items.AIR) {
			throw new IllegalStateException(
					"Forgero trades referenced a schematic item that is not registered: '" + schematicId
							+ "'. The content pack providing it is missing from this runtime.");
		}
		return item;
	}
}
