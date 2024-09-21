package com.sigmundgranaas.forgero.handler.use;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;


/**
 * Handler for throwing tridents
 * Used for testing
 */
@Deprecated
public class ThrowTridentHandler implements StopHandler {
	public static final String TYPE = "forgero:throw_trident";
	public static final ClassKey<ThrowTridentHandler> KEY = new ClassKey<>(TYPE, ThrowTridentHandler.class);
	public static final JsonBuilder<ThrowTridentHandler> BUILDER = HandlerBuilder.fromObject(ThrowTridentHandler.class, (json) -> new ThrowTridentHandler());

	@Override
	public void stoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		if (!world.isClient) {
			TridentEntity tridentEntity = new TridentEntity(world, user, stack.copy());
			tridentEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 2.5f, 1.0f);
			world.spawnEntity(tridentEntity);
		}
	}
}
