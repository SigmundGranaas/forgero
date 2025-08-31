package com.sigmundgranaas.forgero.loader.mixin;

import com.sigmundgranaas.forgero.common.attribute.AttributeManager;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(Item.class)
public class ItemMiningMixin {

	/**
	 * Injects into getMiningSpeedMultiplier to provide Forgero's calculated speed.
	 * This method serves a dual purpose: it applies the correct speed bonus AND effectively
	 * acts as the "isSuitableFor" check, since an unsuitable tool will not get a speed bonus.
	 */
	@Inject(method = "getMiningSpeedMultiplier", at = @At("RETURN"), cancellable = true)
	private void forgero$injectMiningSpeed(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> cir) {
		// isEffective now returns an Optional of the attributes if the tool is effective.
		isEffective(stack, state).ifPresent(attributes -> {
			float forgeroSpeed = attributes.getValue(DefaultAttributes.MINING_SPEED);

			// Only override if Forgero's speed is greater. This respects vanilla tools
			// that might have a specific speed for certain blocks (e.g., Swords on Cobwebs).
			if (forgeroSpeed > cir.getReturnValueF()) {
				cir.setReturnValue(forgeroSpeed);
			}
		});
	}

	/**
	 * A comprehensive check to see if a Forgero tool is effective against a block.
	 * It checks mining level and tool type tags against block tags.
	 *
	 * @return An Optional containing the resolved attributes if effective, otherwise empty.
	 */
	private Optional<AttributeQueryResult> isEffective(ItemStack stack, BlockState state) {
		Optional<AttributeQueryResult> attributesOpt = AttributeManager.getResolvedAttributes(stack);
		if (attributesOpt.isEmpty()) {
			return Optional.empty();
		}
		AttributeQueryResult attributes = attributesOpt.get();

		int miningLevel = (int) attributes.getValue(DefaultAttributes.MINING_LEVEL);

		// First, check if the tool's mining level is sufficient for the block.
		if (state.isIn(BlockTags.NEEDS_DIAMOND_TOOL) && miningLevel < 3) return Optional.empty();
		if (state.isIn(BlockTags.NEEDS_IRON_TOOL) && miningLevel < 2) return Optional.empty();
		if (state.isIn(BlockTags.NEEDS_STONE_TOOL) && miningLevel < 1) return Optional.empty();

		Optional<Component> componentOpt = AttributeManager.getComponent(stack);
		if (componentOpt.isEmpty()) {
			return Optional.empty();
		}

		// Second, check if the tool type matches the block's required tool type.
		Set<String> forgeroTags = componentOpt.get().getTags().stream()
				.map(com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier::name)
				.collect(Collectors.toSet());

		boolean isToolTypeCorrect =
				(forgeroTags.contains("pickaxe") && state.isIn(BlockTags.PICKAXE_MINEABLE)) ||
						(forgeroTags.contains("axe") && state.isIn(BlockTags.AXE_MINEABLE)) ||
						(forgeroTags.contains("shovel") && state.isIn(BlockTags.SHOVEL_MINEABLE)) ||
						(forgeroTags.contains("hoe") && state.isIn(BlockTags.HOE_MINEABLE));

		if (isToolTypeCorrect) {
			return Optional.of(attributes);
		}

		return Optional.empty();
	}
}
