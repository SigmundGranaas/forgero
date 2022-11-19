package com.sigmundgranaas.forgero.item.tooltip;

import com.sigmundgranaas.forgero.property.passive.LeveledProperty;
import com.sigmundgranaas.forgero.property.passive.PassiveProperty;
import com.sigmundgranaas.forgero.property.passive.StaticProperty;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class PassiveWriter implements Writer {
    private final List<Text> tooltip;
    private final List<PassiveProperty> passives;

    public PassiveWriter() {
        this.tooltip = new ArrayList<>();
        this.passives = new ArrayList<>();
    }

    private void writePassive(PassiveProperty property) {
        switch (property.getPassiveType()) {
            case STATIC -> staticPassive(property, tooltip);
            case LEVELED -> leveledPassive(property, tooltip);
        }
    }

    private void staticPassive(PassiveProperty passive, List<Text> tooltip) {
        if (passive instanceof StaticProperty staticProperty) {
            MutableText propertyText = Text.literal("  ").append(Text.translatable(Writer.toTranslationKey(staticProperty.getStaticType().toString().toLowerCase()))).formatted(Formatting.WHITE);
            tooltip.add(propertyText);
        }
    }

    private void leveledPassive(PassiveProperty passive, List<Text> tooltip) {
        if (passive instanceof LeveledProperty leveledProperty) {

        }
    }

    public PassiveWriter addPassive(PassiveProperty passive) {
        this.passives.add(passive);
        return this;
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        if (passives.size() > 0) {
            MutableText attributes = Text.literal(" ").append(Text.translatable(Writer.toTranslationKey("passives"))).append(": ").formatted(Formatting.GRAY);
            tooltip.add(attributes);
        }
        passives.forEach(this::writePassive);
        tooltip.addAll(this.tooltip);
    }
}
