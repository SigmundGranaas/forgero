package com.sigmundgranaas.forgero.minecraft.common.handler.use;

import com.sigmundgranaas.forgero.minecraft.common.entity.Entities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ThrowableItem extends PersistentProjectileEntity {
	private static final TrackedData<ItemStack> STACK = DataTracker.registerData(ThrowableItem.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<Float> weight = DataTracker.registerData(ThrowableItem.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<String> spinTypeData = DataTracker.registerData(ThrowableItem.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<Boolean> hasHit = DataTracker.registerData(ThrowableItem.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static Identifier THROWN_ENTITY_IDENTIFIER = new Identifier("forgero", "thrown_entity");

	public ThrowableItem(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	public ThrowableItem(World world, LivingEntity owner, ItemStack itemStack, Float weight, SpinType spinType) {
		super(Entities.THROWN_ITEM_ENTITY, owner, world);
		this.getDataTracker().set(STACK, itemStack);
		this.getDataTracker().set(spinTypeData, spinType.toString());
		this.getDataTracker().set(ThrowableItem.weight, weight);
		this.getDataTracker().set(hasHit, false);
	}


	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		this.getDataTracker().startTracking(STACK, ItemStack.EMPTY);
		this.getDataTracker().startTracking(weight, 0.0F);
		this.getDataTracker().startTracking(spinTypeData, SpinType.NONE.toString());
		this.getDataTracker().startTracking(hasHit, false);
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);
		this.getDataTracker().set(hasHit, true);
		if (!this.world.isClient) {
			if (hitResult.getType() == HitResult.Type.BLOCK) {
				this.setNoGravity(true);
				this.setVelocity(Vec3d.ZERO);
				this.setPos(hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);
			}
		}
	}

	public SpinType getSpinType() {
		return SpinType.valueOf(this.getDataTracker().get(spinTypeData));
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat("weight", this.getDataTracker().get(weight));
		nbt.putString("spinType", this.getDataTracker().get(spinTypeData));
		if (!this.asItemStack().isEmpty()) {
			nbt.put("Item", this.asItemStack().writeNbt(new NbtCompound()));
		}
	}


	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		this.getDataTracker().set(weight, nbt.getFloat("weight"));
		this.getDataTracker().set(spinTypeData, nbt.getString("spinType"));
		this.getDataTracker().set(STACK, ItemStack.fromNbt(nbt.getCompound("Item")));
	}

	@Override
	protected ItemStack asItemStack() {
		return this.getDataTracker().get(STACK);
	}

	@Override
	public double getDamage() {
		// Capping the max damage at 2x the item's max damage
		return Math.min(asItemStack().getDamage() * getVelocity().length(), asItemStack().getDamage() * 2);
	}

	public void setVelocity(LivingEntity user, float pitch, float yaw, float roll, float velocityMultiplier, float inaccuracy) {
		super.setVelocity(user, pitch, yaw, roll, velocityMultiplier / 10, inaccuracy);
	}

	public enum SpinType {
		VERTICAL,
		HORIZONTAL,
		NONE
	}
}
