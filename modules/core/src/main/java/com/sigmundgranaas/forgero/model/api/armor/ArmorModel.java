package com.sigmundgranaas.forgero.model.api.armor;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.ModelLayer;
import com.sigmundgranaas.forgero.model.api.ModelSlot;

import java.util.List;
import java.util.Optional;

/**
 * Defines how a Forgero component should be rendered as a piece of worn armor.
 * It specifies the 3D geometry to use and describes how to compose the texture for it.
 */
public record ArmorModel(
        OpenIdentifier identifier,
        OpenIdentifier model,
        List<ModelLayer> textures,
        List<ModelSlot> slots,
        Optional<OpenIdentifier> target, // For contextual models.
        Optional<String> context // For contextual models.
) {}
