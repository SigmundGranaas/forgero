package com.sigmundgranaas.forgero.bow.entity;

import static com.sigmundgranaas.forgero.bow.ForgeroBowInitializer.DYNAMIC_ARROW_ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.cachedFilteredFeatures;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.feature.EntityTickFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.OnHitBlockFeature;
import com.sigmundgranaas.forgero.minecraft.common.feature.OnHitEntityFeature;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.world.World;

public class DynamicArrowEntity extends PersistentProjectileEntity {
	private static final TrackedData<ItemStack> STACK;
	public static Identifier DYNAMIC_ARROW_IDENTIFIER = new Identifier("forgero:dynamic_arrow");

	static {
		STACK = DataTracker.registerData(DynamicArrowEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	}

	public DynamicArrowEntity(EntityType<DynamicArrowEntity> entityType, World world) {
		super(entityType, world);
	}

	public DynamicArrowEntity(World world, LivingEntity owner, ItemStack stack) {
		super(DYNAMIC_ARROW_ENTITY, owner, world);
		setStack(stack.copy());
		StateService.INSTANCE.convert(stack)
				.map(state -> ComputedAttribute.apply(state, AttackDamage.KEY))
				.ifPresent(this::setDamage);
	}


	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		if (!this.getStack().isEmpty()) {
			nbt.put("Item", this.getStack().writeNbt(new NbtCompound()));
		}
	}

	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		NbtCompound nbtCompound = nbt.getCompound("Item");
		this.setStack(ItemStack.fromNbt(nbtCompound));
		if (this.getStack().isEmpty()) {
			this.discard();
		}

	}

	@Override
	public void tick() {
		if (this.getStack().isEmpty()) {
			this.discard();
		} else {
			super.tick();

			cachedFilteredFeatures(this.getStack(), EntityTickFeature.KEY, MatchContext.of().put(ENTITY, this).put(WORLD, this.getWorld()))
					.forEach(handler -> handler.handle(this));

			if (!this.noClip) {
				Vec3d vec3d4 = this.getVelocity();
				this.setVelocity(vec3d4.x, vec3d4.y - getGravity(), vec3d4.z);
			}
		}
	}


	public boolean isFireImmune() {
		return this.getStack().getItem().isFireproof() || super.isFireImmune();
	}

	protected void initDataTracker() {
		super.initDataTracker();
		this.getDataTracker().startTracking(STACK, ItemStack.EMPTY);
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

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);
		MatchContext context = MatchContext.of()
				.put(ENTITY, this.getOwner())
				.put(WORLD, this.getWorld());

		if (hitResult instanceof BlockHitResult blockHitResult && getWorld().getBlockState(blockHitResult.getBlockPos()).getBlock() != Blocks.AIR) {
			BlockPos pos = blockHitResult.getBlockPos();
			cachedFilteredFeatures(this.getStack(), OnHitBlockFeature.KEY, context.put(BLOCK_TARGET, this.getWorld().getBlockState(pos)))
					.forEach(handler -> {
						handler.onHit(this.getOwner(), this.getWorld(), pos);
						handler.handle(this, this.getStack(), Hand.MAIN_HAND);
					});
			super.onCollision(hitResult);
		} else if (hitResult instanceof EntityHitResult entityHitResult) {
			cachedFilteredFeatures(this.getStack(), OnHitEntityFeature.KEY, context.put(ENTITY_TARGET, entityHitResult.getEntity()))
					.forEach(handler -> {
						handler.onHit(this.getOwner(), this.getWorld(), entityHitResult.getEntity());
						handler.handle(this, this.getStack(), Hand.MAIN_HAND);
					});
		}
	}

	@Override
	protected ItemStack asItemStack() {
		return getStack();
	}

	public ItemStack getStack() {
		return this.getDataTracker().get(STACK);
	}

	public void setStack(ItemStack stack) {
		this.getDataTracker().set(STACK, stack);
	}

	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		super.onTrackedDataSet(data);
		if (STACK.equals(data)) {
			this.getStack().setHolder(this);
		}
	}
}
