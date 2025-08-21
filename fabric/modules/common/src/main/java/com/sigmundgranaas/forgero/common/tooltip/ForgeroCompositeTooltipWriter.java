package com.sigmundgranaas.forgero.common.tooltip;

import com.sigmundgranaas.forgero.common.tooltip.section.AttributeSectionWriter;
import com.sigmundgranaas.forgero.common.tooltip.section.CompositeSectionWriter;
import com.sigmundgranaas.forgero.common.tooltip.section.DescriptionSectionWriter; // Import the new writer
import com.sigmundgranaas.forgero.common.tooltip.section.TooltipSectionWriter;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;

/**
 * An implementation of TooltipWriter that composes multiple sections.
 * It iterates through a list of TooltipSectionWriters, allowing each to
 * contribute to the final tooltip if they have relevant information to display.
 */
public class ForgeroCompositeTooltipWriter implements TooltipWriter {
	private final List<TooltipSectionWriter> sections;

	public ForgeroCompositeTooltipWriter(Component component, Resolver resolver) {
		// Define the order and composition of tooltip sections here.
		this.sections = List.of(
				new DescriptionSectionWriter(component, resolver),
				new AttributeSectionWriter(component, resolver.resolve(component, new AttributeEngine())),
				new CompositeSectionWriter(component, component.properties(Attribute.KEY))
		);
	}

	@Override
	public void append(List<Text> tooltip, TooltipContext context) {
		for (TooltipSectionWriter section : sections) {
			if (section.shouldShow()) {
				section.append(tooltip, context);
			}
		}
	}
}
