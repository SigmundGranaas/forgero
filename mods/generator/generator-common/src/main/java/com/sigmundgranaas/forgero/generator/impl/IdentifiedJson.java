package com.sigmundgranaas.forgero.generator.impl;

import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;

public record IdentifiedJson(Identifier id, JsonObject json, JsonObject template) {
}
