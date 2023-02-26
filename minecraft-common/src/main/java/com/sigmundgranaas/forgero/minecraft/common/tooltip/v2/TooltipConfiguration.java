package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2;

import com.sigmundgranaas.forgero.core.property.attribute.Category;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

import static com.sigmundgranaas.forgero.core.property.attribute.Category.UPGRADE_CATEGORIES;
import static com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.AttributeWriterHelper.WRITABLE_ATTRIBUTES;


@Data()
@Accessors(fluent = true)
@Builder(toBuilder = true)
public class TooltipConfiguration {
    @Builder.Default
    private boolean hideSectionTitle = false;
    @Builder.Default
    private int baseIndent = 0;
    @Builder.Default
    private int sectionOrder = 0;
    @Builder.Default
    private boolean hideZeroValues = true;
    @Builder.Default
    private boolean showExtendedInfo = false;
    @Builder.Default
    private boolean showDetailedInfo = false;
    @Builder.Default
    private List<String> writableAttributes = WRITABLE_ATTRIBUTES;
    @Builder.Default
    private Set<Category> upgradeCategories = UPGRADE_CATEGORIES;

    @Builder.Default
    private Set<String> hiddenFeatureTypes = Set.of("EFFECTIVE_BLOCKS");

    @Builder.Default
    private boolean padded = false;
}
