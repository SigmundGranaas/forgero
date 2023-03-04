package com.sigmundgranaas.forgero.minecraft.common.tooltip;

import com.sigmundgranaas.forgero.core.property.attribute.AttributeHelper;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.state.composite.Constructed;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;

import java.util.List;

public class CompositeWriter implements Writer {
	private final Composite composite;

	public CompositeWriter(Composite composite) {
		this.composite = composite;
	}

	public static Rarity getRarityFromInt(int rarity) {
		if (rarity >= 100) {
			return Rarity.EPIC;
		} else if (rarity >= 80) {
			return Rarity.RARE;
		} else if (rarity >= 30) {
			return Rarity.UNCOMMON;
		}
		return Rarity.COMMON;
	}

	public static void write(Composite composite, List<Text> tooltip, TooltipContext context, int indent) {
		StringBuilder stringIndent = new StringBuilder(" ");
		stringIndent.append(" ".repeat(Math.max(0, indent)));

		if (composite.slots().size() > 0) {
			MutableText slots = Text.literal(stringIndent.toString()).append(Text.translatable(Writer.toTranslationKey("slots")).formatted(Formatting.GRAY));
			tooltip.add(slots);
			composite.slots().forEach(slot -> {
				MutableText mutableText = Text.literal(stringIndent.toString()).append(Text.translatable(Writer.toTranslationKey(slot.identifier().toLowerCase())).formatted(Formatting.GRAY));
				if (slot.filled()) {
					Rarity rarity = getRarityFromInt(AttributeHelper.of(slot.get().get()).rarity());
					mutableText.append(Text.literal(": ")).append(Writer.nameToTranslatableText(slot.get().get())).formatted(rarity.formatting);
				} else {
					mutableText.append(": -").formatted(Formatting.GRAY);
				}
				tooltip.add(mutableText);
				if (context.isAdvanced() && slot.filled() && slot.get().get() instanceof Construct constructSlot) {
					write(constructSlot, tooltip, context, indent + 2);
				}
			});
		}

		if (composite instanceof Constructed construct && construct.parts().size() > 0) {
			MutableText ingredients = Text.literal(stringIndent.toString()).append(Text.translatable(Writer.toTranslationKey("ingredients"))).append(":").formatted(Formatting.GRAY);
			tooltip.add(ingredients);
			construct.parts().forEach(ingredient -> {
				Rarity rarity = getRarityFromInt(AttributeHelper.of(ingredient).rarity());
				MutableText mutableText = Text.literal(stringIndent.toString()).append(Writer.nameToTranslatableText(ingredient)).formatted(rarity.formatting);
				tooltip.add(mutableText);
				if (context.isAdvanced() && ingredient instanceof Composite constructSlot) {
					write(constructSlot, tooltip, context, indent + 2);
				}
			});
		}
	}

	private void writeCompositeWithIndent(List<Text> tooltip, TooltipContext context, int indent) {
		StringBuilder stringIndent = new StringBuilder(" ");
		stringIndent.append(" ".repeat(Math.max(0, indent)));

		if (composite.slots().size() > 0) {
			MutableText slots = Text.literal(stringIndent.toString()).append(Text.translatable(Writer.toTranslationKey("slots")).append(Text.literal(":")).formatted(Formatting.GRAY));
			tooltip.add(slots);
			composite.slots().forEach(slot -> {
				MutableText mutableText = Text.literal(stringIndent + " ").append(Text.translatable(Writer.toTranslationKey(slot.identifier().toLowerCase())).append(Text.literal(": ")).formatted(Formatting.GRAY));
				if (slot.filled()) {
					Rarity rarity = getRarityFromInt(AttributeHelper.of(slot.get().get()).rarity());
					mutableText.append(Writer.nameToTranslatableText(slot.get().get())).formatted(rarity.formatting);
				} else {
					if (slot.identifier().equals(slot.typeName().toLowerCase())) {
						mutableText.append(Text.literal("-")).formatted(Formatting.GRAY);
					} else {
						mutableText.append(Text.translatable(Writer.toTranslationKey(slot.typeName().toLowerCase()))).formatted(Formatting.GRAY);
					}

				}
				tooltip.add(mutableText);
				if (context.isAdvanced() && slot.filled() && slot.get().get() instanceof Construct constructSlot) {
					write(constructSlot, tooltip, context, indent + 2);
				}
			});
		}

		if (composite instanceof Constructed construct && construct.parts().size() > 0) {
			MutableText ingredients = Text.literal(stringIndent.toString()).append(Text.translatable(Writer.toTranslationKey("ingredients"))).append(":").formatted(Formatting.GRAY);
			tooltip.add(ingredients);
			construct.parts().forEach(ingredient -> {
				Rarity rarity = getRarityFromInt(AttributeHelper.of(ingredient).rarity());
				MutableText mutableText = Text.literal(stringIndent + " ").append(Writer.nameToTranslatableText(ingredient)).formatted(rarity.formatting);
				tooltip.add(mutableText);
				if (context.isAdvanced() && ingredient instanceof Composite constructSlot) {
					write(constructSlot, tooltip, context, indent + 2);
				}
			});
		}
	}


	@Override
	public void write(List<Text> tooltip, TooltipContext context) {
		writeCompositeWithIndent(tooltip, context, 0);
	}
}
