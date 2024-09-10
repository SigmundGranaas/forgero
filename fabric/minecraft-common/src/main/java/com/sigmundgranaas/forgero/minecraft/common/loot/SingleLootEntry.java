package com.sigmundgranaas.forgero.minecraft.common.loot;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.customdata.VisitorHelper;
import com.sigmundgranaas.forgero.core.customdata.handler.LootVisitor;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.LootEntryData;
import com.sigmundgranaas.forgero.minecraft.common.loot.function.ConditionFunction;
import com.sigmundgranaas.forgero.minecraft.common.loot.function.GemLevelFunction;
import lombok.Builder;
import lombok.Data;

import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.BinomialLootNumberProvider;
import net.minecraft.util.Identifier;

@Data
@Builder
public class SingleLootEntry implements LootEntry {
	private List<Identifier> target;
	private Supplier<List<Item>> filter;
	private int weight;
	@Builder.Default
	private int rolls = 1;
	@Builder.Default
	private float chance = 1f;

	public static LootEntry of(LootEntryData data) {
		StateFilter filter = StateFilter.builder()
				.types(data.types())
				.ids(data.ids())
				.lowerRarity(data.lower_rarity_limit())
				.upperRarity(data.upper_rarity_limit())
				.build();

		return SingleLootEntry.builder()
				.chance(data.chance())
				.rolls(data.rolls())
				.filter(filter::filter)
				.target(data.targets().stream().map(Identifier::new).toList())
				.weight(1)
				.build();
	}

	@Override
	public void apply(LootTable.Builder builder) {
		var pool = LootPool.builder()
				.rolls(BinomialLootNumberProvider.create(rolls, chance));
		filter.get().stream().filter(this::filter).forEach(item -> pool.with(ItemEntry.builder(item).apply(new GemLevelFunction.Builder()).apply(new ConditionFunction.Builder()).weight(1)));
		builder.pool(pool);
	}

	@Override
	public boolean matches(Identifier id) {
		return target.stream().anyMatch(target -> target.equals(id));
	}


	private boolean filter(Item item) {
		Optional<LootVisitor.CustomLootData> customLootData = VisitorHelper.of(item, LootVisitor::new);
		//noinspection RedundantIfStatement
		if (customLootData.isPresent() && customLootData.get().isExcluded(target.stream().map(Identifier::toString).toList())) {
			return false;
		}
		return true;
	}
}
