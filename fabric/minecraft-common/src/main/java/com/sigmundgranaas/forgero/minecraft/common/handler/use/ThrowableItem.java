package com.sigmundgranaas.forgero.minecraft.common.handler.use;

import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.cachedFilteredFeatures;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.entity.Entities;
import com.sigmundgranaas.forgero.minecraft.common.feature.onhit.block.OnHitBlockFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.onhit.entity.OnHitEntityFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.tick.EntityTickFeatureExecutor;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ThrowableItem extends PersistentProjectileEntity {
	private static final TrackedData<ItemStack> STACK = DataTracker.registerData(ThrowableItem.class, TrackedDataHandlerRegistry.ITEM_STACK);
	private static final TrackedData<Float> weight = DataTracker.registerData(ThrowableItem.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> initialPitch = DataTracker.registerData(ThrowableItem.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> initialYaw = DataTracker.registerData(ThrowableItem.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<String> spinTypeData = DataTracker.registerData(ThrowableItem.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<Boolean> hasHit = DataTracker.registerData(ThrowableItem.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static Identifier THROWN_ENTITY_IDENTIFIER = new Identifier("forgero", "thrown_entity");

	private final MatchContext tickContext;

	public ThrowableItem(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
		super(entityType, world);
		this.tickContext = MatchContext.of(new MatchContext.KeyValuePair(ENTITY, this), new MatchContext.KeyValuePair(WORLD, this.getWorld()));

	}

	public ThrowableItem(World world, LivingEntity owner, ItemStack itemStack, Float weight, SpinType spinType) {
		super(Entities.THROWN_ITEM_ENTITY, owner, world);
		this.getDataTracker().set(STACK, itemStack);
		this.getDataTracker().set(spinTypeData, spinType.toString());
		this.getDataTracker().set(ThrowableItem.weight, weight);
		this.getDataTracker().set(hasHit, false);
		this.getDataTracker().set(initialPitch, 0f);
		this.getDataTracker().set(initialYaw, 0f);
		this.tickContext = MatchContext.of(new MatchContext.KeyValuePair(ENTITY, this), new MatchContext.KeyValuePair(WORLD, this.getWorld()));

	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		this.getDataTracker().startTracking(STACK, ItemStack.EMPTY);
		this.getDataTracker().startTracking(weight, 0.0F);
		this.getDataTracker().startTracking(spinTypeData, SpinType.NONE.toString());
		this.getDataTracker().startTracking(hasHit, false);
		this.getDataTracker().startTracking(initialPitch, 0f);
		this.getDataTracker().startTracking(initialYaw, 0f);
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);
		this.getDataTracker().set(hasHit, true);
	}

	public SpinType getSpinType() {
		return SpinType.valueOf(this.getDataTracker().get(spinTypeData));
	}

	public boolean isInGround() {
		return this.inGround;
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
	public void tick() {
		if (this.getStack().isEmpty()) {
			this.discard();
		} else {
			super.tick();
			EntityTickFeatureExecutor.initFromStack(this.getStack(), this).execute(tickContext);

			if (!this.noClip && !isInGround()) {
				Vec3d vec3d4 = this.getVelocity();
				this.setVelocity(vec3d4.x, vec3d4.y - getGravity(), vec3d4.z);
			}
		}
	}

	private double getGravity() {
		if (getStack() != null) {
			float weight = StateService.INSTANCE.convert(getStack())
					.map(state -> ComputedAttribute.apply(state, Weight.KEY))
					.orElse(20f);
			if (weight >= 10f) {
				double logModifier = Math.log10(weight) - Math.log10(20f);
				return weight * 0.001 * logModifier;
			}
		}
		return 0f;
	}

	public ItemStack getStack() {
		return this.getDataTracker().get(STACK);
	}

	@Override
	protected void onBlockHit(BlockHitResult blockHitResult) {
		super.onBlockHit(blockHitResult);

		MatchContext context = MatchContext.of()
				.put(ENTITY, this.getOwner())
				.put(WORLD, this.getWorld());
		BlockPos pos = blockHitResult.getBlockPos();
		cachedFilteredFeatures(this.asItemStack(), OnHitBlockFeature.KEY, context.put(BLOCK_TARGET, this.getWorld().getBlockState(pos)))
				.forEach(handler -> {
					handler.onHit(this.getOwner(), this.getWorld(), pos);
					handler.handle(this, this.asItemStack(), Hand.MAIN_HAND);
				});
	}

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult) {
		super.onEntityHit(entityHitResult);

		Entity entity = entityHitResult.getEntity();

		MatchContext context = MatchContext.of()
				.put(ENTITY, this.getOwner())
				.put(WORLD, this.getWorld());

		cachedFilteredFeatures(asItemStack(), OnHitEntityFeature.KEY, context.put(ENTITY_TARGET, entityHitResult.getEntity()))
				.forEach(handler -> {
					handler.onHit(this.getOwner(), this.getWorld(), entityHitResult.getEntity());
					handler.handle(this, asItemStack(), Hand.MAIN_HAND);
				});

		float damage = (float) getDamage(entity);

		Entity owner = this.getOwner();
		DamageSource damageSource;
		if (owner == null) {
			damageSource = super.getDamageSources().arrow(this, this);
		} else {
			damageSource = super.getDamageSources().arrow(this, owner);
			if (owner instanceof LivingEntity) {
				((LivingEntity) owner).onAttacking(entity);
			}
		}

		boolean damaged = entity.damage(damageSource, damage);

		if (damaged) {
			asItemStack().damage(1, Random.create(), null);
		}
		this.dropStack(this.asItemStack(), 0.1F);
		this.discard();
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

	public double getDamage(Entity target) {
		MatchContext ctx = MatchContext.of().put(ENTITY_TARGET, target).put(WORLD, this.getWorld());
		float damage = StateService.INSTANCE.convert(asItemStack())
				.map(container -> ComputedAttribute.of(ContainerTargetPair.of(container, ctx), AttackDamage.KEY))
				.map(ComputedAttribute::asFloat)
				.orElse(1f);
		// Capping the max damage at 2x the item's max damage
		return Math.min(damage * getVelocity().length(), damage * 2);
	}

	public void setVelocity(LivingEntity user, float pitch, float yaw, float roll, float velocityMultiplier, float inaccuracy) {
		super.setVelocity(user, pitch, yaw, roll, velocityMultiplier / 10, inaccuracy);
		this.getDataTracker().set(initialPitch, this.getPitch());
		this.getDataTracker().set(initialYaw, this.getYaw());
	}

	public float getInitialYaw() {
		return this.getDataTracker().get(initialYaw);
	}

	public float getInitialPitch() {
		return this.getDataTracker().get(initialPitch);
	}

	public enum SpinType {
		VERTICAL,
		HORIZONTAL,
		NONE
	}
}
