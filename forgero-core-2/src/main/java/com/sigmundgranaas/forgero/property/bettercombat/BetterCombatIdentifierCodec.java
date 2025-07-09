package com.sigmundgranaas.forgero.property.bettercombat;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.data.loading.impl.codec.ConditionMapper;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BetterCombatIdentifierCodec implements PropertyCodec<BetterCombatIdentifierData> {
	private final ConditionMapper conditionMapper;

	public BetterCombatIdentifierCodec(ConditionMapper conditionMapper) {
		this.conditionMapper = conditionMapper;
	}

	@Override
	public String getPropertyType() {
		return DefaultBetterCombatKeys.BETTER_COMBAT_IDENTIFIER.toString();
	}

	@Override
	public Class<BetterCombatIdentifierData> getPropertyDataType() {
		return BetterCombatIdentifierData.class;
	}

	@Override
	public List<Property> build(List<BetterCombatIdentifierData> dataList) {
		return dataList.stream()
				.map(data -> (Property) new BetterCombatIdentifierProperty(data.value(), conditionMapper.apply(data.condition())))
				.toList();
	}

	@Override
	@Nullable
	public BetterCombatIdentifierData toData(Property property) {
		if (property instanceof BetterCombatIdentifierProperty bcip) {
			return new BetterCombatIdentifierData(bcip.identifier(), conditionMapper.toData(bcip.condition()));
		}
		return null;
	}

	@Override
	public Codec<List<BetterCombatIdentifierData>> getCodec() {
		return BetterCombatIdentifierData.CODEC.listOf();
	}
}
