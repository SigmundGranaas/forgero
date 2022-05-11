package com.sigmundgranaas.forgero.toolhandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class MagneticHandler {
    private final Entity rootEntity;
    private final Vec3d rootVec;

    public MagneticHandler(Entity rootEntity) {
        this.rootEntity = rootEntity;
        this.rootVec = rootEntity.getPos();
    }

    public List<Entity> getNearbyEntities(int range) {
        BlockPos pos1 = new BlockPos(rootVec.x + range, rootVec.y + range, rootVec.z + range);
        BlockPos pos2 = new BlockPos(rootVec.x - range, rootVec.y - range, rootVec.z - range);
        return rootEntity.getWorld().getOtherEntities(rootEntity, new Box(pos1, pos2), entity -> entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity);
    }

    public void pullEntities(int power, List<Entity> entities) {
        for (Entity nearbyEntity : entities) {
            if (!nearbyEntity.getBlockPos().equals(rootEntity.getBlockPos())) {
                Vec3d velocity = nearbyEntity.getPos().relativize(new Vec3d(rootVec.x + 0.5, rootVec.y + 0.5, rootVec.z + 0.5)).normalize().multiply(0.01f * power);
                nearbyEntity.addVelocity(velocity.x, velocity.y, velocity.z);
            }
        }
    }
}
