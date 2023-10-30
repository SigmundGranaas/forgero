package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.attribute.BaseAttribute;
import com.sigmundgranaas.forgero.core.state.composite.Constructed;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.AttributeWriterHelper;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
		return configuration.writableAttributes(container).stream()
				.map(this::entry)
				.flatMap(List::stream)
				.toList();
	}

	protected List<Text> entry(String attributeType) {
		List<Attribute> compressible = helper.attributesOfType(attributeType)
				.sorted(Attribute::compareTo)
				.filter(attribute -> attribute.getPredicate().equals(Matchable.DEFAULT_TRUE))
				.reduce(this::combine)
				.map(List::of)
				.orElse(Collections.emptyList());

		List<Attribute> uniques = helper.attributesOfType(attributeType)
				.filter(attribute -> !attribute.getPredicate().equals(Matchable.DEFAULT_TRUE))
				.sorted(Attribute::compareTo)
				.toList();

		return Stream.concat(compressible.stream(), uniques.stream())
				.filter(attribute -> attribute.getContext().test(Contexts.COMPOSITE))
				.filter(att -> att.leveledValue() != 0)
				.sorted(Attribute::compareTo)
				.map(this::attributeEntry)
				.flatMap(List::stream)
				.toList();
	}

	protected List<Text> attributeEntry(Attribute attribute) {
		if (attribute.getOperation() == NumericOperation.MULTIPLICATION) {
			var builder = ImmutableList.<Text>builder();
			builder.add(helper.writeMultiplicativeAttribute(attribute));
			builder.addAll(helper.writeTarget(attribute));
			return builder.build();
		} else {
			var builder = ImmutableList.<Text>builder();
			builder.add(helper.writeBaseNumber(attribute));
			builder.addAll(helper.writeTarget(attribute));
			return builder.build();
		}
	}

	private Attribute combine(Attribute a1, Attribute a2) {
		if (a1.getOperation() == a2.getOperation()) {
			if (a1.getOperation() == NumericOperation.MULTIPLICATION) {
				return new BaseAttribute(a1.getAttributeType(), NumericOperation.MULTIPLICATION, a1.leveledValue() * a2.leveledValue(), a1.getPredicate(), a1.getOrder(), a1.getLevel(), a1.getCategory(), a1.getId() + a2.getId(), a1.targets(), a1.targetType(), a1.getPriority(), a1.getContext(), a1.source().orElse(null));
			} else {
				return new BaseAttribute(a1.getAttributeType(), NumericOperation.ADDITION, a1.leveledValue() + a2.leveledValue(), a1.getPredicate(), a1.getOrder(), a1.getLevel(), a1.getCategory(), a1.getId() + a2.getId(), a1.targets(), a1.targetType(), a1.getPriority(), a1.getContext(), a1.source().orElse(null));
			}
		} else {
			return new BaseAttribute(a1.getAttributeType(), NumericOperation.ADDITION, a1.leveledValue() * a2.leveledValue(), a1.getPredicate(), a1.getOrder(), a1.getLevel(), a1.getCategory(), a1.getId() + a2.getId(), a1.targets(), a1.targetType(), a1.getPriority(), a1.getContext(), a1.source().orElse(null));
		}
	}
}
