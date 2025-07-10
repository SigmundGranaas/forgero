package com.sigmundgranaas.forgero.model.impl.dto;

import org.jetbrains.annotations.Nullable;

public record ModelSelectorDTO(String target_slot, @Nullable String target_tag, String model) {}
