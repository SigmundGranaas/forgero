package com.sigmundgranaas.forgerocommon.toolhandler;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Predicate;

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


public class MagneticHandler {
    private final Entity rootEntity;
    private final Vec3d rootVec;

    public MagneticHandler(Entity rootEntity) {
        this.rootEntity = rootEntity;
        this.rootVec = rootEntity.getPos();
    }

    public List<Entity> getNearbyEntities(int range, Predicate<Entity> predicate) {
        BlockPos pos1 = new BlockPos(rootVec.x + range, rootVec.y + range, rootVec.z + range);
        BlockPos pos2 = new BlockPos(rootVec.x - range, rootVec.y - range, rootVec.z - range);
        return rootEntity.getWorld().getOtherEntities(rootEntity, new Box(pos1, pos2), predicate);
    }

    public void pullEntities(int power, List<Entity> entities) {
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
}
