package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.feature.FeatureWriterSelector;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeatureSectionWriter extends SectionWriter {
    private final PropertyContainer container;

    private final FeatureWriterSelector selector;

    public FeatureSectionWriter(PropertyContainer container, TooltipConfiguration configuration) {
        super(configuration);
        this.container = container;
        this.selector = FeatureWriterSelector.defaultSelector();
    }

    public FeatureSectionWriter(PropertyContainer container, FeatureWriterSelector selector, TooltipConfiguration configuration) {
        super(configuration);
        this.container = container;
        this.selector = selector;
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
        if (container.stream().features().toList().size() > 0) {
            return !container.stream().features().allMatch(feature -> configuration.hiddenFeatureTypes().contains(feature.getType()));
        }
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
        return container.stream().features().filter(feature -> !configuration.hiddenFeatureTypes().contains(feature.getType())).map(this::entry).flatMap(List::stream).toList();
    }

    public List<Text> entry(PropertyData data) {
        List<Text> entries = new ArrayList<>();
        selector.writer(data, configuration).write(entries, null);
        return entries;
    }
}
