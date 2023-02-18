package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.*;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.text.NumberFormat;
import java.util.List;
import java.util.stream.Stream;

public class AttributeWriterHelper extends BaseWriter {
    public static final List<String> WRITABLE_ATTRIBUTES = List.of(AttackDamage.KEY, MiningSpeed.KEY, Durability.KEY, MiningLevel.KEY, AttackSpeed.KEY, Armor.KEY);
    private final PropertyContainer container;

    public AttributeWriterHelper(PropertyContainer container) {
        this.container = container;

    }

    public static float roundFloat(float number) {
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(2);
        try {
            return Float.parseFloat(format.format(number));
        } catch (NumberFormatException e) {
            return 1f;
        }
    }

    public Text writePercentageAttribute(Attribute attribute) {
        return writeAttributeType(attribute)
                .append(indented(1))
                .append(percentageNumberText(attribute));
    }

    public Text writeBaseNumber(Attribute attribute) {
        return writeAttributeType(attribute)
                .append(indented(1))
                .append(number(attribute));
    }

    private String number(Attribute attribute) {
        float value = attribute.getValue();
        if (Math.round(value) == value) {
            return String.valueOf(Math.round(value));
        }
        return String.valueOf(value);
    }

    public Text writeMultiplicativeAttribute(Attribute attribute) {
        return writeAttributeType(attribute)
                .append(indented(1))
                .append(multiplicativeNumberText(attribute));
    }

    public Text writeAdditionAttribute(Attribute attribute) {
        return writeAttributeType(attribute)
                .append(indented(1))
                .append(additionSign(attribute.getValue()))
                .append(number(attribute));
    }

    public Text multiplicativeNumberText(Attribute attribute) {
        float value = attribute.getValue();
        return Text.literal(String.valueOf((int) value == value ? (int) value : value)).append(translatableMultiplier());
    }

    public Text percentageNumberText(Attribute attribute) {
        float percentage = roundFloat((attribute.getValue() * 100) - 100);
        return multiplicativeSign(attribute.getValue())
                .append(Text.literal(String.format("%s", (int) percentage == percentage ? (int) percentage : percentage + "%")));
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
        return indented(3).append(writeAttributeType(attribute.type())).append(sectionSeparator());
    }

    public Text writeAttributeType(String type) {
        return Text.translatable(String.format("tooltip.forgero.attribute.%s", type.toLowerCase()));
    }

    public Stream<Attribute> attributesOfType(String type) {
        return container.stream().getAttributes().filter(attribute -> attribute.type().equals(type));
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {

    }

    private Text sectionSeparator() {
        return Text.translatable("tooltip.forgero.section.section_separator");
    }


}
