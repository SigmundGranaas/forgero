package com.sigmundgranaas.forgero.data.mapper.impl;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.CompositeAttributeComponent;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeData;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.AttributeDataImpl;
import com.sigmundgranaas.forgero.data.loading.api.data.attribute.ComputationData;
import com.sigmundgranaas.forgero.data.loading.impl.codec.AttributeCodecs;
import com.sigmundgranaas.forgero.data.loading.impl.codec.OperatorMapper;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AttributeCodec implements PropertyCodec<AttributeData> {

	private final OperatorMapper operatorMapper;
	private Codec<List<AttributeData>> codec;

	public AttributeCodec(OperatorMapper operatorMapper) {
		this.operatorMapper = operatorMapper;
	}

	@Override
	public String getPropertyType() {
		return "forgero:attributes";
	}

	@Override
	public Class<AttributeData> getPropertyDataType() {
		return AttributeData.class;
	}

	@Override
	public List<Property> build(List<AttributeData> dataList) {
		if (dataList == null) {
			return Collections.emptyList();
		}
		return dataList.stream().map(this::mapAttribute).collect(Collectors.toList());
	}

	@Override
	@Nullable
	public AttributeData toData(Property property) {
		if (!(property instanceof Attribute attribute)) {
			return null;
		}

		var id = attribute.type();
		if (attribute instanceof CompositeAttributeComponent cac) {
			id = cac.type();
		} else if (attribute instanceof SimpleAttribute sa) {
			id = sa.type();
		}

		var computation = new ComputationData(attribute.value(),
				operatorMapper.toString(attribute.operator()),
				operatorMapper.fromOrder(attribute.group()));

		return new AttributeDataImpl(
				id,
				attribute.type(),
				computation,
				attribute.condition().orElse(null),
				attribute instanceof CompositeAttributeComponent cac ? cac.compositeKey() : null
		);
	}

	@Override
	public Codec<List<AttributeData>> getCodec() {
		if (this.codec == null) {
			throw new IllegalStateException("Codec has not been initialized. Call setCodecs(...) first.");
		}
		return this.codec;
	}

	public void setCodecs(Codec<Condition> conditionCodec) {
		this.codec = Codec.list(AttributeCodecs.create(conditionCodec));
	}

	private Property mapAttribute(AttributeData data) {
		var computation = data.computation();
		var operator = operatorMapper.apply(computation.operator());
		int group = operatorMapper.leveledOrder(computation.order());
		Condition condition = data.condition() != null ? data.condition() : Condition.ALWAYS_TRUE;

		if (data.composite() != null) {
			return new CompositeAttributeComponent(data.type(), computation.value(), operator, group, data.composite());
		} else {
			return new SimpleAttribute(data.type(), computation.value(), operator, group, condition);
		}
	}
}
