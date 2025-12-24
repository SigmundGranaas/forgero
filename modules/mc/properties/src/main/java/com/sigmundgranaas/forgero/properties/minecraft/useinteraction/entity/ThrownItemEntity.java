package com.sigmundgranaas.forgero.properties.minecraft.useinteraction.entity;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitManager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A projectile entity that represents a thrown item.
 * This is a simplified version for the new UseInteraction system.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Stores the thrown ItemStack</li>
 *   <li>Configurable spin type for rendering</li>
 *   <li>Weight-based gravity</li>
 *   <li>Drops item on entity hit or pickup</li>
 * </ul>
 */
public class ThrownItemEntity extends PersistentProjectileEntity {
	public static final Identifier IDENTIFIER = new Identifier("forgero", "thrown_item");

	private static final TrackedData<ItemStack> ITEM_STACK = DataTracker.registerData(ThrownItemEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<Float> WEIGHT = DataTracker.registerData(ThrownItemEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<String> SPIN_TYPE = DataTracker.registerData(ThrownItemEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<Float> INITIAL_PITCH = DataTracker.registerData(ThrownItemEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> INITIAL_YAW = DataTracker.registerData(ThrownItemEntity.class, TrackedDataHandlerRegistry.FLOAT);

	private static final float DEFAULT_WEIGHT = 10f;
	private static final float BASE_DAMAGE = 5f;

	/**
	 * Attribute identifier for resolving attack damage from Forgero components.
	 */
	private static final OpenIdentifier ATTACK_DAMAGE_ATTR = new OpenIdentifier("forgero", "attack_damage");

	public ThrownItemEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
		super(entityType, world);
	}

	public ThrownItemEntity(EntityType<? extends ThrownItemEntity> entityType, World world, LivingEntity owner, ItemStack stack, float weight, SpinType spinType) {
		super(entityType, owner, world);
		this.dataTracker.set(ITEM_STACK, stack.copy());
		this.dataTracker.set(WEIGHT, weight);
		this.dataTracker.set(SPIN_TYPE, spinType.name());
		this.dataTracker.set(INITIAL_PITCH, owner.getPitch());
		this.dataTracker.set(INITIAL_YAW, owner.getYaw());
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(ITEM_STACK, ItemStack.EMPTY);
		this.dataTracker.startTracking(WEIGHT, DEFAULT_WEIGHT);
		this.dataTracker.startTracking(SPIN_TYPE, SpinType.NONE.name());
		this.dataTracker.startTracking(INITIAL_PITCH, 0f);
		this.dataTracker.startTracking(INITIAL_YAW, 0f);
	}

	@Override
	public void tick() {
		if (this.getThrownStack().isEmpty()) {
			this.discard();
			return;
		}

		super.tick();

		if (!this.noClip && !this.inGround) {
			Vec3d velocity = this.getVelocity();
			this.setVelocity(velocity.x, velocity.y - getGravity(), velocity.z);
		}
	}

	private double getGravity() {
		float weight = this.dataTracker.get(WEIGHT);
		if (weight >= 1f) {
			double logModifier = Math.log10(weight) - Math.log10(2f);
			return weight * 0.01 * logModifier;
		}
		return 0.01;
	}

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult) {
		super.onEntityHit(entityHitResult);

		Entity target = entityHitResult.getEntity();
		ItemStack stack = this.getThrownStack();
		float damage = calculateDamage(stack);

		Entity owner = this.getOwner();
		DamageSource damageSource;
		if (owner == null) {
			damageSource = this.getDamageSources().arrow(this, this);
		} else {
			damageSource = this.getDamageSources().arrow(this, owner);
			if (owner instanceof LivingEntity livingOwner) {
				livingOwner.onAttacking(target);
			}
		}

		boolean damaged = target.damage(damageSource, damage);

		if (damaged) {
			if (stack.isDamageable()) {
				stack.setDamage(stack.getDamage() + 1);
			}

			// Trigger OnHit effects from the thrown item
			if (owner != null && !stack.isEmpty()) {
				OnHitManager.handleOnHit(stack, owner, target);
			}
		}

		this.dropStack(stack, 0.1f);
		this.discard();
	}

	/**
	 * Calculates damage based on velocity and attack_damage attribute.
	 * Resolves attack_damage from the component if available, otherwise uses BASE_DAMAGE.
	 *
	 * @param stack The thrown item stack
	 * @return The calculated damage value
	 */
	private float calculateDamage(ItemStack stack) {
		double velocityLength = this.getVelocity().length();
		float baseDamage = resolveAttribute(stack, ATTACK_DAMAGE_ATTR, BASE_DAMAGE);
		return (float) Math.min(baseDamage * velocityLength, baseDamage * 2);
	}

	/**
	 * Resolves an attribute value from the item's Forgero component.
	 * If the item is not a Forgero item or doesn't have the attribute, returns the fallback value.
	 *
	 * @param stack    The item stack to resolve from
	 * @param attr     The attribute identifier to query
	 * @param fallback The fallback value if resolution fails or returns zero/negative
	 * @return The resolved attribute value, or fallback if not available
	 */
	private static float resolveAttribute(ItemStack stack, OpenIdentifier attr, float fallback) {
		return ForgeroApi.converter().toComponent(stack)
				.map(component -> {
					AttributeQueryResult result = ForgeroApi.resolver()
							.resolve(component, new AttributeEngine());
					return result.getValue(attr);
				})
				.filter(value -> value > 0)
				.orElse(fallback);
	}

	@Override
	protected ItemStack asItemStack() {
		return this.getThrownStack();
	}

	public ItemStack getThrownStack() {
		return this.dataTracker.get(ITEM_STACK);
	}

	public SpinType getSpinType() {
		String spinName = this.dataTracker.get(SPIN_TYPE);
		try {
			return SpinType.valueOf(spinName);
		} catch (IllegalArgumentException e) {
			return SpinType.NONE;
		}
	}

	public float getInitialPitch() {
		return this.dataTracker.get(INITIAL_PITCH);
	}

	public float getInitialYaw() {
		return this.dataTracker.get(INITIAL_YAW);
	}

	public float getWeight() {
		return this.dataTracker.get(WEIGHT);
	}

	public boolean isInGround() {
		return this.inGround;
	}

	@Override
	public void setVelocity(Entity user, float pitch, float yaw, float roll, float speed, float divergence) {
		super.setVelocity(user, pitch, yaw, roll, speed, divergence);
		this.dataTracker.set(INITIAL_PITCH, this.getPitch());
		this.dataTracker.set(INITIAL_YAW, this.getYaw());
	}

	@Override
	protected SoundEvent getHitSound() {
		return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
	}

	@Override
	public void onPlayerCollision(PlayerEntity player) {
		if (!this.getWorld().isClient && (this.inGround || this.isNoClip()) && this.shake <= 0) {
			if (this.tryPickup(player)) {
				player.sendPickup(this, 1);
				this.discard();
			}
		}
	}

	@Override
	protected boolean tryPickup(PlayerEntity player) {
		return switch (this.pickupType) {
			case ALLOWED -> player.getInventory().insertStack(this.getThrownStack());
			case CREATIVE_ONLY -> player.getAbilities().creativeMode;
			default -> false;
		};
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat("Weight", this.dataTracker.get(WEIGHT));
		nbt.putString("SpinType", this.dataTracker.get(SPIN_TYPE));
		nbt.putFloat("InitialPitch", this.dataTracker.get(INITIAL_PITCH));
		nbt.putFloat("InitialYaw", this.dataTracker.get(INITIAL_YAW));
		if (!this.getThrownStack().isEmpty()) {
			nbt.put("ThrownItem", this.getThrownStack().writeNbt(new NbtCompound()));
		}
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		this.dataTracker.set(WEIGHT, nbt.getFloat("Weight"));
		this.dataTracker.set(SPIN_TYPE, nbt.getString("SpinType"));
		this.dataTracker.set(INITIAL_PITCH, nbt.getFloat("InitialPitch"));
		this.dataTracker.set(INITIAL_YAW, nbt.getFloat("InitialYaw"));
		if (nbt.contains("ThrownItem")) {
			this.dataTracker.set(ITEM_STACK, ItemStack.fromNbt(nbt.getCompound("ThrownItem")));
		}
	}

	/**
	 * Spin type for visual rendering of the thrown item.
	 */
	public enum SpinType {
		VERTICAL,
		HORIZONTAL,
		NONE
	}
}
