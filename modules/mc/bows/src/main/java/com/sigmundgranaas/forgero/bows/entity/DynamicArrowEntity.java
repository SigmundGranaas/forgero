package com.sigmundgranaas.forgero.bows.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.loader.api.ForgeroInitializedCallback;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitManager;
import com.sigmundgranaas.forgero.properties.minecraft.onhitblock.OnHitBlockManager;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Custom arrow entity that integrates with Forgero's property system.
 *
 * <p>Features:
 * <ul>
 *   <li>ItemStack tracking for custom arrow components</li>
 *   <li>Weight-based physics with logarithmic gravity</li>
 *   <li>OnHit effects on entity and block collision</li>
 *   <li>Attack damage from component attributes</li>
 *   <li>NBT persistence for dimension transitions</li>
 *   <li>Custom rendering support</li>
 * </ul>
 */
public class DynamicArrowEntity extends PersistentProjectileEntity {
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicArrowEntity.class);
	private static final TrackedData<ItemStack> STACK;

	// Attribute identifiers
	private static final OpenIdentifier ATTACK_DAMAGE_ATTR = new OpenIdentifier("forgero", "attack_damage");
	private static final OpenIdentifier WEIGHT_ATTR = new OpenIdentifier("forgero", "weight");

	// Services initialized via callback
	private static ComponentConverter converter;
	private static Resolver resolver;

	// Track initialization to prevent premature discarding
	private boolean initialized = false;
	// Cache the stack locally to avoid DataTracker sync issues
	private ItemStack cachedStack = ItemStack.EMPTY;

	static {
		STACK = DataTracker.registerData(DynamicArrowEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

		// Initialize services when Forgero is ready
		ForgeroInitializedCallback.EVENT.register(services -> {
			converter = services.converter();
			resolver = services.resolver();
		});
	}

	/**
	 * Constructor for entity spawning (used by registry).
	 * Sets initialized to false to prevent premature discarding during NBT load.
	 */
	public DynamicArrowEntity(EntityType<DynamicArrowEntity> entityType, World world) {
		super(entityType, world);
		// Don't set initialized here - will be set to true after NBT is loaded
	}

	/**
	 * Constructor for direct creation with owner and arrow stack.
	 * Resolves attack damage from the arrow's component attributes.
	 *
	 * @param world The world
	 * @param owner The entity that shot the arrow
	 * @param stack The arrow ItemStack
	 */
	public DynamicArrowEntity(World world, LivingEntity owner, ItemStack stack) {
		super(DynamicArrowEntityRegistry.DYNAMIC_ARROW_ENTITY, owner, world);
		setStack(stack.copy());
		this.initialized = true;

		// Resolve attack damage from component
		// If converter is null (callback hasn't fired yet), try to get it directly
		ComponentConverter activeConverter = converter;
		Resolver activeResolver = resolver;

		if (activeConverter == null || activeResolver == null) {
			var services = ForgeroInitializedCallback.getServices().orElse(null);
			if (services != null) {
				activeConverter = services.converter();
				activeResolver = services.resolver();
			}
		}

		if (activeConverter != null && activeResolver != null) {
			final ComponentConverter finalConverter = activeConverter;
			final Resolver finalResolver = activeResolver;

			finalConverter.toComponent(stack).ifPresent(component -> {
				AttributeQueryResult result = finalResolver.resolve(component, new AttributeEngine());
				float damage = result.getValue(ATTACK_DAMAGE_ATTR);
				if (damage > 0) {
					setDamage(damage);
				}
			});
		}

		// Set owner explicitly at the end of initialization
		// This ensures the entity is fully initialized before setting the owner
		if (owner != null) {
			this.setOwner(owner);
			LOGGER.debug("DynamicArrowEntity fully initialized with owner: {}, UUID: {}",
				owner.getName().getString(), owner.getUuid());
		}
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		this.getDataTracker().startTracking(STACK, ItemStack.EMPTY);
	}

	@Override
	public void tick() {
		if (this.initialized && this.getStack().isEmpty()) {
			LOGGER.debug("Discarding DynamicArrowEntity at tick {}: stack empty", this.age);
			this.discard();
			return;
		}

		super.tick();

		// Apply weight-based gravity while in flight
		if (!this.noClip && !this.inGround) {
			Vec3d velocity = this.getVelocity();
			this.setVelocity(velocity.x, velocity.y - getGravity(), velocity.z);
		}
	}

	/**
	 * Calculates gravity based on arrow weight using logarithmic scaling.
	 *
	 * <p>Formula: weight * 0.01 * (log10(weight) - log10(2))
	 * <ul>
	 *   <li>At weight=2: gravity = 0 (baseline)</li>
	 *   <li>At weight=4: gravity ≈ 0.012</li>
	 *   <li>At weight=10: gravity ≈ 0.070</li>
	 * </ul>
	 *
	 * @return The gravity acceleration to apply
	 */
	private double getGravity() {
		if (getStack() == null) {
			return 0f;
		}

		// Get converter/resolver, using fallback if static fields are null
		ComponentConverter activeConverter = converter;
		Resolver activeResolver = resolver;

		if (activeConverter == null || activeResolver == null) {
			var services = ForgeroInitializedCallback.getServices().orElse(null);
			if (services != null) {
				activeConverter = services.converter();
				activeResolver = services.resolver();
			}
		}

		if (activeConverter == null || activeResolver == null) {
			return 0f; // No services available, use default vanilla gravity
		}

		final ComponentConverter finalConverter = activeConverter;
		final Resolver finalResolver = activeResolver;

		float weight = finalConverter.toComponent(getStack())
				.map(component -> {
					AttributeQueryResult result = finalResolver.resolve(component, new AttributeEngine());
					return result.getValue(WEIGHT_ATTR);
				})
				.orElse(2f); // Default weight

		if (weight >= 1f) {
			double logModifier = Math.log10(weight) - Math.log10(2f);
			return weight * 0.01 * logModifier;
		}

		return 0f;
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);

		// Handle block collision
		if (hitResult instanceof BlockHitResult blockHitResult) {
			BlockPos pos = blockHitResult.getBlockPos();
			if (getWorld().getBlockState(pos).getBlock() == Blocks.AIR) {
				return;
			}

			// Use owner if available, otherwise use arrow entity
			var source = getOwner() != null ? getOwner() : this;
			OnHitBlockManager.handleOnHitBlock(getStack(), getWorld(), source, pos);
		}
		// Handle entity collision
		else if (hitResult instanceof EntityHitResult entityHitResult) {
			var target = entityHitResult.getEntity();
			var source = getOwner() != null ? getOwner() : this;
			OnHitManager.handleOnHit(getStack(), source, target);
		}
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		if (!this.getStack().isEmpty()) {
			nbt.put("Item", this.getStack().writeNbt(new NbtCompound()));
		}
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		NbtCompound nbtCompound = nbt.getCompound("Item");
		this.setStack(ItemStack.fromNbt(nbtCompound));
		this.initialized = true;
		if (this.getStack().isEmpty()) {
			this.discard();
		}
	}

	@Override
	protected ItemStack asItemStack() {
		return getStack();
	}

	public ItemStack getStack() {
		// Use cached stack if available, otherwise fall back to DataTracker
		// This avoids issues with DataTracker sync timing
		if (!cachedStack.isEmpty()) {
			return cachedStack;
		}
		return this.getDataTracker().get(STACK);
	}

	public void setStack(ItemStack stack) {
		this.cachedStack = stack;
		this.getDataTracker().set(STACK, stack);
	}

	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		super.onTrackedDataSet(data);
		if (STACK.equals(data)) {
			// Update cached stack when DataTracker syncs
			this.cachedStack = this.getDataTracker().get(STACK);
			this.cachedStack.setHolder(this);
		}
	}

	@Override
	public boolean isFireImmune() {
		return this.getStack().getItem().isFireproof() || super.isFireImmune();
	}
}
