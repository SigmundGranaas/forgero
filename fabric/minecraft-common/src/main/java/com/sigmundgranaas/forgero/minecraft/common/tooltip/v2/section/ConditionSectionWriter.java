package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.condition.Conditional;
import com.sigmundgranaas.forgero.core.condition.NamedCondition;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;
import com.sigmundgranaas.forgero.minecraft.common.utils.Text;

import net.minecraft.client.item.TooltipContext;

public class ConditionSectionWriter extends SectionWriter {
	private final Conditional<?> container;

	public ConditionSectionWriter(Conditional<?> container, TooltipConfiguration configuration) {
		super(configuration);
		this.container = container;
	}

	public static Optional<SectionWriter> of(PropertyContainer container) {
		return of(container, TooltipConfiguration.builder().build());
	}

	public static Optional<SectionWriter> of(PropertyContainer container, TooltipConfiguration configuration) {
		if (container instanceof Conditional<?> conditional) {
			SectionWriter writer = new ConditionSectionWriter(conditional, configuration);
			if (writer.shouldWrite()) {
				return Optional.of(writer);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean shouldWrite() {
		return container.namedConditions().size() > 0;
	}

	@Override
	public void write(List<net.minecraft.text.Text> tooltip, TooltipContext context) {
		if (!configuration.hideSectionTitle()) {
			tooltip.add(createSection("conditions"));
		}
		tooltip.addAll(entries());

		super.write(tooltip, context);
	}

	@Override
	public int getSectionOrder() {
		return 0;
	}

	@Override
	public List<net.minecraft.text.Text> entries() {
		return container.namedConditions().stream().map(this::entry).toList();
	}

	public net.minecraft.text.Text entry(NamedCondition data) {
		return indented(entryIndent()).append(Text.translatable(String.format("condition.forgero.%s", data.name())));
	}
}
