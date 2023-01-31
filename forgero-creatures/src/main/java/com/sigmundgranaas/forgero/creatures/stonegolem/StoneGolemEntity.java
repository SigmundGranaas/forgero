package com.sigmundgranaas.forgero.creatures.stonegolem;

import com.sigmundgranaas.forgero.creatures.kuruk.EatBarkGoal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
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
    private EatBarkGoal eatLeavesGoal;

    private int eatLeavesTimer;
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
        } else if (status == 10) {
            this.eatLeavesTimer = 40;
        } else if (status == 112) {
            this.playSound(SoundEvents.ITEM_AXE_STRIP, 1.0F, 1.0F);
        } else {
            super.handleStatus(status);
        }
    }


    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this));
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.0, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, MobEntity.class, 5, false, false, entity -> entity instanceof Monster && !(entity instanceof CreeperEntity)));
        this.targetSelector.add(4, new UniversalAngerGoal<>(this, false));
        this.goalSelector.add(4, new TemptGoal(this, 1.1, Ingredient.ofItems(Items.WHEAT), false));
        this.goalSelector.add(5, new EatOreGoal());
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(8, new LookAroundGoal(this));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {

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
        if (this.world.isClient) {
            this.eatLeavesTimer = Math.max(0, this.eatLeavesTimer - 1);
        }

        int k;
        int j;
        int i;
        BlockState blockState;
        super.tickMovement();
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

    private class EatOreGoal extends Goal {
        private BlockPos orePos;
        private int eatingTicks;
        private int lastEatingTicks;
        private boolean running;
        private int ticks;

        @Nullable
        private Vec3d nextTarget;

        private Optional<BlockPos> findState(Predicate<BlockState> predicate, double searchDistance) {
            BlockPos blockPos = getBlockPos();
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for (int i = 0; (double) i <= searchDistance; i = i > 0 ? -i : 1 - i) {
                for (int j = 0; (double) j < searchDistance; ++j) {
                    for (int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
                        for (int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l) {
                            mutable.set(blockPos, k, i - 1, l);
                            if (blockPos.isWithinDistance(mutable, searchDistance) && predicate.test(world.getBlockState(mutable))) {
                                return Optional.of(mutable);
                            }
                        }
                    }
                }
            }

            return Optional.empty();
        }

        public void start() {
            this.eatingTicks = 0;
            this.ticks = 0;
            this.running = true;
        }

        void cancel() {
            this.running = false;
        }

        public void stop() {
            this.running = false;
            StoneGolemEntity.this.navigation.stop();
        }

        public void tick() {
            ++this.ticks;
            if (this.ticks > 600) {
                orePos = null;
            } else {
                Vec3d vec3d = Vec3d.ofBottomCenter(orePos).add(0.0, 0.6000000238418579, 0.0);
                if (vec3d.distanceTo(StoneGolemEntity.this.getPos()) > 1.0) {
                    this.nextTarget = vec3d;
                    this.moveToNextTarget();
                } else {
                    if (this.nextTarget == null) {
                        this.nextTarget = vec3d;
                    }

                    boolean bl = StoneGolemEntity.this.getPos().distanceTo(this.nextTarget) <= 0.1;
                    boolean bl2 = true;
                    if (!bl && this.ticks > 600) {
                        orePos = null;
                    } else {
                        if (bl) {
                            boolean bl3 = StoneGolemEntity.this.random.nextInt(25) == 0;
                            if (bl3) {
                                this.nextTarget = new Vec3d(vec3d.getX() + (double) this.getRandomOffset(), vec3d.getY(), vec3d.getZ() + (double) this.getRandomOffset());
                                StoneGolemEntity.this.navigation.stop();
                            } else {
                                bl2 = false;
                            }

                            StoneGolemEntity.this.getLookControl().lookAt(vec3d.getX(), vec3d.getY(), vec3d.getZ());
                        }

                        if (bl2) {
                            this.moveToNextTarget();
                        }

                        ++this.eatingTicks;
                        if (StoneGolemEntity.this.random.nextFloat() < 0.05F && this.eatingTicks > this.lastEatingTicks + 60) {
                            this.lastEatingTicks = this.eatingTicks;
                            StoneGolemEntity.this.playSound(SoundEvents.ENTITY_IRON_GOLEM_DAMAGE, 1.0F, 1.0F);
                        }

                    }
                }
            }
        }

        private float getRandomOffset() {
            return (StoneGolemEntity.this.random.nextFloat() * 2.0F - 1.0F) * 0.33333334F;
        }

        private void moveToNextTarget() {
            StoneGolemEntity.this.getMoveControl().moveTo(this.nextTarget.getX(), this.nextTarget.getY(), this.nextTarget.getZ(), 0.25);
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public boolean canStart() {
            Optional<BlockPos> optional = this.findState((BlockState state) -> state.isIn(TagKey.of(Registry.BLOCK_KEY, new Identifier("forgero:vein_mining_ores"))), 10);
            if (optional.isPresent()) {
                orePos = optional.get();
                StoneGolemEntity.this.navigation.startMovingTo((double) orePos.getX() + 0.5, (double) orePos.getY() + 0.5, (double) orePos.getZ() + 0.5, 0.25);
                return true;
            } else {
                return false;
            }
        }
    }
}
