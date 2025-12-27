package com.sigmundgranaas.forgero.common.tooltip.section.impl;

import com.sigmundgranaas.forgero.common.tooltip.TooltipDescriptor;
import com.sigmundgranaas.forgero.common.tooltip.api.writer.SectionWriter;
import com.sigmundgranaas.forgero.common.tooltip.display.TooltipTextFormatter;
import com.sigmundgranaas.forgero.common.tooltip.section.SectionWriterContext;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation for section writers with common functionality.
 * <p>
 * Provides template methods for building tooltip sections with consistent formatting.
 * Subclasses implement {@link #buildLines()} to generate their specific content.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * public class MySectionWriter extends AbstractSectionWriter {
 *     public MySectionWriter(SectionWriterContext context) {
 *         super(context);
 *     }
 *
 *     @Override
 *     protected void buildLines() {
 *         addLine(Text.literal("My content").formatted(Formatting.GRAY));
 *     }
 * }
 * }</pre>
 */
public abstract class AbstractSectionWriter implements SectionWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSectionWriter.class);

	protected final SectionWriterContext context;
	protected final List<Text> lines = new ArrayList<>();

	protected AbstractSectionWriter(SectionWriterContext context) {
		this.context = context;
	}

	@Override
	public final void append(List<Text> tooltip, TooltipContext tooltipContext) {
		if (!shouldShow()) {
			return;
		}

		// Add section header if configured
		if (context.section().showHeader()) {
			tooltip.add(createHeader());
		}

		// Add all content lines
		tooltip.addAll(lines);

		// Add padding after section if configured
		if (context.renderConfig().addPaddingAfterSections()) {
			tooltip.add(Text.empty());
		}
	}

	@Override
	public boolean shouldShow() {
		lines.clear();
		try {
			buildLines();
		} catch (Exception e) {
			LOGGER.error("Error building tooltip section '{}': {}",
					context.section().id(), e.getMessage(), e);
			return false;
		}
		return !lines.isEmpty();
	}

	/**
	 * Template method for subclasses to build their content lines.
	 */
	protected abstract void buildLines();

	/**
	 * Creates the section header text.
	 */
	protected MutableText createHeader() {
		return TooltipTextFormatter.createSectionHeader(context.section().translationKey());
	}

	/**
	 * Adds a line to the section content.
	 */
	protected void addLine(Text line) {
		lines.add(line);
	}

	/**
	 * Adds a translatable line from a descriptor.
	 */
	protected void addDescriptorLine(TooltipDescriptor descriptor) {
		List<String> argNames = descriptor.getTranslationArgNames();

		if (argNames.isEmpty()) {
			addLine(indented(1)
					.append(Text.translatable(descriptor.template()).formatted(Formatting.GRAY)));
		} else {
			Object[] args = context.valueResolver().resolveArgs(argNames);
			addLine(indented(1)
					.append(Text.translatable(descriptor.template(), args).formatted(Formatting.GRAY)));
		}
	}

	/**
	 * Adds all descriptor lines for this section.
	 */
	protected void addAllDescriptorLines() {
		for (TooltipDescriptor descriptor : context.sectionDescriptors()) {
			addDescriptorLine(descriptor);
		}
	}

	/**
	 * Creates indented text with spaces.
	 */
	protected MutableText indented(int level) {
		return Text.literal(" ".repeat(context.baseIndent() + level));
	}

	/**
	 * Gets the standard content indentation level.
	 */
	protected int getContentIndent() {
		return context.baseIndent() + 1;
	}

	/**
	 * Creates a labeled value line.
	 */
	protected MutableText createLabeledLine(String labelKey, Text valueText, Formatting labelColor) {
		return indented(1)
				.append(Text.translatable(labelKey).formatted(labelColor))
				.append(": ")
				.append(valueText);
	}

	/**
	 * Creates a simple gray text line.
	 */
	protected MutableText createGrayLine(String translationKey) {
		return indented(1)
				.append(Text.translatable(translationKey).formatted(Formatting.GRAY));
	}

	/**
	 * Creates a literal text line (not translated).
	 */
	protected MutableText createLiteralLine(String text, Formatting color) {
		return indented(1)
				.append(Text.literal(text).formatted(color));
	}
}
