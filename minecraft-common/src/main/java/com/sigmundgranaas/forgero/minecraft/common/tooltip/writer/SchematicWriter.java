package com.sigmundgranaas.forgero.minecraft.common.tooltip.writer;

import com.sigmundgranaas.forgero.core.property.AttributeType;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeHelper;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.AttributeWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.StateWriter;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class SchematicWriter extends StateWriter {
    public SchematicWriter(State state) {
        super(state);
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        var materials = state.getCustomValue("ingredient_count");
        if (materials.isPresent()) {
            MutableText materialText = Text.translatable("item.forgero.material_count", materials.get().presentableValue()).formatted(Formatting.GRAY);
            tooltip.add(materialText);

        }
        super.write(tooltip, context);
        var writer = AttributeWriter.of(AttributeHelper.of(state));
        for (AttributeType type : AttributeType.values()) {
            writer.addAttribute(type);
        }

        writer.write(tooltip, context);
    }
}
