package com.sigmundgranaas.forgero.property.active;

import com.sigmundgranaas.forgero.property.ActivePropertyType;
import com.sigmundgranaas.forgero.resource.data.PropertyPojo;

import java.util.function.Function;
import java.util.function.Predicate;

public record VeinBreaking(int depth, String tag, String description) implements ActiveProperty {
    public static Predicate<PropertyPojo.Active> predicate = (active) -> active.type == ActivePropertyType.VEIN_MINING_PATTERN;
    public static Function<PropertyPojo.Active, ActiveProperty> factory = (active) -> new VeinBreaking(active.depth, active.tag, active.description);

    @Override
    public ActivePropertyType getActiveType() {
        return ActivePropertyType.VEIN_MINING_PATTERN;
    }
}
