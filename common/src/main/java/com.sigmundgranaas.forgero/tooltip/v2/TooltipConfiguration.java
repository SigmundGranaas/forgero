package com.sigmundgranaas.forgero.tooltip.v2;

import static com.sigmundgranaas.forgero.core.property.attribute.Category.UPGRADE_CATEGORIES;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.state.State;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;


@Data()
@Accessors(fluent = true)
@Builder(toBuilder = true)
public class TooltipConfiguration {
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
	private Set<Category> upgradeCategories = UPGRADE_CATEGORIES;
	@Builder.Default
	private Set<String> hiddenFeatureTypes = Set.of("EFFECTIVE_BLOCKS");
	@Builder.Default
	private boolean padded = false;


	public List<String> writableAttributes(PropertyContainer container) {
		return filteredAttributes(container).orElseGet(() -> TooltipAttributeRegistry.getWritableAttributes(container));
	}

	public Optional<List<String>> filteredAttributes(PropertyContainer container) {
		if (container instanceof State state) {
			return Optional.of(TooltipAttributeRegistry.getFilterForType(state.type())).filter(collection -> !collection.isEmpty());
		}
		return Optional.empty();
	}
}
