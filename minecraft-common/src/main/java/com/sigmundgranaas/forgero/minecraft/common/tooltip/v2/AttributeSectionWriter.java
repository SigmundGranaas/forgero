package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.AttributeWriterHelper.WRITABLE_ATTRIBUTES;
import static com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.UpgradeAttributeSectionWriter.UPGRADE_CATEGORIES;

public class AttributeSectionWriter extends SectionWriter {

    private static final String sectionTranslationKey = "attributes";
    private final PropertyContainer container;

    private final AttributeWriterHelper helper;

    public AttributeSectionWriter(PropertyContainer container) {
        this.container = container;
        this.helper = new AttributeWriterHelper(container);
    }

    public static Optional<SectionWriter> of(PropertyContainer container) {
        SectionWriter writer = new AttributeSectionWriter(container);
        if (writer.shouldWrite()) {
            return Optional.of(writer);
        }
        return Optional.empty();
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        tooltip.add(createSection(sectionTranslationKey));
        tooltip.addAll(entries());
    }

    @Override
    public boolean shouldWrite() {
        return container.stream()
                .getAttributes()
                .anyMatch(attr -> attr.getOrder() != CalculationOrder.COMPOSITE && !UPGRADE_CATEGORIES.contains(attr.getCategory()));
    }

    @Override
    public int getSectionOrder() {
        return 1;
    }

    @Override
    public List<Text> entries() {
        return WRITABLE_ATTRIBUTES.stream().map(this::entry).flatMap(Optional::stream).toList();
    }

    protected Optional<Text> entry(String attributeType) {
        Optional<Attribute> attributeOpt = helper.attributesOfType(attributeType).findFirst();
        if (attributeOpt.isPresent()) {
            Attribute attribute = attributeOpt.get();
            if (attribute.getOperation() == NumericOperation.MULTIPLICATION) {
                return Optional.of(helper.writeMultiplicativeAttribute(attribute));
            } else {
                return Optional.of(helper.writeBaseNumber(attribute));
            }
        }
        return Optional.empty();
    }
}
