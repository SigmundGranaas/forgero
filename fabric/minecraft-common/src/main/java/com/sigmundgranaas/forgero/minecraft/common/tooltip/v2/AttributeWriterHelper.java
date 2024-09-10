package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackSpeed;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttributeHelper;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.difference.DifferenceHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class AttributeWriterHelper extends BaseWriter {
	private final PropertyContainer container;
	private final AttributeHelper helper;
	private final TooltipConfiguration configuration;
	private final DifferenceHelper differenceHelper = new DifferenceHelper();
	@Nullable
	private PropertyContainer comparativeContainer;

	public AttributeWriterHelper(PropertyContainer container, TooltipConfiguration configuration) {
		this.container = container;
		this.helper = new AttributeHelper(container);
		this.configuration = configuration;
	}

	public static String number(float attribute) {
		if (Math.round(attribute) == attribute || roundFloat(attribute) == Math.round(attribute)) {
			return String.valueOf(Math.round(attribute));
		}
		return String.valueOf(roundFloat(attribute));
	}

	public static float roundFloat(float number) {
		NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
		format.setMaximumFractionDigits(2);
		try {
			return Float.parseFloat(format.format(number));
		} catch (NumberFormatException e) {
			return 1f;
		}
	}

	public void setComparativeContainer(@Nullable PropertyContainer comparativeContainer) {
		this.comparativeContainer = comparativeContainer;
	}

	public MutableText writePercentageAttribute(Attribute attribute) {
		return writePercentageAttribute(attribute, 1);
	}

	public MutableText writePercentageAttribute(Attribute attribute, int indent) {
		return
				writeAttributeType(attribute.type(), indent)
				.append(indented(1))
				.append(percentageNumberText(attribute));
	}

	public MutableText writeBaseNumber(Attribute attribute) {
		float value = attribute.leveledValue();
		if (attribute.getAttributeType().equals(AttackSpeed.KEY) && (isPartHead(container) || (isSchematic(container)) && value < -1)) {
			value = 4 + value;
		}

		return writeAttributeType(attribute.type())
				.append(indented(1))
				.append(number(value));
	}

	private boolean isPartHead(Object o) {
		return o instanceof Matchable matchable && (matchable.test(Type.WEAPON_HEAD) || matchable.test(Type.TOOL_PART_HEAD));
	}

	private boolean isSchematic(Object o) {
		return o instanceof Matchable matchable && matchable.test(Type.SCHEMATIC);
	}

	public MutableText writeBaseNumber(ComputedAttribute attribute) {
		float value = attribute.asFloat();
		if (attribute.key().equals(AttackSpeed.KEY) && isPartHead(container)) {
			value = 4 + value;
		}

		if (comparativeContainer != null) {
			var comparativeAttribute = new AttributeHelper(comparativeContainer).apply(attribute.key());
			if (!attribute.asFloat().equals(comparativeAttribute.asFloat())) {
				float difference = attribute.asFloat() - comparativeAttribute.asFloat();
				return writeAttributeType(attribute.key())
						.append(indented(1))
						.append(Text.literal(number(value))
								.append(differenceHelper.getDifferenceMarker(difference, attribute.key()))
								.formatted(differenceHelper.getDifferenceFormatting(difference, attribute.key())));
			}
		}
		return writeAttributeType(attribute.key())
				.append(indented(1))
				.append(number(value));
	}

	public MutableText writeMultiplicativeAttribute(Attribute attribute) {
		return writeAttributeType(attribute.type())
				.append(indented(1))
				.append(multiplicativeNumberText(attribute));
	}

	public MutableText writeAdditionAttribute(Attribute attribute) {
		return writeAdditionAttribute(attribute, 1);
	}

		public MutableText writeAdditionAttribute(Attribute attribute, int indent) {
		float value = attribute.leveledValue();
		if (attribute.getAttributeType().equals(AttackSpeed.KEY)) {
			value = 4 + value;
		}
		return writeAttributeType(attribute.type(), indent)
				.append(indented(1))
				.append(additionSign(value))
				.append(number(attribute.leveledValue()));
	}

	public MutableText multiplicativeNumberText(Attribute attribute) {
		float value = attribute.leveledValue();
		return Text.literal(number(value)).append(translatableMultiplier());
	}

	public MutableText percentageNumberText(Attribute attribute) {
		String percentage = number(attribute.leveledValue() * 100 - 100);
		return multiplicativeSign(attribute.leveledValue())
				.append(Text.literal(percentage + "%"));
	}

	public List<MutableText> writeTarget(Attribute attribute) {
		var writer = new PredicateWriterFactory().build(attribute.getPredicate(), configuration);
		if (attribute.getPredicate() == Matchable.DEFAULT_TRUE) {
			return Collections.emptyList();
		} else if (writer.isPresent()) {
			return writer.get().write(attribute.getPredicate());
		} else {
			MutableText against = indented(configuration.baseIndent() + 2)
					.append(Text.translatable("tooltip.forgero.against")
							.formatted(Formatting.GRAY));
			against.append(TagWriter.writeTagList(attribute.targets()));
			return List.of(against);
		}
	}

	private MutableText multiplicativeSign(float value) {
		if (value >= 1) {
			return Text.literal("+");
		} else {
			return Text.literal("");
		}
	}

	private MutableText additionSign(float value) {
		if (value >= 0) {
			return Text.literal("+");
		} else {
			return Text.literal("");
		}
	}

	public Text translatableMultiplier() {
		return Text.translatable("tooltip.forgero.attribute.multiplier");
	}

	public MutableText writeAttributeType(String attribute) {
		return writeAttributeType(attribute, 1);
	}
		public MutableText writeAttributeType(String attribute, int indent) {
		return indented(configuration.baseIndent() + indent)
				.append(writeTranslatableAttributeType(attribute)
						.formatted(neutral()))
				.append(sectionSeparator()
						.formatted(neutral()));
	}

	public MutableText writeTranslatableAttributeType(String type) {
		return Text.translatable(String.format("tooltip.forgero.attribute.%s", type.toLowerCase(Locale.ENGLISH)));
	}

	public Stream<Attribute> attributesOfType(String type) {
		return container.stream().getAttributes().filter(attribute -> attribute.type().equals(type));
	}

	public ComputedAttribute attributeOfType(String type) {
		return helper.apply(type);
	}

	@Override
	public void write(List<Text> tooltip, TooltipContext context) {

	}

	private MutableText sectionSeparator() {
		return Text.translatable("tooltip.forgero.section.section_separator");
	}


}
