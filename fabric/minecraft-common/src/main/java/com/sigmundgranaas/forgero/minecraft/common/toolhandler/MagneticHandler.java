package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.RunnableHandler;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.core.property.v2.cache.RunnableHandlerCache;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Copyright 2021 Luligabi
 * <p>
 * Modifications copyright (C) 2022 Sigmund Granaas Sandring
 * <p>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 *
 * <a href="https://github.com/Luligabi1/Incantationem">Original work</a>
 */


public class MagneticHandler implements RunnableHandler {
	public static String MAGNETIC_TYPE = "MAGNETIC";
	private final Entity rootEntity;
	private final Vec3d rootVec;

	private final float power;

	private final int distance;

	public MagneticHandler(Entity rootEntity, float power, int level) {
		this.rootEntity = rootEntity;
		this.rootVec = rootEntity.getPos();
		this.power = power;
		this.distance = level;
	}

	public static Optional<RunnableHandler> of(PropertyContainer container, Entity rootEntity) {
		var key = new PropertyTargetCacheKey(ContainerTargetPair.of(container), MAGNETIC_TYPE);
		boolean hasMagnetic = ContainsFeatureCache.containsFeatureCache.getUnchecked(key);
		if (hasMagnetic) {
			return Optional.of(RunnableHandlerCache.computeIfAbsent(key, () -> createMagneticHandler(container, rootEntity)));
		}
		return Optional.empty();
	}

	public static RunnableHandler createMagneticHandler(PropertyContainer container, Entity rootEntity) {
		var magnetic = container.stream().features().filter(prop -> prop.type().equals(MAGNETIC_TYPE)).toList();
		var value = magnetic.stream().map(data -> 1f)
				.reduce(0f, Float::sum);
		Optional<Integer> level = magnetic.stream().map(data -> 1).findFirst();
		return level.map(l -> (RunnableHandler) new MagneticHandler(rootEntity, value, l + 3)).orElse(RunnableHandler.EMPTY);
	}


	public List<Entity> getNearbyEntities(int range, Predicate<Entity> predicate) {
		BlockPos pos1 = new BlockPos(rootVec.x + range, rootVec.y + range, rootVec.z + range);
		BlockPos pos2 = new BlockPos(rootVec.x - range, rootVec.y - range, rootVec.z - range);
		return rootEntity.getWorld().getOtherEntities(rootEntity, new Box(pos1, pos2), predicate);
	}

	public void pullEntities(List<Entity> entities) {
		for (Entity nearbyEntity : entities) {
			double dist = nearbyEntity.getPos().distanceTo(rootVec);
			if (dist < 1) {
				nearbyEntity.addVelocity(0, 0, 0);
			} else {
				Vec3d velocity = nearbyEntity.getPos().relativize(rootVec).normalize().multiply(0.02f * power);
				nearbyEntity.addVelocity(velocity.x, velocity.y, velocity.z);
			}
		}
	}

	@Override
	public void run() {
		pullEntities(getNearbyEntities(distance, entity -> entity instanceof ItemEntity));
	}

	@Override
	public String type() {
		return MAGNETIC_TYPE;
	}
}
