package com.sigmundgranaas.forgero.data.mapper.api;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PropertyCodec<T extends PropertyData> {
	String getPropertyType();
	Class<T> getPropertyDataType();
	List<Property> build(List<T> dataList);
	@Nullable
	T toData(Property property);
	Codec<List<T>> getCodec();
}
