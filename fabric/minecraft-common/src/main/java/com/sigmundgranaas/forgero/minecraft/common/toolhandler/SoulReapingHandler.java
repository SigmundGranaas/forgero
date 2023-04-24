package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import static com.sigmundgranaas.forgero.minecraft.common.toolhandler.EntityStatuses.ENTITY_STATUS_TOTEM;

import com.sigmundgranaas.forgero.core.property.v2.RunnableHandler;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.core.registry.SoulLevelPropertyRegistry;
import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.core.soul.SoulBindable;
import com.sigmundgranaas.forgero.core.soul.SoulSource;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.conversion.CachedConverter;
import com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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
		var converted = CachedConverter.of(stack);
		if (converted.isPresent() && converted.get() instanceof Composite construct) {
			String name = targetEntity.hasCustomName() && targetEntity.getCustomName() != null ? targetEntity.getCustomName().getString() : targetEntity.getName().getString();
			SoulSource soulSource = new SoulSource(EntityType.getId(targetEntity.getType()).toString(), name);
			Soul soul = new Soul(soulSource, SoulLevelPropertyRegistry.handler());
			if (ContainsFeatureCache.check(PropertyTargetCacheKey.of(construct, "SOUL_REAPING"))) {
				SoulEntity soulEntity = new SoulEntity(targetEntity.getWorld(), soul);
				soulEntity.setPosition(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
				entity.world.spawnEntity(soulEntity);
				soulEntity.world.sendEntityStatus(soulEntity, EntityStatuses.PLAY_SPAWN_EFFECTS);
			} else if (ContainsFeatureCache.check(new PropertyTargetCacheKey(ContainerTargetPair.of(construct), "SOUL_BINDING"))) {
				State state = construct;
				if (construct instanceof SoulBindable bindable) {
					state = bindable.bind(soul);
				}
				if (state instanceof Composite comp) {
					if (comp.has("forgero:soul-totem").isPresent()) {
						entity.world.sendEntityStatus(entity, ENTITY_STATUS_TOTEM);
						state = comp.removeUpgrade("forgero:soul-totem");
					}
				}
				entity.getInventory().setStack(entity.getInventory().selectedSlot, CachedConverter.of(state));
			} else if (hasSoulTotemInHand()) {
				entity.world.sendEntityStatus(entity, ENTITY_STATUS_TOTEM);
				ItemStack totemSack = getTotemStack();
				totemSack.decrement(1);
				State state = construct;
				if (construct instanceof SoulBindable bindable) {
					state = bindable.bind(soul);
				}
				entity.getInventory().setStack(entity.getInventory().selectedSlot, CachedConverter.of(state));
			}
		}
	}

	private boolean hasSoulTotemInHand() {
		for (Hand hand : Hand.values()) {
			ItemStack handStack = entity.getStackInHand(hand);
			Item totem = Registry.ITEM.get(new Identifier("forgero:soul-totem"));
			if (totem != Items.AIR && handStack.isOf(totem)) {
				return true;
			}
		}
		return false;
	}

	private ItemStack getTotemStack() {
		for (Hand hand : Hand.values()) {
			ItemStack handStack = entity.getStackInHand(hand);
			Item totem = Registry.ITEM.get(new Identifier("forgero:soul-totem"));
			if (totem != Items.AIR && handStack.isOf(totem)) {
				return handStack;
			}
		}
		return ItemStack.EMPTY;
	}
}
