package com.sigmundgranaas.forgero.minecraft.common.entity;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.SoulEncoder;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.ai.AboveGroundTargeting;
import net.minecraft.entity.ai.NoPenaltySolidTargeting;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

import static com.sigmundgranaas.forgero.minecraft.common.entity.Entities.SOUL_ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.item.Items.BOTTLED_SOUL;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.SOUL_IDENTIFIER;
import static net.minecraft.item.Items.GLASS_BOTTLE;

public class SoulEntity extends AnimalEntity implements Flutterer {
	public static final Identifier SOUL_ENTITIY_IDENTIFIER = new Identifier(Forgero.NAMESPACE, "soul_entity");
	private final Soul soul;

	public SoulEntity(World world, Soul soul, double x, double y, double z) {
		super(SOUL_ENTITY, world);
		this.setPosition(x, y, z);
		this.soul = soul;
		this.moveControl = new FlightMoveControl(this, 5, true);
	}

	public SoulEntity(World world, Soul soul) {
		super(SOUL_ENTITY, world);
		this.soul = soul;
		this.moveControl = new FlightMoveControl(this, 5, true);
	}

	public SoulEntity(EntityType<? extends SoulEntity> type, World world) {
		super(type, world);
		this.soul = new Soul();
		this.moveControl = new FlightMoveControl(this, 5, true);
	}

	public static DefaultAttributeContainer.Builder createSoulEntities() {
		return BeeEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 4);
	}

	protected EntityNavigation createNavigation(World world) {
		BirdNavigation birdNavigation = new BirdNavigation(this, world) {
			public boolean isValidPosition(BlockPos pos) {
				return !this.world.getBlockState(pos.down()).isAir();
			}

		};
		birdNavigation.setCanPathThroughDoors(false);
		birdNavigation.setCanSwim(false);
		birdNavigation.setCanEnterOpenDoors(true);
		return birdNavigation;
	}


	public Soul getSoul() {
		return soul;
	}

	public void playSoulSound() {
		this.playSound(SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_DEATH, getSoundVolume(), getSoundPitch());
	}

	public void tick() {
		if (random.nextDouble() > 0.94) {
			createSoulParticles(2);
		}
		super.tick();
	}

	@Override
	public void playSpawnEffects() {
		playSoulSound();
		createSoulParticles(30);
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_DEATH;
	}

	public void createSoulParticles(int amount) {
		for (int i = 0; i < amount; i++) {
			this.getWorld().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX() + this.random.nextGaussian() / 3, this.getY() + this.random.nextGaussian() / 3, this.getZ() + this.random.nextGaussian() / 3, 0, 0, 0);
		}
	}

	protected void initGoals() {
		this.goalSelector.add(1, new SoulWanderAroundGoal());
	}

	public float getPathfindingFavor(BlockPos pos, WorldView world) {
		return world.getBlockState(pos).isAir() ? 10.0F : 0.0F;
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);
		if (itemStack.isOf(GLASS_BOTTLE)) {
			createSoulParticles(30);
			playSoulSound();
			if (this.getWorld().isClient) {
				return ActionResult.CONSUME;
			}
			if (!player.getAbilities().creativeMode) {
				itemStack.decrement(1);
			}
			var soulCompound = new NbtCompound();
			soulCompound.put(SOUL_IDENTIFIER, SoulEncoder.ENCODER.encode(getSoul()));
			ItemStack soulStack = new ItemStack(BOTTLED_SOUL);
			soulStack.getOrCreateNbt().put(FORGERO_IDENTIFIER, soulCompound);
			player.giveItemStack(soulStack);
			this.remove(Entity.RemovalReason.DISCARDED);
			return ActionResult.SUCCESS;
		}
		return super.interactMob(player, hand);
	}

	public boolean hasWings() {
		return true;
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	@Override
	protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
	}

	public boolean isInAir() {
		return !this.isOnGround();
	}

	@Nullable
	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
		return null;
	}

	class SoulWanderAroundGoal extends Goal {

		SoulWanderAroundGoal() {
			this.setControls(EnumSet.of(Control.MOVE));
		}

		public boolean canStart() {
			return SoulEntity.this.navigation.isIdle();
		}

		public boolean shouldContinue() {
			return SoulEntity.this.navigation.isFollowingPath();
		}

		public void start() {
			Vec3d vec3d = this.getRandomLocation();
			if (vec3d != null) {
				SoulEntity.this.navigation.startMovingAlong(SoulEntity.this.navigation.findPathTo(new BlockPos((int) vec3d.x, (int) vec3d.y, (int) vec3d.z), 1), 1);
			}
		}

		@Nullable
		private Vec3d getRandomLocation() {
			Vec3d vec3d2 = SoulEntity.this.getRotationVec(0.0F);
			Vec3d vec3d3 = AboveGroundTargeting.find(SoulEntity.this, 8, 7, vec3d2.x, vec3d2.z, 1.5707964F, 5, 1);
			return vec3d3 != null ? vec3d3 : NoPenaltySolidTargeting.find(SoulEntity.this, 8, 4, -2, vec3d2.x, vec3d2.z, 1.5707963705062866);
		}
	}
}
