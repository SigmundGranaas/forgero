package com.sigmundgranaas.forgero.item.adapter;

import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.Target;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class AttributeWriter {
    public static void write(State state, List<Text> tooltip, TooltipContext context) {
        var attributes = state.getProperties();

        MutableText slots = Text.literal(" Attributes: ").formatted(Formatting.GRAY);
        tooltip.add(slots);

        addAttribute(attributes, AttributeType.ATTACK_DAMAGE, "Attack Damage", tooltip);
        addAttribute(attributes, AttributeType.ATTACK_SPEED, "Attack Speed", tooltip);
        addAttributeInt(attributes, AttributeType.DURABILITY, "Durability", tooltip);
        addAttribute(attributes, AttributeType.MINING_SPEED, "Mining Speed", tooltip);
        addAttributeInt(attributes, AttributeType.MINING_LEVEL, "Mining Level", tooltip);
        addAttributeInt(attributes, AttributeType.RARITY, "Rarity", tooltip);
    }

    private static void addAttribute(List<Property> attributes, AttributeType type, String title, List<Text> tooltip) {
        float result = Property.stream(attributes).applyAttribute(Target.createEmptyTarget(), type);
        if (result != 0f) {
            MutableText miningLevel = Text.literal(String.format("  %s : ", title)).formatted(Formatting.GRAY);
            miningLevel.append(Text.literal(String.format("%s", result)).formatted(Formatting.WHITE));
            tooltip.add(miningLevel);
        }
    }

    private static void addAttributeInt(List<Property> attributes, AttributeType type, String title, List<Text> tooltip) {
        int result = (int) Property.stream(attributes).applyAttribute(Target.createEmptyTarget(), type);
        if (result != 0) {
            MutableText miningLevel = Text.literal(String.format("  %s : ", title)).formatted(Formatting.GRAY);
            miningLevel.append(Text.literal(String.format("%s", result)).formatted(Formatting.WHITE));
            tooltip.add(miningLevel);
        }
    }
}
