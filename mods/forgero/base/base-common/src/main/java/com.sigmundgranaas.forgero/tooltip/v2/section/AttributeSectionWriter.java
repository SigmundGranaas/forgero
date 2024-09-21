package com.sigmundgranaas.forgero.tooltip.v2.section;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Reach;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.tooltip.v2.AttributeWriterHelper;
import com.sigmundgranaas.forgero.tooltip.v2.TooltipConfiguration;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

public class AttributeSectionWriter extends SectionWriter {

	private static final String sectionTranslationKey = "attributes";
	private final PropertyContainer container;

	private final AttributeWriterHelper helper;

	public AttributeSectionWriter(PropertyContainer container, TooltipConfiguration configuration) {
		super(configuration);
		this.container = container;
		this.helper = new AttributeWriterHelper(container, configuration);
		if (container instanceof State state && ForgeroStateRegistry.STATES != null) {
			this.helper.setComparativeContainer(state.strip());
		}
	}

	public static Optional<SectionWriter> of(PropertyContainer container) {
		return of(container, TooltipConfiguration.builder().build());
	}

	public static Optional<SectionWriter> of(PropertyContainer container, TooltipConfiguration configuration) {
		SectionWriter writer = new AttributeSectionWriter(container, configuration);
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
		return !entries().isEmpty();
	}

	@Override
	public int getSectionOrder() {
		return 1;
	}

	@Override
	public List<Text> entries() {
		return configuration.writableAttributes(container).stream().map(this::entry).flatMap(List::stream).toList();
	}

	protected List<Text> entry(String attributeType) {
		Optional<Attribute> attributeOpt = helper.attributesOfType(attributeType)
				.filter(attribute -> attribute.getContext().test(Contexts.UNDEFINED))
				.findFirst();
		if (attributeOpt.isPresent()) {
			if (shouldIgnoreAttribute(attributeOpt.get())) {
				return Collections.emptyList();
			}

			ComputedAttribute attribute = ComputedAttribute.of(container, attributeType);

			var builder = ImmutableList.<Text>builder();
			builder.add(helper.writeBaseNumber(attribute));
			builder.addAll(helper.writeTarget(attributeOpt.get()));
			return builder.build();

		}

		return Collections.emptyList();
	}

	private boolean shouldIgnoreAttribute(Attribute attribute) {
		if (configuration.hideZeroValues() && !configuration.showDetailedInfo() && attribute.leveledValue() == 0f) {
			return true;
		}

		if (attribute.getAttributeType().equals(Reach.KEY) && attribute.getValue() == 4.5) {
			return true;
		}

		return false;
	}
}
