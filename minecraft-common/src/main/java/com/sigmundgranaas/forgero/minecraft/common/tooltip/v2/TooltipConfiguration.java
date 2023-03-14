package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import static com.sigmundgranaas.forgero.core.property.attribute.Category.UPGRADE_CATEGORIES;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Armor;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningLevel;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;


@Data()
@Accessors(fluent = true)
@Builder(toBuilder = true)
public class TooltipConfiguration {
	public static final List<String> WRITABLE_ATTRIBUTES = List.of(AttackDamage.KEY, MiningSpeed.KEY, Durability.KEY, MiningLevel.KEY, AttackSpeed.KEY, Armor.KEY, Weight.KEY);

	@Builder.Default
	private boolean hideSectionTitle = false;
	@Builder.Default
	private int baseIndent = 0;
	@Builder.Default
	private int sectionOrder = 0;
	@Builder.Default
	private boolean hideZeroValues = true;
	@Builder.Default
	private boolean showExtendedInfo = false;
	@Builder.Default
	private boolean showDetailedInfo = false;
	@Builder.Default
	private List<String> writableAttributes = WRITABLE_ATTRIBUTES;
	@Builder.Default
	private Set<Category> upgradeCategories = UPGRADE_CATEGORIES;
	@Builder.Default
	private Set<String> hiddenFeatureTypes = Set.of("EFFECTIVE_BLOCKS");
	@Builder.Default
	private boolean padded = false;

	public List<String> writableAttributes() {
		if (ForgeroConfigurationLoader.configuration.hideRarity) {
			return writableAttributes;
		} else {
			return ImmutableList.<String>builder().addAll(writableAttributes).add("RARITY").build();
		}
	}
}
