package com.sigmundgranaas.forgero.common.tooltip.section.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tooltip.section.SectionWriterContext;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Writes the slots section.
 * <p>
 * Displays available upgrade slots and their status (filled/empty).
 * <p>
 * Example output:
 * <pre>
 * Slots:
 *   Gem: Diamond Gem
 *   Binding: Empty
 * </pre>
 */
public class SlotsSectionWriter extends AbstractSectionWriter {

	public SlotsSectionWriter(SectionWriterContext context) {
		super(context);
	}

	@Override
	protected void buildLines() {
		Component component = context.component();

		if (!(component instanceof CustomizableComponent customizable)) {
			return;
		}

		List<ComponentUpgradeSlot> slots = customizable.getUpgradeSlots();
		if (slots.isEmpty()) {
			return;
		}

		for (ComponentUpgradeSlot slot : slots) {
			addLine(buildSlotLine(slot));
		}
	}

	/**
	 * Builds a single line for a slot.
	 *
	 * @param slot The slot to display
	 * @return The formatted text line
	 */
	private MutableText buildSlotLine(ComponentUpgradeSlot slot) {
		MutableText line = indented(1);

		// Slot type label (e.g., "Gem", "Binding")
		String slotTypeKey = toSlotTypeTranslationKey(slot.slotType());
		line.append(Text.translatable(slotTypeKey).formatted(Formatting.GRAY));
		line.append(Text.literal(": ").formatted(Formatting.DARK_GRAY));

		if (slot.isFilled()) {
			// Show content name
			Component content = slot.getContent().orElseThrow();
			MutableText contentName = componentToTranslatableText(content);
			line.append(contentName.formatted(Formatting.WHITE));
		} else {
			// Show empty indicator
			line.append(Text.translatable("tooltip.forgero.slot.empty").formatted(Formatting.DARK_GRAY));

		slot.tags().stream()
				.filter(tag -> tag.path().startsWith("contexts/"))
				.findFirst()
				.ifPresent(scope -> {
					String scopeKey = toScopeTranslationKey(scope);
					line.append(Text.literal(" (").formatted(Formatting.DARK_GRAY));
					line.append(Text.translatable(scopeKey).formatted(Formatting.DARK_GRAY));
					line.append(Text.literal(")").formatted(Formatting.DARK_GRAY));
				});
		}

		return line;
	}

	/**
	 * Converts a slot type identifier to a translation key.
	 * <p>
	 * Example: "forgero:gem" -> "tooltip.forgero.slot_type.gem"
	 */
	private String toSlotTypeTranslationKey(OpenIdentifier slotType) {
		return String.format("tooltip.forgero.slot_type.%s", slotType.path());
	}

	/**
	 * Converts a scope identifier to a translation key.
	 * <p>
	 * Example: "forgero:offensive" -> "tooltip.forgero.scope.offensive"
	 */
	private String toScopeTranslationKey(OpenIdentifier scopeId) {
		return String.format("tooltip.forgero.scope.%s", scopeId.path());
	}

	/**
	 * Converts a component to translatable text.
	 * <p>
	 * Splits hyphenated names and translates each part.
	 * Example: "diamond-gem" -> "Diamond Gem"
	 */
	private MutableText componentToTranslatableText(Component component) {
		MutableText text = Text.literal("");
		String name = component.id().name();
		String[] parts = name.split("-");

		for (int i = 0; i < parts.length; i++) {
			if (i > 0) {
				text.append(Text.translatable("util.forgero.name_separator"));
			}
			text.append(Text.translatable(toItemTranslationKey(parts[i])));
		}

		return text;
	}

	/**
	 * Converts a name part to an item translation key.
	 * <p>
	 * Example: "diamond" -> "item.forgero.diamond"
	 */
	private String toItemTranslationKey(String namePart) {
		return String.format("item.forgero.%s", namePart);
	}
}
