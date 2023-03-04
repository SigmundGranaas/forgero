package com.sigmundgranaas.forgero.minecraft.common.tooltip.writer;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.state.SchematicBased;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.StateWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.DefaultWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section.SlotSectionWriter;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Supplier;

public class SchematicWriter extends StateWriter {
	public SchematicWriter(State state) {
		super(state);
	}

	@Override
	public void write(List<Text> tooltip, TooltipContext context) {
		var materials = state.getCustomValue("ingredient_count");
		if (materials.isPresent()) {
			MutableText materialText = Text.translatable("tooltip.forgero.material_count", "").formatted(Formatting.GRAY).append(Text.literal(materials.get().presentableValue()).formatted(Formatting.WHITE));
			tooltip.add(materialText);
		}

		ForgeroStateRegistry.STATES.all()
				.stream()
				.map(Supplier::get)
				.filter(parts -> parts instanceof SchematicBased based && based.schematic().identifier().equals(state.identifier()))
				.findAny()
				.flatMap(SlotSectionWriter::of)
				.ifPresent(writer -> writer.write(tooltip, context));

		new DefaultWriter(state).write(tooltip, context);
	}
}
