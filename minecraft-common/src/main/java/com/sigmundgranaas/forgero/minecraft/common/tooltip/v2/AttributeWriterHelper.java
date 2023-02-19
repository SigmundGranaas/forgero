package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.*;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class AttributeWriterHelper extends BaseWriter {
    public static final List<String> WRITABLE_ATTRIBUTES = List.of(AttackDamage.KEY, MiningSpeed.KEY, Durability.KEY, MiningLevel.KEY, AttackSpeed.KEY, Armor.KEY);
    private final PropertyContainer container;

    public AttributeWriterHelper(PropertyContainer container) {
        this.container = container;

    }


    public MutableText writePercentageAttribute(Attribute attribute) {
        return writeAttributeType(attribute)
                .append(indented(1))
                .append(percentageNumberText(attribute));
    }

    public MutableText writeBaseNumber(Attribute attribute) {
        return writeAttributeType(attribute)
                .append(indented(1))
                .append(number(attribute.leveledValue()));
    }

    public MutableText writeMultiplicativeAttribute(Attribute attribute) {
        return writeAttributeType(attribute)
                .append(indented(1))
                .append(multiplicativeNumberText(attribute));
    }

    public MutableText writeAdditionAttribute(Attribute attribute) {
        return writeAttributeType(attribute)
                .append(indented(1))
                .append(additionSign(attribute.leveledValue()))
                .append(number(attribute.leveledValue()));
    }

    public MutableText multiplicativeNumberText(Attribute attribute) {
        float value = attribute.leveledValue();
        return Text.literal(String.valueOf((int) value == value ? (int) value : value)).append(translatableMultiplier());
    }

    public MutableText percentageNumberText(Attribute attribute) {
        String percentage = number(roundFloat(attribute.leveledValue() * 100) - 100);
        return multiplicativeSign(attribute.leveledValue())
                .append(Text.literal(percentage + "%"));
    }

    private String number(float attribute) {
        if (Math.round(attribute) == attribute) {
            return String.valueOf(Math.round(attribute));
        }
        return String.valueOf(attribute);
    }


    public float roundFloat(float number) {
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
        try {
            return Float.parseFloat(format.format(number));
        } catch (NumberFormatException e) {
            return 1f;
        }
    }

    public Optional<MutableText> writeTarget(Attribute attribute) {
        if (attribute.applyCondition(Target.EMPTY) || attribute.targets().isEmpty()) {
            return Optional.empty();
        } else {
            MutableText against = indented(4).append(Text.translatable("tooltip.forgero.against").formatted(Formatting.GRAY));
            attribute.targets().stream().map(target -> Text.translatable(String.format("tooltip.forgero.tag.%s", target.replace(":", "."))).append(indented(1))).forEach(against::append);
            return Optional.of(against);
        }
    }

    private MutableText multiplicativeSign(float value) {
        if (value >= 1) {
            return Text.literal("+");
        } else {
            return Text.literal("-");
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

    public MutableText writeAttributeType(Attribute attribute) {
        return indented(3).append(writeAttributeType(attribute.type()).formatted(neutral())).append(sectionSeparator().formatted(neutral()));
    }

    public MutableText writeAttributeType(String type) {
        return Text.translatable(String.format("tooltip.forgero.attribute.%s", type.toLowerCase()));
    }

    public Stream<Attribute> attributesOfType(String type) {
        return container.stream().getAttributes().filter(attribute -> attribute.type().equals(type));
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {

    }

    private MutableText sectionSeparator() {
        return Text.translatable("tooltip.forgero.section.section_separator");
    }


}
