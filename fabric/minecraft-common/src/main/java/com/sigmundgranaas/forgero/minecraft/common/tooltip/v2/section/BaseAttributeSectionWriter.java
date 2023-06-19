package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.state.composite.Constructed;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.AttributeWriterHelper;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

public class BaseAttributeSectionWriter extends SectionWriter {

	private static final String sectionTranslationKey = "base_attributes";
	private final PropertyContainer container;

	private final AttributeWriterHelper helper;

	public BaseAttributeSectionWriter(PropertyContainer container, TooltipConfiguration configuration) {
		super(configuration);
		this.container = container;
		this.helper = new AttributeWriterHelper(container, configuration);
	}

	public static Optional<SectionWriter> of(PropertyContainer container) {
		return of(container, TooltipConfiguration.builder().build());
	}

	public static Optional<SectionWriter> of(PropertyContainer container, TooltipConfiguration configuration) {
		SectionWriter writer = new BaseAttributeSectionWriter(container, configuration);
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
				.anyMatch(attribute -> attribute.getContext().test(Contexts.COMPOSITE)) && !(container instanceof Constructed);
	}

	@Override
	public int getSectionOrder() {
		return 1;
	}

	@Override
	public List<Text> entries() {
		return configuration.writableAttributes().stream().map(this::entry).flatMap(List::stream).toList();
	}

	protected List<Text> entry(String attributeType) {
		Optional<Attribute> attributeOpt = helper.attributesOfType(attributeType).filter(attribute -> attribute.getContext().test(Contexts.COMPOSITE)).findFirst();
		if (attributeOpt.isPresent() && attributeOpt.get().leveledValue() != 0) {
			Attribute attribute = attributeOpt.get();
			if (attribute.getOperation() == NumericOperation.MULTIPLICATION) {
				var builder = ImmutableList.<Text>builder();
				builder.add(helper.writeMultiplicativeAttribute(attribute));
				helper.writeTarget(attribute).ifPresent(builder::add);
				return builder.build();
			} else {
				var builder = ImmutableList.<Text>builder();
				builder.add(helper.writeBaseNumber(attribute));
				helper.writeTarget(attribute).ifPresent(builder::add);
				return builder.build();
			}
		}
		return Collections.emptyList();
	}
}
