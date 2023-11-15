package com.sigmundgranaas.forgero.minecraft.common.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.attribute.BaseAttribute;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttributeModification;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class ModifiableFeatureAttribute implements ComputedAttribute {
	private final Attribute value;

	public ModifiableFeatureAttribute(Attribute value) {
		this.value = value;
	}

	public static ModifiableFeatureAttribute of(JsonElement value, String jsonKey, String key) {
		return of(value.getAsJsonObject(), jsonKey, key, 1);
	}

	public static ModifiableFeatureAttribute of(String key, float value) {
		return new ModifiableFeatureAttribute(BaseAttribute.of(value, key));
	}

	public static ModifiableFeatureAttribute of(JsonObject value, String jsonKey, String key, float defaultValue) {
		if (value.has(jsonKey)) {
			return new ModifiableFeatureAttribute(BaseAttribute.of(value.get(jsonKey).getAsFloat(), key));
		} else {
			return new ModifiableFeatureAttribute(BaseAttribute.of(defaultValue, key));
		}
	}

	@Override
	public String key() {
		return value.type();
	}

	@Override
	public Float asFloat() {
		return value.leveledValue();
	}

	public ModifiableFeatureAttribute with(PropertyContainer container) {
		float result = container.stream().with(value).applyAttribute(key());
		return new ModifiableFeatureAttribute(BaseAttribute.of(result, key()));
	}

	public ModifiableFeatureAttribute with(Entity entity) {
		if (entity instanceof PlayerEntity player) {
			return with(player);
		}
		return this;
	}

	public ModifiableFeatureAttribute with(PlayerEntity entity) {
		return StateService.INSTANCE
				.convert(entity.getMainHandStack())
				.map(this::with)
				.orElse(this);
	}

	@Override
	public ComputedAttribute modify(AttributeModification mod) {
		return this;
	}
}
