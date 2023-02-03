package com.sigmundgranaas.forgero.creatures.stonegolem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class StoneGolemEntity extends GolemEntity implements IAnimatable, Angerable {

    public static final Identifier STONE_GOLEM_ID = new Identifier("forgero:stone_golem");
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private final TagKey<Block> eatableOre = TagKey.of(Registry.BLOCK_KEY, new Identifier("forgero:vein_mining_ores"));
    private final Predicate<BlockState> isEatableOre = (BlockState state) -> state.isIn(eatableOre);
    private int eatingTicks;

    private int lastEatingTicks;
    private int angerTime;
    @Nullable
    private UUID angryAt;
    private int attackTicksLeft;

    public StoneGolemEntity(EntityType<? extends GolemEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createStoneGolemEntityAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 15.0);
    }

    public static boolean isValidSpawn(EntityType<? extends GolemEntity> type, ServerWorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return pos.getY() <= world.getSeaLevel() && HostileEntity.isSpawnDark(world, pos, random);
    }

    public void handleStatus(byte status) {
        if (status == EntityStatuses.PLAY_ATTACK_SOUND) {
            this.attackTicksLeft = 10;
            this.playSound(SoundEvents.ENTITY_GOAT_SCREAMING_RAM_IMPACT, 1.0f, 1.0f);
        } else if (status == 112) {
            this.eatingTicks = 600;
        } else if (status == 111) {
            this.eatingTicks = 0;
        } else {
            super.handleStatus(status);
        }
    }


    protected void initGoals() {
        this.targetSelector.add(1, new RevengeGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, MobEntity.class, 5, false, false, entity -> entity instanceof Monster && !(entity instanceof CreeperEntity)));
        this.targetSelector.add(4, new UniversalAngerGoal<>(this, false));
        this.goalSelector.add(3, new EatOre());
        this.goalSelector.add(4, new TemptGoal(this, 1.1, Ingredient.ofItems(Items.IRON_ORE), false));
        this.goalSelector.add(5, new NavigateToEatableOre());
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(8, new LookAroundGoal(this));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        var velocity = this.getVelocity();
        if (eatingTicks > 0) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stone_golem.hammering", ILoopType.EDefaultLoopTypes.LOOP));
        } else if (velocity.x != 0f || velocity.z != 0f) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stone_golem.walk", ILoopType.EDefaultLoopTypes.LOOP));
        } else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.stone_golem.still", ILoopType.EDefaultLoopTypes.LOOP));
        }

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "main_controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public void tickMovement() {
        int k;
        int j;
        int i;
        BlockState blockState;
        super.tickMovement();
        if (this.eatingTicks > 0) {
            --this.eatingTicks;
        }

        if (this.eatingTicks > this.lastEatingTicks + 80) {
            this.lastEatingTicks = this.eatingTicks;
            StoneGolemEntity.this.playSound(SoundEvents.BLOCK_STONE_BREAK, 1.0F, 1.0F);
        }

        if (this.attackTicksLeft > 0) {
            --this.attackTicksLeft;
        }
        if (this.getVelocity().horizontalLengthSquared() > 2.500000277905201E-7 && this.random.nextInt(5) == 0 && !(blockState = this.world.getBlockState(new BlockPos(i = MathHelper.floor(this.getX()), j = MathHelper.floor(this.getY() - (double) 0.2f), k = MathHelper.floor(this.getZ())))).isAir()) {
            this.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState), this.getX() + ((double) this.random.nextFloat() - 0.5) * (double) this.getWidth(), this.getY() + 0.1, this.getZ() + ((double) this.random.nextFloat() - 0.5) * (double) this.getWidth(), 4.0 * ((double) this.random.nextFloat() - 0.5), 0.5, ((double) this.random.nextFloat() - 0.5) * 4.0);
        }
        if (!this.world.isClient) {
            this.tickAngerLogic((ServerWorld) this.world, true);
        }
    }


    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        if (hitPos.y > 0.85 && 1.0 > hitPos.y) {
            world.spawnEntity(new ItemEntity(world, getX() + hitPos.getX(), getY() + hitPos.getY(), getZ() + hitPos.getZ(), new ItemStack(Items.DIAMOND)));
        }
        return super.interactAt(player, hitPos, hand);
    }

    public int getAttackTicksLeft() {
        return this.attackTicksLeft;
    }

    @Override
    public int getAngerTime() {
        return this.angerTime;
    }

    @Override
    public void setAngerTime(int angerTime) {
        this.angerTime = angerTime;
    }

    @Nullable
    @Override
    public UUID getAngryAt() {
        return angryAt;
    }

    @Override
    public void setAngryAt(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
    }

    private float getAttackDamage() {
        return (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    }

    @Override
    public boolean tryAttack(Entity target) {
        this.attackTicksLeft = 10;

        this.world.sendEntityStatus(this, EntityStatuses.PLAY_ATTACK_SOUND);
        float f = this.getAttackDamage();
        float g = (int) f > 0 ? f / 2.0f + (float) this.random.nextInt((int) f) : f;
        boolean bl = target.damage(DamageSource.mob(this), g);
        if (bl) {
            double d;
            if (target instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) target;
                d = livingEntity.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
            } else {
                d = 0.0;
            }
            double d2 = d;
            double e = Math.max(0.0, 1.0 - d2);
            target.setVelocity(target.getVelocity().add(0.0, (double) 0.4f * e, 0.0));
            this.applyDamageEffects(this, target);
        }
        this.playSound(SoundEvents.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        return bl;
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_GOAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_GOAT_DEATH;
    }

    private Optional<BlockPos> findState(Predicate<BlockState> predicate, double searchDistance) {
        BlockPos blockPos = getBlockPos();
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int i = 0; (double) i <= searchDistance; i = i > 0 ? -i : 1 - i) {
            for (int j = 0; (double) j < searchDistance; ++j) {
                for (int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
                    for (int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l) {
                        mutable.set(blockPos, k, i - 1, l);
                        if (blockPos.isWithinDistance(mutable, searchDistance) && predicate.test(world.getBlockState(mutable))) {
                            var directions = Direction.values();
                            ArrayUtils.reverse(Direction.values());
                            for (Direction dir : directions) {
                                if (world.getBlockState(mutable.offset(dir)).isAir()) {
                                    return Optional.of(mutable);
                                }
                            }
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    private class NavigateToEatableOre extends Goal {
        private BlockPos orePos = null;
        private int ticks = 0;

        public void start() {
            this.ticks = 0;
        }

        public void stop() {
            this.ticks = 0;
            this.orePos = null;
            StoneGolemEntity.this.navigation.stop();
            super.stop();
        }

        public void tick() {
            ++this.ticks;
            if (this.ticks > 600 || orePos == null) {
                stop();
            } else {
                Vec3d vec3d = Vec3d.ofBottomCenter(orePos).add(0.0, 2.6, 0.0);
                if (vec3d.distanceTo(StoneGolemEntity.this.getPos()) < 2) {
                    stop();
                } else {
                    StoneGolemEntity.this.getLookControl().lookAt(orePos.getX(), orePos.getY(), orePos.getZ());

                }
            }
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public boolean canStart() {
            Optional<BlockPos> optional = StoneGolemEntity.this.findState(isEatableOre, 10);
            if (optional.isPresent()) {
                orePos = optional.get();
                StoneGolemEntity.this.navigation.startMovingTo((double) orePos.getX() + 0.5, (double) orePos.getY() + 0.5, (double) orePos.getZ() + 0.5, 1);
                return true;
            } else {
                return false;
            }
        }
    }

    private class EatOre extends Goal {
        @Nullable
        private BlockPos orePos = null;
        private int eatingTicks = 0;
        private int lastEatingTicks = 0;

        public void start() {
            if (orePos != null) {
                StoneGolemEntity.this.world.sendEntityStatus(StoneGolemEntity.this, (byte) 112);
                StoneGolemEntity.this.getLookControl().lookAt(orePos.getX(), orePos.getY(), orePos.getZ());
                this.eatingTicks = 0;
            }
        }

        public void stop() {
            super.stop();
            StoneGolemEntity.this.world.sendEntityStatus(StoneGolemEntity.this, (byte) 111);
            this.orePos = null;
            StoneGolemEntity.this.eatingTicks = 0;
            this.lastEatingTicks = -20;
            this.eatingTicks = 0;
        }

        public void tick() {
            ++this.eatingTicks;
            if (this.orePos == null) {
                stop();
                return;
            }
            if (this.eatingTicks > 600) {
                finishEating();
            }
            if (this.orePos != null) {
                StoneGolemEntity.this.getLookControl().lookAt(orePos.getX(), orePos.getY(), orePos.getZ());
            }
            if (eatingTicks > this.lastEatingTicks + 80) {
                this.lastEatingTicks = eatingTicks;
                StoneGolemEntity.this.playSound(SoundEvents.BLOCK_STONE_BREAK, 1.0F, 1.0F);
            }
        }

        private void finishEating() {
            StoneGolemEntity.this.world.breakBlock(orePos, false, StoneGolemEntity.this, 1);
            stop();
        }

        @Override
        public boolean shouldContinue() {
            if (orePos != null) {
                if (StoneGolemEntity.this.getX() - orePos.getX() > 2.5 || StoneGolemEntity.this.getZ() - orePos.getZ() > 2.5) {
                    return false;
                }
                return !StoneGolemEntity.this.world.getBlockState(orePos).isAir();
            }
            return false;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }


        @Override
        public boolean canStart() {
            Optional<BlockPos> optional = StoneGolemEntity.this.findState(isEatableOre, 2);
            if (optional.isPresent()) {
                orePos = optional.get();
                return true;
            } else {
                return false;
            }
        }
    }
}
