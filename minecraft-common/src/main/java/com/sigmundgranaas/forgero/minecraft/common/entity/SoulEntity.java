package com.sigmundgranaas.forgero.minecraft.common.entity;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.soul.Soul;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.sigmundgranaas.forgero.minecraft.common.entity.Entities.SOUL_ENTITY;

public class SoulEntity extends PathAwareEntity {
    public static final Identifier SOUL_ENTITIY_IDENTIFIER = new Identifier(Forgero.NAMESPACE, "soul_entity");
    private final Soul soul;

    public SoulEntity(World world, Soul soul, double x, double y, double z) {
        super(SOUL_ENTITY, world);
        this.setPosition(x, y, z);
        this.soul = soul;
    }

    public SoulEntity(World world, Soul soul) {
        super(SOUL_ENTITY, world);
        this.soul = soul;
    }

    public SoulEntity(EntityType<? extends SoulEntity> type, World world) {
        super(type, world);
        this.soul = new Soul();
    }

    public Soul getSoul() {
        return soul;
    }

    public void playSoulSound() {
        this.playSound(SoundEvents.PARTICLE_SOUL_ESCAPE, getSoundVolume(), getSoundPitch());
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_DEATH;
    }

    public void createSoulParticles() {
        Vec3d vec3d = this.getVelocity();
        for (int i = 0; i < 10; i++) {
            this.world.addParticle(ParticleTypes.SOUL, this.getX() + (this.random.nextDouble() - 0.5) * (double) this.getWidth(), this.getY() + 0.1, this.getZ() + (this.random.nextDouble() - 0.5) * (double) this.getWidth(), vec3d.x * -0.2, 0.1, vec3d.z * -0.2);
        }
    }
}
