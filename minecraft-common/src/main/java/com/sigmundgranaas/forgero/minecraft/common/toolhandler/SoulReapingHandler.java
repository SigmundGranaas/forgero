package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.v2.RunnableHandler;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.core.soul.SoulBindable;
import com.sigmundgranaas.forgero.core.soul.SoulSource;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class SoulReapingHandler implements RunnableHandler {

    private final PlayerEntity entity;

    private final LivingEntity targetEntity;

    public SoulReapingHandler(PlayerEntity entity, LivingEntity targetEntity) {
        this.entity = entity;
        this.targetEntity = targetEntity;
    }

    public static SoulReapingHandler of(PlayerEntity entity, LivingEntity targetEntity) {
        return new SoulReapingHandler(entity, targetEntity);
    }

    @Override
    public String type() {
        return "SOUL_REAPING_HANDLER";
    }

    @Override
    public void run() {
        ItemStack stack = entity.getMainHandStack();
        var converted = StateConverter.of(stack);
        if (converted.isPresent() && converted.get() instanceof Composite construct) {
            SoulSource soulSource = new SoulSource(targetEntity.getType().toString());
            Soul soul = new Soul(soulSource);
            if (ContainsFeatureCache.check(new PropertyTargetCacheKey(ContainerTargetPair.of(construct), "SOUL_REAPING"))) {
                SoulEntity soulEntity = new SoulEntity(targetEntity.getWorld(), soul);
                soulEntity.setPosition(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
                targetEntity.getWorld().spawnEntity(soulEntity);
                soulEntity.playSoulSound();
                soulEntity.createSoulParticles();
            } else if (ContainsFeatureCache.check(new PropertyTargetCacheKey(ContainerTargetPair.of(construct), "SOUL_BINDING"))) {
                State state = construct;
                if (construct instanceof SoulBindable bindable) {
                    state = bindable.bind(soul);
                }
                if (state instanceof Composite comp) {
                    if (comp.has("forgero:soul_totem").isPresent()) {
                        state = comp.removeUpgrade("forgero:soul_totem");
                    }
                }
                entity.getInventory().setStack(entity.getInventory().selectedSlot, StateConverter.of(state));
            }
        }
    }
}
