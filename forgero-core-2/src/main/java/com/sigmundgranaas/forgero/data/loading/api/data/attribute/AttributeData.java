package com.sigmundgranaas.forgero.data.loading.api.data.attribute;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import org.jetbrains.annotations.Nullable;

public interface AttributeData extends PropertyData {
	OpenIdentifier id();
	OpenIdentifier type();
	ComputationData computation();
	@Nullable
	Condition condition();
	@Nullable
	OpenIdentifier composite();
}
