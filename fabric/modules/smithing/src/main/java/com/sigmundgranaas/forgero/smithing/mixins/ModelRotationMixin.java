package com.sigmundgranaas.forgero.smithing.mixins;

import java.util.HashMap;
import java.util.Map;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.util.math.AffineTransformation;

@Mixin(ModelRotation.class)
public abstract class ModelRotationMixin {

	// Custom rotation storage
	private static final Map<String, Float> customRotations = new HashMap<>();
	private static boolean enableCustomRotations = false;

	@Inject(method = "getRotation", at = @At("HEAD"), cancellable = true)
	private void forgero$customRotation(CallbackInfoReturnable<AffineTransformation> cir) {
		if (enableCustomRotations) {
			cir.setReturnValue(createCustomRotation());
		}
	}

	/**
	 * Creates a custom rotation transformation
	 */
	private static AffineTransformation createCustomRotation() {
		float xRotation = customRotations.getOrDefault("x", 0.0f);
		float yRotation = customRotations.getOrDefault("y", 0.0f);
		float zRotation = customRotations.getOrDefault("z", 0.0f);

		Quaternionf quaternion = new Quaternionf()
				.rotateX((float) Math.toRadians(xRotation))
				.rotateY((float) Math.toRadians(yRotation))
				.rotateZ((float) Math.toRadians(zRotation));

		return new AffineTransformation(
				new Vector3f(0, 0, 0),  // translation
				quaternion,              // rotation
				new Vector3f(1, 1, 1),  // scale
				null                     // right rotation
		);
	}

	// Public API methods to control custom rotations
	@Unique
	private static void setCustomRotation(float x, float y, float z) {
		customRotations.put("x", x);
		customRotations.put("y", y);
		customRotations.put("z", z);
		enableCustomRotations = true;
	}

	@Unique
	private static void setCustomXRotation(float degrees) {
		customRotations.put("x", degrees);
		enableCustomRotations = true;
	}

	@Unique
	private static void setCustomYRotation(float degrees) {
		customRotations.put("y", degrees);
		enableCustomRotations = true;
	}

	@Unique
	private static void setCustomZRotation(float degrees) {
		customRotations.put("z", degrees);
		enableCustomRotations = true;
	}

	@Unique
	private static void enableCustomRotations() {
		enableCustomRotations = true;
	}

	@Unique
	private static void disableCustomRotations() {
		enableCustomRotations = false;
	}

	@Unique
	private static void resetRotations() {
		customRotations.clear();
		enableCustomRotations = false;
	}

	@Unique
	private static boolean isCustomRotationEnabled() {
		return enableCustomRotations;
	}
}
