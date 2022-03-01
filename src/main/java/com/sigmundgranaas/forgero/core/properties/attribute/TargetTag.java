package com.sigmundgranaas.forgero.core.properties.attribute;

import java.util.List;

public interface TargetTag {
    boolean isApplicable(List<String> tag);
}
