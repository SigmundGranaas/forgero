package com.sigmundgranaas.forgero.toolhandler;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.item.StateItem;

import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;

import java.util.Optional;

public class PropertyHelper {
	public static Optional<PropertyContainer> ofPlayerHands(@Nullable PlayerEntity player) {
		if (player != null && player.getMainHandStack().getItem() instanceof StateItem stateItem) {
			return Optional.of(stateItem.dynamicState(player.getMainHandStack()));
		}
		return Optional.empty();
	}
}
