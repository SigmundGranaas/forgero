package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section;

import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.AttributeWriterHelper;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public class AttributeSectionWriter extends SectionWriter {

    private static final String sectionTranslationKey = "attributes";
    private final PropertyContainer container;

    private final AttributeWriterHelper helper;

    public AttributeSectionWriter(PropertyContainer container, TooltipConfiguration configuration) {
        super(configuration);
        this.container = container;
        this.helper = new AttributeWriterHelper(container, configuration);
    }

    public static Optional<SectionWriter> of(PropertyContainer container) {
        return of(container, TooltipConfiguration.builder().build());
    }

    public static Optional<SectionWriter> of(PropertyContainer container, TooltipConfiguration configuration) {
        SectionWriter writer = new AttributeSectionWriter(container, configuration);
        if (writer.shouldWrite()) {
            return Optional.of(writer);
        }
        return Optional.empty();
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        tooltip.add(createSection(sectionTranslationKey));
        tooltip.addAll(entries());
        super.write(tooltip, context);
    }

    @Override
    public boolean shouldWrite() {
        return entries().size() > 0 && container.stream().getAttributes().noneMatch(attribute -> attribute.getOrder() == CalculationOrder.COMPOSITE);
    }

    @Override
    public int getSectionOrder() {
        return 1;
    }

    @Override
    public List<Text> entries() {
        return configuration.writableAttributes().stream().map(this::entry).flatMap(Optional::stream).toList();
    }

    protected Optional<Text> entry(String attributeType) {
        var attribute = helper.attributeOfType(attributeType);
        if (configuration.hideZeroValues() && !configuration.showDetailedInfo() && attribute.asFloat() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(helper.writeBaseNumber(attribute));
        }
    }
}
