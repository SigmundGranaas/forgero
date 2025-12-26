package com.sigmundgranaas.forgero.model.api.armor;

import com.sigmundgranaas.forgero.common.identifier.api.Identifiable;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.Contextual;
import com.sigmundgranaas.forgero.model.api.Layered;
import com.sigmundgranaas.forgero.model.api.ModelLayer;
import com.sigmundgranaas.forgero.model.api.ModelSlot;
import com.sigmundgranaas.forgero.model.api.Slotted;

import java.util.List;
import java.util.Optional;

/**
 * Defines how a Forgero component should be rendered as a piece of worn armor.
 * It specifies the 3D geometry to use and describes how to compose the texture for it.
 *
 * This model implements modular capability interfaces, allowing generic code to work
 * with it through composition rather than inheritance.
 */
public record ArmorModel(
        OpenIdentifier identifier,
        OpenIdentifier model,
        List<ModelLayer> layers,
        List<ModelSlot> slots,
        Optional<OpenIdentifier> target,
        Optional<String> context
) implements Identifiable, Layered, Slotted, Contextual {

	@Override
	public OpenIdentifier id() {
		return identifier;
	}
}
