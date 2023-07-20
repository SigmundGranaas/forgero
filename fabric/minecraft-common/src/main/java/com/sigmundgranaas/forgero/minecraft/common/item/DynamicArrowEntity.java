package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.state.State;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DynamicArrowEntity extends ArrowEntity {
	private final State state;

	public DynamicArrowEntity(World world, LivingEntity shooter, State state) {
		super(world, shooter);
		this.state = state;
	}

	@Override
	public void tick() {
		super.tick();
		// Then manually apply our own gravity
		if (!this.noClip) {
			Vec3d vec3d4 = this.getVelocity();
			this.setVelocity(vec3d4.x, vec3d4.y - getGravity(), vec3d4.z);
		}
	}

	private double getGravity() {
		return 0.001 * (Weight.apply(state));
	}
}
