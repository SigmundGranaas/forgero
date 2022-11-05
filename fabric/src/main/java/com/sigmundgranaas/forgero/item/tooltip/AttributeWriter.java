package com.sigmundgranaas.forgero.item.tooltip;

import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.Target;
import com.sigmundgranaas.forgero.property.attribute.AttributeHelper;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.sigmundgranaas.forgero.property.AttributeType.*;

public class AttributeWriter implements Writer {
    private final List<Text> tooltip;
    private final List<AttributeType> attributes;
    private final AttributeHelper helper;

    public AttributeWriter(AttributeHelper helper) {
        this.helper = helper;
        this.tooltip = new ArrayList<>();
        this.attributes = new ArrayList<>();
    }

    public static AttributeWriter of(AttributeHelper helper) {
        return new AttributeWriter(helper);
    }

    public static void write(State state, List<Text> tooltip, TooltipContext context) {
        var attributes = state.getProperties();

        MutableText slots = Text.literal(" Attributes: ").formatted(Formatting.GRAY);
        tooltip.add(slots);

        addAttribute(attributes, ATTACK_DAMAGE, "Attack Damage", tooltip);
        addAttribute(attributes, AttributeType.ATTACK_SPEED, "Attack Speed", tooltip);
        addAttributeInt(attributes, AttributeType.DURABILITY, "Durability", tooltip);
        addAttribute(attributes, AttributeType.MINING_SPEED, "Mining Speed", tooltip);
        addAttributeInt(attributes, AttributeType.MINING_LEVEL, "Mining Level", tooltip);
        addAttributeInt(attributes, AttributeType.RARITY, "Rarity", tooltip);
    }

    private static void addAttribute(List<Property> attributes, AttributeType type, String title, List<Text> tooltip) {
        float result = Property.stream(attributes).applyAttribute(Target.createEmptyTarget(), type);
        if (result != 0f) {
            MutableText miningLevel = Text.translatable(String.format("  %s: ", title)).formatted(Formatting.GRAY);
            miningLevel.append(Text.translatable(String.format("%s", result)).formatted(Formatting.WHITE));
            tooltip.add(miningLevel);
        }
    }

    private static void addAttributeInt(List<Property> attributes, AttributeType type, String title, List<Text> tooltip) {
        int result = (int) Property.stream(attributes).applyAttribute(Target.createEmptyTarget(), type);
        if (result != 0) {
            MutableText miningLevel = Text.literal("  ").append(Text.translatable(Writer.toTranslationKey(title))).append(" : ").formatted(Formatting.GRAY);
            miningLevel.append(Text.literal(String.format("%s", result)).formatted(Formatting.WHITE));
            tooltip.add(miningLevel);
        }
    }

    public AttributeWriter addAttribute(AttributeType type) {
        this.attributes.add(type);
        return this;
    }

    private void writeAttribute(AttributeType type) {
        switch (type) {
            case ATTACK_DAMAGE -> floatAttribute(ATTACK_DAMAGE, tooltip);
            case ATTACK_SPEED -> floatAttribute(ATTACK_SPEED, tooltip);
            case MINING_SPEED -> floatAttribute(MINING_SPEED, tooltip);
            case RARITY -> intAttribute(RARITY, tooltip);
            case DURABILITY -> intAttribute(DURABILITY, tooltip);
            case MINING_LEVEL -> intAttribute(MINING_LEVEL, tooltip);
        }
    }

    private void intAttribute(AttributeType type, List<Text> tooltip) {
        int result = (int) helper.attribute(type);
        if (result != 0) {
            MutableText miningLevel = Text.literal("  ").append(Text.translatable(Writer.toTranslationKey(type.toString()))).append(": ").formatted(Formatting.GRAY);
            miningLevel.append(Text.literal(String.format("%s", result)).formatted(Formatting.WHITE));
            tooltip.add(miningLevel);
        }
    }

    private void floatAttribute(AttributeType type, List<Text> tooltip) {

        float result = Property.stream(helper.attributes()).applyAttribute(Target.createEmptyTarget(), type);
        if (type == ATTACK_SPEED) {
            result += 4f;
        }
        DecimalFormat df = new DecimalFormat("#.00");
        result = Float.parseFloat(df.format(result));
        if (result != 0f) {
            MutableText miningLevel = Text.literal("  ").append(Text.translatable(Writer.toTranslationKey(type.toString()))).append(": ").formatted(Formatting.GRAY);
            miningLevel.append(Text.literal(String.format("%s", result)).formatted(Formatting.WHITE));
            tooltip.add(miningLevel);
        }
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        if (attributes.size() > 0) {
            MutableText attributes = Text.literal(" ").append(Text.translatable(Writer.toTranslationKey("attributes"))).append(": ").formatted(Formatting.GRAY);
            tooltip.add(attributes);
        }
        attributes.forEach(this::writeAttribute);
        tooltip.addAll(this.tooltip);
    }

}
