package com.sigmundgranaas.forgero.minecraft.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EnderTeleportationEntity extends ThrownEntity {
	public static EntityType<EnderTeleportationEntity> ENDER_TELEPORT_ENTITY;
	private int maxLength;
	private LivingEntity owner;
	private int lifeTicks;
	private Vec3d targetPosition = new Vec3d(0f, 0f, 0f);

	public EnderTeleportationEntity(EntityType<? extends ThrownEntity> entityType, World world) {
		super(entityType, world);

	}

	public EnderTeleportationEntity(World world, LivingEntity owner, int maxLength) {
		super(ENDER_TELEPORT_ENTITY, owner, world);
		this.maxLength = maxLength;
		this.owner = owner;
		this.lifeTicks = 0;
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
		world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
		if (!this.world.isClient && !this.isRemoved()) {
			Entity entity = this.getOwner();
			if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
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
				BlockPos pos = new BlockPos(hitResult.getPos());
				if (world.getBlockState(pos.add(0, 1, 0)).isAir()) {
					entity.requestTeleport(pos.getX(), pos.getY() + 1, pos.getZ());
				} else {
					entity.requestTeleport(pos.getX(), pos.getY(), pos.getZ());
				}
				entity.onLanding();
			}
			this.discard();
		}


	}

	private void spawnTeleportationParticles() {
		double d = this.getX();
		double e = this.getY();
		double f = this.getZ();
		for (int i = 0; i < 20; ++i) {
			this.world.addParticle(ParticleTypes.PORTAL, d, e, f, this.random.nextGaussian() / 5, this.random.nextGaussian() / 5, this.random.nextGaussian() / 5);
		}
	}

	@Override
	protected void initDataTracker() {

	}


	public void baseTick() {
		super.baseTick();
		spawnTeleportationParticles();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.owner != null) {
			spawnTeleportationParticles();
			// Update blink projectile position and direction
			targetPosition = calculateTargetPosition();

			// Smoothly move the entity towards the target position
			double smoothingFactor = 0.1;
			double dx = (targetPosition.x - this.getX()) * smoothingFactor;
			double dy = (targetPosition.y - this.getY()) * smoothingFactor;
			double dz = (targetPosition.z - this.getZ()) * smoothingFactor;

			// Update the entity's position
			this.setVelocity(dx, dy, dz);
			this.move(MovementType.SELF, this.getVelocity());
			this.lifeTicks++;

			// Check for block collision and teleport player
			if (!this.world.isAir(this.getBlockPos())) {
				// Teleport the player to the projectile's position
				if (!this.world.isClient) {
					owner.teleport(this.getX(), this.getY(), this.getZ());
				}

				// Remove the blink projectile entity after teleporting
				this.remove(RemovalReason.KILLED);
			}

			// Limit the blink projectile
			// Limit the blink projectile's life
		}

		if (this.lifeTicks > 1000) {
			this.remove(RemovalReason.DISCARDED);
		}
	}

	public Vec3d calculateTargetPosition() {
		Vec3d direction = owner.getRotationVector();
		double distanceMultiplier = lifeTicks * 0.1;
		return owner.getPos().add(direction.multiply(distanceMultiplier)).add(0, owner.getEyeHeight(owner.getPose()) - 0.5, 0);
	}

	@Override
	public boolean hasNoGravity() {
		return true;
	}

	private float numberValue(double value) {
		return (float) Math.sqrt(Math.pow(value, 2));
	}

	private double calculateDistanceFromOrigin(PlayerEntity entity) {
		return squaredDistanceTo(entity);
	}
}
