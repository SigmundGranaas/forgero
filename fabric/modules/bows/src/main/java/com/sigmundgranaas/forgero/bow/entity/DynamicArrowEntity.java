package com.sigmundgranaas.forgero.bow.entity;

import static com.sigmundgranaas.forgero.bow.ForgeroBowInitializer.DYNAMIC_ARROW_ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.feature.onhit.block.OnHitBlockFeatureExecutor;
import com.sigmundgranaas.forgero.minecraft.common.feature.onhit.entity.OnHitEntityFeatureExecutor;
import com.sigmundgranaas.forgero.minecraft.common.feature.tick.EntityTickFeatureExecutor;
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
	private final MatchContext tickContext;

	static {
		STACK = DataTracker.registerData(DynamicArrowEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
	}

	public DynamicArrowEntity(EntityType<DynamicArrowEntity> entityType, World world) {
		super(entityType, world);
		this.tickContext = MatchContext.of(new MatchContext.KeyValuePair(ENTITY, this), new MatchContext.KeyValuePair(WORLD, this.getWorld()));
	}

	public DynamicArrowEntity(World world, LivingEntity owner, ItemStack stack) {
		super(DYNAMIC_ARROW_ENTITY, owner, world);
		setStack(stack.copy());
		StateService.INSTANCE.convert(stack)
				.map(state -> ComputedAttribute.apply(state, AttackDamage.KEY))
				.ifPresent(this::setDamage);
		this.tickContext = MatchContext.of(new MatchContext.KeyValuePair(ENTITY, this), new MatchContext.KeyValuePair(WORLD, this.getWorld()));
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
			EntityTickFeatureExecutor.initFromStack(this.getStack(), this).execute(tickContext);

			if (!this.noClip && !this.inGround) {
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

	public boolean isFireImmune() {
		return this.getStack().getItem().isFireproof() || super.isFireImmune();
	}

	protected void initDataTracker() {
		super.initDataTracker();
		this.getDataTracker().startTracking(STACK, ItemStack.EMPTY);
	}



	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);

		if (hitResult instanceof BlockHitResult blockHitResult && getWorld().getBlockState(blockHitResult.getBlockPos()).getBlock() != Blocks.AIR) {
			BlockPos pos = blockHitResult.getBlockPos();
			MatchContext ctx;
			if(this.getOwner() == null){
				ctx = MatchContext.of(new MatchContext.KeyValuePair(ENTITY, this), new MatchContext.KeyValuePair(WORLD, this.getWorld()), new MatchContext.KeyValuePair(BLOCK_TARGET, pos));
			} else {
				ctx = MatchContext.of(new MatchContext.KeyValuePair(ENTITY, this.getOwner()), new MatchContext.KeyValuePair(WORLD, this.getWorld()), new MatchContext.KeyValuePair(BLOCK_TARGET, pos));
			}
			OnHitBlockFeatureExecutor.initFromStack(this.getStack(), this, pos).executeIfNotCoolingDown(ctx);

		} else if (hitResult instanceof EntityHitResult entityHitResult) {
			MatchContext ctx;
			if(this.getOwner() == null){
				ctx = MatchContext.of(new MatchContext.KeyValuePair(ENTITY, this), new MatchContext.KeyValuePair(WORLD, this.getWorld()), new MatchContext.KeyValuePair(ENTITY_TARGET, entityHitResult.getEntity()));
			} else {
				ctx = MatchContext.of(new MatchContext.KeyValuePair(ENTITY, this.getOwner()), new MatchContext.KeyValuePair(WORLD, this.getWorld()), new MatchContext.KeyValuePair(ENTITY_TARGET, entityHitResult.getEntity()));
			}

			OnHitEntityFeatureExecutor.initFromStack(this.getStack(), this, entityHitResult.getEntity()).executeIfNotCoolingDown(ctx);
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
