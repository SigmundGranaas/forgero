package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

public class FeatureSectionWriter extends SectionWriter {
	private final PropertyContainer container;


	public FeatureSectionWriter(PropertyContainer container, TooltipConfiguration configuration) {
		super(configuration);
		this.container = container;
	}


	public static Optional<SectionWriter> of(PropertyContainer container) {
		return of(container, TooltipConfiguration.builder().build());
	}

	public static Optional<SectionWriter> of(PropertyContainer container, TooltipConfiguration configuration) {
		SectionWriter writer = new FeatureSectionWriter(container, configuration);
		if (writer.shouldWrite()) {
			return Optional.of(writer);
		}
		return Optional.empty();
	}

	@Override
	public boolean shouldWrite() {
		return false;
	}

	@Override
	public void write(List<Text> tooltip, TooltipContext context) {
		if (!configuration.hideSectionTitle()) {
			tooltip.add(createSection("features"));
		}
		tooltip.addAll(entries());

		super.write(tooltip, context);
	}

	@Override
	public int getSectionOrder() {
		return 0;
	}

	@Override
	public List<Text> entries() {
		return Collections.emptyList();

	}

}
