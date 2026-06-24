package com.sigmundgranaas.forgero.bows.gametest;

import java.util.List;

import com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntity;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.Vec3d;

/**
 * Proves the flagship dynamic-bow promise in REAL gameplay: a bow's attributes change the projectile
 * that actually flies. The existing bow launch tests assert only "an arrow spawned"; these fire two
 * bows that differ in exactly one attribute (via {@link ArrowScenario}) and compare the resulting
 * {@link DynamicArrowEntity}'s real velocity / divergence.
 */
public class ArrowPhysicsGametest implements FabricGameTest {

	private static ServerPlayerEntity aimedPlayer(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.setPitch(0.0f);
		player.setYaw(0.0f);
		return player;
	}

	/** Higher draw_power must produce a measurably faster arrow. */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void higher_draw_power_gives_higher_velocity(TestContext context) {
		ServerPlayerEntity weak = aimedPlayer(context);
		ServerPlayerEntity strong = aimedPlayer(context);

		ArrowScenario.fire(context, weak, ArrowScenario.syntheticBow("scn_bow_weak", 2.0f, 90.0f), ArrowScenario.forgeroArrow());
		ArrowScenario.fire(context, strong, ArrowScenario.syntheticBow("scn_bow_strong", 8.0f, 90.0f), ArrowScenario.forgeroArrow());

		// Read on tick 1 so both arrows have had identical (minimal) drag applied.
		context.waitAndRun(1, () -> {
			double slow = ArrowScenario.arrowsOf(context, weak).get(0).getVelocity().length();
			double fast = ArrowScenario.arrowsOf(context, strong).get(0).getVelocity().length();
			context.assertTrue(fast > slow + 1.0,
					"draw_power 8 must launch the arrow faster than draw_power 2 (slow=" + slow + ", fast=" + fast + ")");
			context.complete();
		});
	}

	/** Higher accuracy must produce a tighter spread (less divergence between shots). */
	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, required = true)
	public void higher_accuracy_reduces_divergence(TestContext context) {
		ServerPlayerEntity sloppy = aimedPlayer(context);
		ServerPlayerEntity precise = aimedPlayer(context);

		int shots = 12;
		for (int i = 0; i < shots; i++) {
			ArrowScenario.fire(context, sloppy, ArrowScenario.syntheticBow("scn_bow_sloppy_" + i, 3.0f, 30.0f), ArrowScenario.forgeroArrow());
			ArrowScenario.fire(context, precise, ArrowScenario.syntheticBow("scn_bow_precise_" + i, 3.0f, 100.0f), ArrowScenario.forgeroArrow());
		}

		context.waitAndRun(1, () -> {
			double sloppySpread = dispersion(ArrowScenario.arrowsOf(context, sloppy));
			double preciseSpread = dispersion(ArrowScenario.arrowsOf(context, precise));
			context.assertTrue(preciseSpread < sloppySpread,
					"accuracy 100 must keep a tighter spread than accuracy 30 (sloppy=" + sloppySpread
							+ ", precise=" + preciseSpread + ")");
			context.complete();
		});
	}

	/**
	 * Mean angular dispersion of a group of arrows around their OWN mean direction. Measuring spread
	 * relative to the group mean (not the horizontal aim) cancels the common downward gravity tilt
	 * every arrow shares after a tick, isolating the divergence the accuracy attribute controls.
	 */
	private static double dispersion(List<DynamicArrowEntity> arrows) {
		if (arrows.size() < 2) {
			return 0.0;
		}
		Vec3d mean = Vec3d.ZERO;
		for (DynamicArrowEntity arrow : arrows) {
			mean = mean.add(arrow.getVelocity().normalize());
		}
		mean = mean.normalize();
		double sum = 0.0;
		for (DynamicArrowEntity arrow : arrows) {
			sum += 1.0 - mean.dotProduct(arrow.getVelocity().normalize());
		}
		return sum / arrows.size();
	}
}
