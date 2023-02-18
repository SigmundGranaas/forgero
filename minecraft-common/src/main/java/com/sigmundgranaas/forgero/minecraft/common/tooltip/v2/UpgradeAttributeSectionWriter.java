package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.AttributeWriterHelper.WRITABLE_ATTRIBUTES;

public class UpgradeAttributeSectionWriter extends SectionWriter {
    private static final String sectionTranslationKey = "upgrade_attributes";
    private static final Set<Category> upgradeCategories = Set.of(Category.UTILITY, Category.DEFENSIVE, Category.OFFENSIVE, Category.ALL);
    private final AttributeWriterHelper helper;
    private final PropertyContainer container;

    public UpgradeAttributeSectionWriter(PropertyContainer container) {
        this.helper = new AttributeWriterHelper(container);
        this.container = container;
    }

    public static Optional<SectionWriter> of(PropertyContainer container) {
        SectionWriter writer = new UpgradeAttributeSectionWriter(container);
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
                .map(Attribute::getCategory)
                .anyMatch(upgradeCategories::contains);
    }

    @Override
    public int getSectionOrder() {
        return 1;
    }

    @Override
    public List<Text> entries() {
        return upgradeCategories.stream().map(this::category).flatMap(List::stream).toList();
    }

    protected List<Text> category(Category category) {
        List<Text> entries = WRITABLE_ATTRIBUTES.stream().map(attribute -> entry(attribute, category)).flatMap(Optional::stream).toList();
        if (entries.size() > 0) {
            Text section = indented(1).append(createSection(category.toString().toLowerCase()));
            return ImmutableList.<Text>builder().add(section).addAll(entries).build();
        }
        return Collections.emptyList();
    }


    protected Optional<Text> entry(String attributeType, Category category) {
        Optional<Attribute> attributeOpt = helper.attributesOfType(attributeType).filter(attribute -> category.equals(attribute.getCategory())).findFirst();
        if (attributeOpt.isPresent()) {
            Attribute attribute = attributeOpt.get();
            if (attribute.getOperation() == NumericOperation.MULTIPLICATION) {
                return Optional.of(helper.writePercentageAttribute(attribute));
            } else {
                return Optional.of(helper.writeAdditionAttribute(attribute));
            }
        }
        return Optional.empty();
    }

}
