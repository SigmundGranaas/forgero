package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.model.ModelBuilder;

/**
 * Implementation of ModelBuilder.DisplaySettings.
 */
public record DisplaySettingsImpl(float[] rotation, float[] translation, float[] scale) implements ModelBuilder.DisplaySettings {
}
