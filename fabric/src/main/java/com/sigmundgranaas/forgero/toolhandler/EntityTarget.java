package com.sigmundgranaas.forgero.toolhandler;

import com.sigmundgranaas.forgerocore.property.Target;
import com.sigmundgranaas.forgerocore.property.TargetTypes;
import net.minecraft.entity.EntityType;

import java.util.Collections;
import java.util.Set;

public record EntityTarget(EntityType<?> targetType) implements Target {
    @Override
    public Set<TargetTypes> getTypes() {
        return Set.of(TargetTypes.ENTITY);
    }

    @Override
    public Set<String> getTags() {
        return Collections.emptySet();
    }

    @Override
    public boolean isApplicable(Set<String> tag, TargetTypes type) {
        if (type == TargetTypes.ENTITY) {
            for (String stringTag : tag) {
                var entityType = EntityType.get(stringTag);
                if (entityType.isPresent() && entityType.get() == targetType) {
                    return true;
                }
            }
        }
        return false;
    }
}
