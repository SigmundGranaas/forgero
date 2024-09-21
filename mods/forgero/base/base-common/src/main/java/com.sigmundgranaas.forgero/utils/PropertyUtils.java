package com.sigmundgranaas.forgero.utils;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.PropertyStream;
import com.sigmundgranaas.forgero.service.StateService;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class PropertyUtils {
	public static Optional<? extends PropertyContainer> of(Entity entity) {
		if (entity instanceof LivingEntity living) {
			return StateService.INSTANCE.convert(living.getMainHandStack());
		}
		return Optional.empty();
	}

	public static PropertyStream stream(Entity entity) {
		return of(entity)
				.map(PropertyContainer::stream)
				.orElseGet(PropertyStream::empty);
	}

	public static PropertyContainer container(Entity entity) {
		Optional<? extends PropertyContainer> entityContainer = of(entity);
		if (entityContainer.isPresent()) {
			return entityContainer.get();
		}
		return PropertyContainer.EMPTY;
	}
}

