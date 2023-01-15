package com.sigmundgranaas.forgero.minecraft.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EnderTeleportationEntity extends ThrownEntity {
    private int maxLength;

    public static EntityType<EnderTeleportationEntity> ENDER_TELEPORT_ENTITY;

    public EnderTeleportationEntity(EntityType<? extends ThrownEntity> entityType, World world) {
        super(entityType, world);
    }

    public EnderTeleportationEntity(World world, LivingEntity owner, int maxLength ) {
        super(ENDER_TELEPORT_ENTITY, owner, world);
        this.maxLength = maxLength;
        super.setNoGravity(true);
    }

    @Override
    public boolean shouldRender(double distance) {
        return false;
    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        entityHitResult.getEntity().damage(DamageSource.thrownProjectile(this, this.getOwner()), 0.0F);
    }


    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        super.spawnSprintingParticles();
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        for(int i = 0; i < 20; ++i) {
            this.world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, d, e, f, this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian());
        }

        if (!this.world.isClient && !this.isRemoved()) {
            Entity entity = this.getOwner();
            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
                if (serverPlayerEntity.networkHandler.getConnection().isOpen() && serverPlayerEntity.world == this.world && !serverPlayerEntity.isSleeping()) {
                    if (entity.hasVehicle()) {
                        serverPlayerEntity.requestTeleportAndDismount(this.getX(), this.getY(), this.getZ());
                    } else {
                        entity.requestTeleport(this.getX(), this.getY(), this.getZ());
                    }
                    entity.onLanding();
                    entity.damage(DamageSource.FALL, 5.0F);
                }
            } else if (entity != null) {
                entity.requestTeleport(this.getX(), this.getY(), this.getZ());
                entity.onLanding();
            }
            this.discard();
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void initDataTracker() {

    }

    public void tick() {
        Vec3d vec3d = this.getVelocity();
        double d = this.getX() + vec3d.x;
        double e = this.getY() + vec3d.y;
        double f = this.getZ() + vec3d.z;
        this.setPosition(d, e, f);
        this.setPos(d, e, f);

        for(int i = 0; i < 32; ++i) {
            this.world.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0, this.getZ(), this.random.nextGaussian(), 0.0, this.random.nextGaussian());
        }

        Entity entity = this.getOwner();
        if (entity instanceof PlayerEntity && !entity.isAlive()) {
            this.discard();
        } else {
            super.tick();
            if(entity instanceof PlayerEntity player && calculateDistanceFromOrigin(player) > maxLength){
                onCollision(new BlockHitResult(getPos(), Direction.UP, getBlockPos(), true));
                this.discard();
            }
        }
    }
   private double calculateDistanceFromOrigin(PlayerEntity entity){
        return squaredDistanceTo(entity);
    }
}
