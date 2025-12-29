package com.sigmundgranaas.repairkit.mixin;

import com.sigmundgranaas.repairkit.RepairMaterialProviders;
import com.sigmundgranaas.repairkit.item.RepairKitItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Mixin to handle offhand repair kit interaction.
 *
 * When a player right-clicks with a damaged tool in main hand and has a
 * filled repair kit in offhand, this starts the repair action.
 */
@Mixin(Item.class)
public class ItemStackUseMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void repairkit$onUse(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (hand != Hand.MAIN_HAND) {
            return;
        }

        ItemStack mainHandStack = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack offhandStack = player.getStackInHand(Hand.OFF_HAND);

        // Skip if main hand is a repair kit (let it handle its own use)
        if (mainHandStack.getItem() instanceof RepairKitItem) {
            return;
        }

        // Check if main hand has damaged item and offhand has filled repair kit
        if (!mainHandStack.isDamageable() || !mainHandStack.isDamaged()) {
            return;
        }

        if (!(offhandStack.getItem() instanceof RepairKitItem)) {
            return;
        }

        if (RepairKitItem.isEmpty(offhandStack)) {
            return;
        }

        // Check if the kit's material matches the tool's repair ingredient
        if (!canRepairWith(offhandStack, mainHandStack)) {
            return;
        }

        // Start the repair use action
        player.setCurrentHand(Hand.MAIN_HAND);
        cir.setReturnValue(TypedActionResult.consume(mainHandStack));
    }

    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    private void repairkit$onFinishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (!(user instanceof PlayerEntity player)) {
            return;
        }

        ItemStack offhandStack = player.getStackInHand(Hand.OFF_HAND);

        if (!(offhandStack.getItem() instanceof RepairKitItem kitItem)) {
            return;
        }

        if (RepairKitItem.isEmpty(offhandStack)) {
            return;
        }

        if (!stack.isDamageable() || !stack.isDamaged()) {
            return;
        }

        if (!canRepairWith(offhandStack, stack)) {
            return;
        }

        if (!world.isClient) {
            // Perform repair
            if (kitItem.attemptRepair(offhandStack, stack, player)) {
                // Play repair sound
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 0.5f, 1.2f);
            }
        }

        cir.setReturnValue(stack);
    }

    private static boolean canRepairWith(ItemStack kitStack, ItemStack toolStack) {
        Optional<Identifier> storedMaterial = RepairKitItem.getStoredMaterial(kitStack);
        if (storedMaterial.isEmpty()) {
            return false;
        }

        Optional<Ingredient> repairIngredient = RepairMaterialProviders.getRepairIngredient(toolStack);
        if (repairIngredient.isEmpty()) {
            return false;
        }

        Item materialItem = Registries.ITEM.get(storedMaterial.get());
        ItemStack materialStack = new ItemStack(materialItem);
        return repairIngredient.get().test(materialStack);
    }
}
