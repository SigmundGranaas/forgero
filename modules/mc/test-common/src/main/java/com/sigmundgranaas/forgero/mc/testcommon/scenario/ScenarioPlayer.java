package com.sigmundgranaas.forgero.mc.testcommon.scenario;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Wrapper for the player in a gameplay scenario.
 */
record ScenarioPlayer(ServerPlayerEntity player) {
}
