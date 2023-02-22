package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.feature.FeatureWriterSelector;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeatureSectionWriter extends SectionWriter {
    private final PropertyContainer container;

    private final FeatureWriterSelector selector;

    public FeatureSectionWriter(PropertyContainer container) {
        this.container = container;
        this.selector = FeatureWriterSelector.defaultSelector();
    }

    public FeatureSectionWriter(PropertyContainer container, FeatureWriterSelector selector) {
        this.container = container;
        this.selector = selector;
    }

    public static Optional<SectionWriter> of(PropertyContainer container) {
        SectionWriter writer = new FeatureSectionWriter(container);
        if (writer.shouldWrite()) {
            return Optional.of(writer);
        }
        return Optional.empty();
    }

    @Override
    public boolean shouldWrite() {
        return container.stream().features().toList().size() > 0;
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        tooltip.add(createSection("features"));
        tooltip.addAll(entries());
    }

    @Override
    public int getSectionOrder() {
        return 0;
    }

    @Override
    public List<Text> entries() {
        return container.stream().features().map(this::entry).flatMap(List::stream).toList();
    }

    public List<Text> entry(PropertyData data) {
        List<Text> entries = new ArrayList<>();
        selector.writer(data).write(entries, null);
        return entries;
    }
}
