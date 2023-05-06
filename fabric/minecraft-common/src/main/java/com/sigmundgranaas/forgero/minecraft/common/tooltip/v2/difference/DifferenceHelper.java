package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.difference;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;

import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class DifferenceHelper implements DifferenceWriter {
	private final Map<String, DifferenceWriter> helperMap;

	public DifferenceHelper() {
		this.helperMap = new HashMap<>();
		helperMap.put(Durability.KEY, new DurabilityWriter());
		helperMap.put(Weight.KEY, new WeightWriter());
	}

	public Formatting getDifferenceFormatting(float difference, String type) {
		return Optional.ofNullable(helperMap.get(type))
				.map(helper -> helper.getDifferenceFormatting(difference))
				.orElseGet(() -> getDifferenceFormatting(difference));
	}

	public MutableText getDifferenceMarker(float difference, String type) {
		return Optional.ofNullable(helperMap.get(type))
				.map(helper -> helper.getDifferenceMarker(difference))
				.orElseGet(() -> getDifferenceMarker(difference));
	}

}
