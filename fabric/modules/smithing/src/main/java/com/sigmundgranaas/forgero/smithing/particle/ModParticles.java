package com.sigmundgranaas.forgero.smithing.particle;

import com.sigmundgranaas.forgero.core.Forgero;

import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public final class ModParticles {
	private static boolean registered = false;

	public static final DefaultParticleType WORKABLE_WAX = FabricParticleTypes.simple();

	private ModParticles() {
	}

	public static void register() {
		if (registered) {
			return;
		}

		registered = true;

		Registry.register(
				Registries.PARTICLE_TYPE,
				id("workable_wax"),
				WORKABLE_WAX
		);
	}

	private static Identifier id(String name) {
		return new Identifier(Forgero.NAMESPACE, name);
	}
}
