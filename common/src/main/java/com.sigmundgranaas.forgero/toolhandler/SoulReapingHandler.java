package com.sigmundgranaas.forgero.toolhandler;

import com.sigmundgranaas.forgero.core.property.v2.RunnableHandler;
import com.sigmundgranaas.forgero.core.property.v2.cache.ContainerTargetPair;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.FeatureContainerKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import com.sigmundgranaas.forgero.core.registry.SoulLevelPropertyRegistry;
import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.core.soul.SoulBindable;
import com.sigmundgranaas.forgero.core.soul.SoulSource;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.entity.SoulEntity;
import com.sigmundgranaas.forgero.service.StateService;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import static com.sigmundgranaas.forgero.toolhandler.EntityStatuses.ENTITY_STATUS_TOTEM;

public class SoulReapingHandler implements RunnableHandler {

	private final PlayerEntity entity;

	private final LivingEntity targetEntity;

	private final StateService service;

	public SoulReapingHandler(PlayerEntity entity, LivingEntity targetEntity, StateService service) {
		this.entity = entity;
		this.targetEntity = targetEntity;
		this.service = service;
	}

	public static SoulReapingHandler of(PlayerEntity entity, LivingEntity targetEntity) {
		return new SoulReapingHandler(entity, targetEntity, StateService.INSTANCE);
	}

	@Override
	public String type() {
		return "SOUL_REAPING_HANDLER";
	}

	@Override
	public void run() {
		ItemStack stack = entity.getMainHandStack();
		var converted = service.convert(stack);
		if (converted.isPresent() && converted.get() instanceof Composite construct) {
			String name = targetEntity.hasCustomName() && targetEntity.getCustomName() != null ? targetEntity.getCustomName().getString() : targetEntity.getName().getString();
			SoulSource soulSource = new SoulSource(EntityType.getId(targetEntity.getType()).toString(), name);
			Soul soul = new Soul(soulSource, SoulLevelPropertyRegistry.handler());
			if (FeatureCache.check(FeatureContainerKey.of(construct, Feature.key("forgero:soul_reaping")))) {
				// SoulEntity soulEntity = new SoulEntity(targetEntity.getWorld(), soul);
				// soulEntity.setPosition(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
				// entity.getWorld().spawnEntity(soulEntity);
				// soulEntity.getWorld().sendEntityStatus(soulEntity, EntityStatuses.PLAY_SPAWN_EFFECTS);
			} else if (FeatureCache.check(new FeatureContainerKey(ContainerTargetPair.of(construct), Feature.key("forgero:soul_binding")))) {
				State state = construct;
				if (construct instanceof SoulBindable bindable) {
					state = bindable.bind(soul);
				}
				if (state instanceof Composite comp) {
					if (comp.has("forgero:soul-totem").isPresent()) {
						entity.getWorld().sendEntityStatus(entity, ENTITY_STATUS_TOTEM);
						state = comp.removeUpgrade("forgero:soul-totem");
					}
				}
				entity.getInventory().setStack(entity.getInventory().selectedSlot, service.convert(state).orElse(ItemStack.EMPTY));
			} else if (hasSoulTotemInHand()) {
				entity.getWorld().sendEntityStatus(entity, ENTITY_STATUS_TOTEM);
				ItemStack totemSack = getTotemStack();
				totemSack.decrement(1);
				State state = construct;
				if (construct instanceof SoulBindable bindable) {
					state = bindable.bind(soul);
				}
				entity.getInventory().setStack(entity.getInventory().selectedSlot, service.convert(state).orElse(ItemStack.EMPTY));
			}
		}
	}

	private boolean hasSoulTotemInHand() {
		for (Hand hand : Hand.values()) {
			ItemStack handStack = entity.getStackInHand(hand);
			Item totem = Registries.ITEM.get(new Identifier("forgero:soul-totem"));
			if (totem != Items.AIR && handStack.isOf(totem)) {
				return true;
			}
		}
		return false;
	}

	private ItemStack getTotemStack() {
		for (Hand hand : Hand.values()) {
			ItemStack handStack = entity.getStackInHand(hand);
			Item totem = Registries.ITEM.get(new Identifier("forgero:soul-totem"));
			if (totem != Items.AIR && handStack.isOf(totem)) {
				return handStack;
			}
		}
		return ItemStack.EMPTY;
	}
}
