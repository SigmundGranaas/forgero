package com.sigmundgranaas.forgero.minecraft.common.feature;

import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public interface OnHitHandler extends Feature {
	void onHit(Entity root, World world, Entity target);
}
