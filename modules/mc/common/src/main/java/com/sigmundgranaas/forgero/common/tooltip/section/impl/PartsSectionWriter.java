package com.sigmundgranaas.forgero.common.tooltip.section.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tooltip.section.SectionWriterContext;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;

/**
 * Writes the parts/composition section.
 * <p>
 * Displays the structural components that make up this item (blade, handle, binding, etc.).
 * Only shown for {@link StructuredComponent} instances.
 * <p>
 * Example output:
 * <pre>
 * Parts:
 *   Blade: Iron Sword Blade
 *   Handle: Oak Handle
 *   Binding: Leather Binding
 * </pre>
 */
public class PartsSectionWriter extends AbstractSectionWriter {

	public PartsSectionWriter(SectionWriterContext context) {
		super(context);
	}

	@Override
	protected void buildLines() {
		Component component = context.component();

		if (!(component instanceof StructuredComponent structured)) {
			return;
		}

		Collection<ComponentPart> parts = structured.structure().allParts();
		if (parts.isEmpty()) {
			return;
		}

		for (ComponentPart part : parts) {
			addLine(buildPartLine(part));
		}
	}

	/**
	 * Builds a single line for a part.
	 *
	 * @param part The part to display
	 * @return The formatted text line
	 */
	private MutableText buildPartLine(ComponentPart part) {
		MutableText line = indented(1);

		// Part type label (e.g., "Blade", "Handle")
		String partTypeKey = toPartTypeTranslationKey(part.partType());
		line.append(Text.translatable(partTypeKey).formatted(Formatting.GRAY));
		line.append(Text.literal(": ").formatted(Formatting.DARK_GRAY));

		// Part content name
		Component content = part.getContent();
		MutableText contentName = componentToTranslatableText(content);
		line.append(contentName.formatted(Formatting.WHITE));

		return line;
	}

	/**
	 * Converts a part type identifier to a translation key.
	 * <p>
	 * Example: "forgero:parts/types/blade" -> "tooltip.forgero.part_type.blade"
	 */
	private String toPartTypeTranslationKey(OpenIdentifier partType) {
		return String.format("tooltip.forgero.part_type.%s", partType.name());
	}

	/**
	 * Converts a component to translatable text.
	 * <p>
	 * Splits hyphenated names and translates each part.
	 * Example: "iron-sword_blade" -> "Iron Sword Blade"
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
	 * Example: "iron" -> "item.forgero.iron"
	 */
	private String toItemTranslationKey(String namePart) {
		return String.format("item.forgero.%s", namePart);
	}
}
