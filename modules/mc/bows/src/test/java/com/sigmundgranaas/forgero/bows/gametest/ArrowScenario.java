package com.sigmundgranaas.forgero.bows.gametest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntity;
import com.sigmundgranaas.forgero.bows.handlers.LaunchProjectileHandler;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.useinteraction.UseContext;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;

/**
 * Harness for validating the dynamic-bow promise in real gameplay: that a bow's attributes actually
 * change the projectile that flies.
 * <p>
 * It fires through the real launch path ({@link LaunchProjectileHandler#apply} with a fully-drawn
 * {@link UseContext}, exactly as {@code DynamicArrowEntity.onCollision} and the bow mixin route to),
 * spawning a real {@link DynamicArrowEntity}, and retrieves it by owner so the test can read its
 * actual {@code getVelocity()}. Bows are built synthetically so two can differ in exactly one
 * attribute (draw_power or accuracy), isolating its gameplay effect.
 */
public final class ArrowScenario {

	public static final OpenIdentifier DRAW_POWER = new OpenIdentifier("forgero", "draw_power");
	public static final OpenIdentifier ACCURACY = new OpenIdentifier("forgero", "accuracy");

	private ArrowScenario() {
	}

	/** Builds a bow ItemStack carrying exactly the given draw_power and accuracy attributes. */
	public static ItemStack syntheticBow(String name, float drawPower, float accuracy) {
		Map<String, List<?>> props = new HashMap<>();
		props.put(Attribute.KEY.key(), List.of(
				new SimpleAttribute(DRAW_POWER, drawPower),
				new SimpleAttribute(ACCURACY, accuracy)));
		Component component = new StaticComponent(
				new OpenIdentifier("forgero-test", name),
				Set.of(new OpenIdentifier("forgero", "tool"), new OpenIdentifier("forgero", "bow")),
				props);
		return ForgeroApi.converter().toStack(component)
				.orElseThrow(() -> new IllegalStateException("Could not build synthetic bow: " + name));
	}

	/** A registered Forgero arrow (so the launch spawns a DynamicArrowEntity, not a vanilla arrow). */
	public static ItemStack forgeroArrow() {
		Component component = ForgeroApi.componentRegistry().get(new OpenIdentifier("forgero", "oak-arrow"))
				.orElseThrow(() -> new IllegalStateException("forgero:oak-arrow is not registered"));
		return ForgeroApi.converter().toStack(component)
				.orElseThrow(() -> new IllegalStateException("Could not convert forgero:oak-arrow to a stack"));
	}

	/** Fires {@code bow} (with {@code arrow} available) from {@code player} at full draw, the real launch path. */
	public static void fire(TestContext context, ServerPlayerEntity player, ItemStack bow, ItemStack arrow) {
		player.setStackInHand(Hand.MAIN_HAND, bow);
		player.getInventory().insertStack(arrow.copy());
		// basePower/baseDivergence are fallbacks; the bow's own draw_power/accuracy attributes drive the shot.
		new LaunchProjectileHandler(3.0f, 1.0f)
				.apply(UseContext.release(context.getWorld(), player, Hand.MAIN_HAND, bow, 20, 0, 1.0f));
	}

	/** The dynamic arrows owned by {@code player} (filtered by owner to ignore other tests' arrows). */
	public static List<DynamicArrowEntity> arrowsOf(TestContext context, ServerPlayerEntity player) {
		return context.getWorld().getEntitiesByClass(DynamicArrowEntity.class,
				player.getBoundingBox().expand(500.0),
				arrow -> arrow.getOwner() != null && arrow.getOwner().getUuid().equals(player.getUuid()));
	}
}
