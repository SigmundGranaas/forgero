package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.AttributeWriterHelper;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.*;

public class UpgradeAttributeSectionWriter extends SectionWriter {
	public static final Set<Category> UPGRADE_CATEGORIES = Set.of(Category.UTILITY, Category.DEFENSIVE, Category.OFFENSIVE, Category.ALL);
	private static final String sectionTranslationKey = "upgrade_attributes";
	private final AttributeWriterHelper helper;
	private final PropertyContainer container;

	public UpgradeAttributeSectionWriter(PropertyContainer container, TooltipConfiguration configuration) {
		super(configuration);
		this.helper = new AttributeWriterHelper(container, configuration);
		this.container = container;
	}

	public static Optional<SectionWriter> of(PropertyContainer container) {
		return of(container, TooltipConfiguration.builder().build());
	}

	public static Optional<SectionWriter> of(PropertyContainer container, TooltipConfiguration configuration) {
		SectionWriter writer = new UpgradeAttributeSectionWriter(container, configuration);
		if (writer.shouldWrite()) {
			return Optional.of(writer);
		}
		return Optional.empty();
	}

	@Override
	public void write(List<Text> tooltip, TooltipContext context) {
		tooltip.add(createSection(sectionTranslationKey));
		tooltip.addAll(entries());

		super.write(tooltip, context);
	}


	@Override
	public boolean shouldWrite() {
		return container.stream()
				.getAttributes()
				.map(Attribute::getCategory)
				.anyMatch(UPGRADE_CATEGORIES::contains);
	}

	@Override
	public int getSectionOrder() {
		return 1;
	}

	@Override
	public List<Text> entries() {
		return configuration.upgradeCategories().stream().map(this::category).flatMap(List::stream).toList();
	}

	protected List<Text> category(Category category) {
		List<Text> entries = configuration.writableAttributes(container).stream().map(attribute -> entry(attribute, category)).flatMap(List::stream).toList();
		if (!entries.isEmpty()) {
			var builder = ImmutableList.<Text>builder();
			Text section = indented(1).append(createSection(category.toString().toLowerCase(Locale.ENGLISH)));
			if (category != Category.ALL) {
				builder.add(section);
			}
			return builder.addAll(entries).build();
		}
		return Collections.emptyList();
	}

	protected List<Text> entry(String attributeType, Category category) {
		return helper.attributesOfType(attributeType)
				.filter(attribute -> category.equals(attribute.getCategory()))
				.filter(att -> att.leveledValue() != 0)
				.sorted(Attribute::compareTo)
				.map(this::attributeEntry)
				.flatMap(List::stream)
				.toList();
	}

	protected List<Text> attributeEntry(Attribute attribute) {
		if (configuration.hideZeroValues() && !configuration.showDetailedInfo() && attribute.getValue() == 0) {
			return Collections.emptyList();
		}
		var builder = ImmutableList.<Text>builder();
		if (attribute.getOperation() == NumericOperation.MULTIPLICATION) {
			builder.add(helper.writePercentageAttribute(attribute));
			builder.addAll(helper.writeTarget(attribute));
		} else {
			builder.add(helper.writeAdditionAttribute(attribute));
			builder.addAll(helper.writeTarget(attribute));
		}
		return builder.build();
	}
}
