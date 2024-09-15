package com.sigmundgranaas.forgero.core.customdata.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.customdata.ClassBasedVisitor;
import com.sigmundgranaas.forgero.core.customdata.Context;
import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.customdata.DataVisitor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * A {@link DataVisitor} for {@link CustomLootData}.
 * {@link CustomLootData} is used to exclude resources from a loot table.
 * <p>
 * This visitor is used to extract {@link CustomLootData} from a {@link DataContainer}.
 * It will check if a loot table is excluded from this resource.
 */
public class LootVisitor extends ClassBasedVisitor<LootVisitor.CustomLootData> {
	public static final String KEY = "loot";

	public LootVisitor() {
		super(CustomLootData.class, KEY);
	}

	@Data
	@Accessors(fluent = true)
	public static class CustomLootData {
		Context context = Context.LOCAL;
		@SerializedName(value = "excluded_loot_tables", alternate = {"excludedLootTables", "excluded"})
		Set<String> excludedLootTables = Collections.emptySet();
		Set<String> onlyLootTables = Collections.emptySet();

		/**
		 * Checks if the loot table is excluded from this resource.
		 *
		 * @param lootTable the loot table to check
		 * @return true if the loot table is excluded
		 */
		public boolean isExcluded(String lootTable) {
			if (onlyLootTables.isEmpty()) {
				if (excludedLootTables.isEmpty()) {
					return false;
				} else {
					return excludedLootTables.contains(lootTable);
				}
			} else {
				return !onlyLootTables.contains(lootTable);
			}
		}

		/**
		 * Checks if the loot table is excluded from this resource.
		 *
		 * @param lootTables the loot tables to check
		 * @return true if the loot table is excluded
		 */
		public boolean isExcluded(Collection<String> lootTables) {
			return lootTables.stream().anyMatch(this::isExcluded);
		}
	}
}
