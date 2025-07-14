package com.sigmundgranaas.forgero.property.bettercombat;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.Property;
import com.sigmundgranaas.forgero.core.property.condition.Condition;
import com.sigmundgranaas.forgero.data.loading.api.data.PropertyData;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class BetterCombatIdentifierCodec implements PropertyCodec<BetterCombatIdentifierData> {
	private Codec<List<BetterCombatIdentifierData>> codec;

	public BetterCombatIdentifierCodec() {
	}

	public void initialize(Codec<Condition> conditionCodec) {
		this.codec = BetterCombatIdentifierData.createCodec(conditionCodec).listOf();
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
				.map(data -> (Property) new BetterCombatIdentifierProperty(data.value(), data.condition()))
				.collect(Collectors.toList());
	}

	@Override
	@Nullable
	public BetterCombatIdentifierData toData(Property property) {
		if (property instanceof BetterCombatIdentifierProperty bcip) {
			return new BetterCombatIdentifierData(bcip.identifier(), bcip.condition());
		}
		return null;
	}

	@Override
	public Codec<List<BetterCombatIdentifierData>> getCodec() {
		if (codec == null) {
			throw new IllegalStateException("BetterCombatIdentifierCodec has not been initialized. Call initialize() first.");
		}
		return codec;
	}
}
