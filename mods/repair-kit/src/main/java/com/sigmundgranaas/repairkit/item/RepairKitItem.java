package com.sigmundgranaas.repairkit.item;

import com.sigmundgranaas.repairkit.RepairMaterialProviders;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A repair kit item that can be filled with materials and used to repair equipment.
 *
 * NBT Structure:
 * - RepairMaterial: String identifier of the stored material (e.g., "minecraft:iron_ingot")
 * - MaterialCount: Integer count of materials stored (0-64)
 *
 * Usage:
 * - Main hand + materials in offhand: Hold right-click to fill kit
 * - Damaged tool in main hand + filled kit in offhand: Hold right-click to repair
 */
public class RepairKitItem extends Item {
    public static final String NBT_REPAIR_MATERIAL = "RepairMaterial";
    public static final String NBT_MATERIAL_COUNT = "MaterialCount";
    public static final int MAX_CAPACITY = 64;
    public static final int USE_DURATION_TICKS = 40; // 2 seconds

    private final RepairKitTier tier;

    public RepairKitItem(RepairKitTier tier, Settings settings) {
        super(settings.maxCount(1));
        this.tier = tier;
    }

    public RepairKitTier getTier() {
        return tier;
    }

    // ===== NBT Helpers =====

    /**
     * Get the material stored in this kit.
     *
     * @param stack The repair kit item stack
     * @return Optional containing the material identifier, or empty if kit is empty
     */
    public static Optional<Identifier> getStoredMaterial(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(NBT_REPAIR_MATERIAL)) {
            String materialId = nbt.getString(NBT_REPAIR_MATERIAL);
            if (!materialId.isEmpty()) {
                return Optional.of(new Identifier(materialId));
            }
        }
        return Optional.empty();
    }

    /**
     * Get the count of materials stored in this kit.
     *
     * @param stack The repair kit item stack
     * @return The number of materials stored (0-64)
     */
    public static int getMaterialCount(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(NBT_MATERIAL_COUNT)) {
            return nbt.getInt(NBT_MATERIAL_COUNT);
        }
        return 0;
    }

    /**
     * Set the stored material and count.
     *
     * @param stack    The repair kit item stack
     * @param material The material identifier to store
     * @param count    The count of materials (0-64)
     */
    public static void setStoredMaterial(ItemStack stack, Identifier material, int count) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (count > 0 && material != null) {
            nbt.putString(NBT_REPAIR_MATERIAL, material.toString());
            nbt.putInt(NBT_MATERIAL_COUNT, Math.min(count, MAX_CAPACITY));
        } else {
            // Clear if count is 0
            nbt.remove(NBT_REPAIR_MATERIAL);
            nbt.putInt(NBT_MATERIAL_COUNT, 0);
        }
    }

    /**
     * Check if this kit is empty (no materials stored).
     */
    public static boolean isEmpty(ItemStack stack) {
        return getMaterialCount(stack) == 0;
    }

    /**
     * Check if this kit is full (64 materials stored).
     */
    public static boolean isFull(ItemStack stack) {
        return getMaterialCount(stack) >= MAX_CAPACITY;
    }

    /**
     * Add materials to the kit.
     *
     * @param stack    The repair kit item stack
     * @param material The material to add
     * @param amount   The amount to add
     * @return The number of materials actually added (may be less if kit fills up)
     */
    public static int addMaterials(ItemStack stack, Identifier material, int amount) {
        int currentCount = getMaterialCount(stack);
        Optional<Identifier> storedMaterial = getStoredMaterial(stack);

        // Check if material matches or kit is empty
        if (storedMaterial.isPresent() && !storedMaterial.get().equals(material)) {
            return 0; // Can't mix materials
        }

        int spaceAvailable = MAX_CAPACITY - currentCount;
        int toAdd = Math.min(amount, spaceAvailable);

        if (toAdd > 0) {
            setStoredMaterial(stack, material, currentCount + toAdd);
        }

        return toAdd;
    }

    /**
     * Use one material from the kit.
     *
     * @param stack The repair kit item stack
     * @return true if a material was consumed, false if kit was empty
     */
    public static boolean useMaterial(ItemStack stack) {
        int count = getMaterialCount(stack);
        if (count > 0) {
            Optional<Identifier> material = getStoredMaterial(stack);
            setStoredMaterial(stack, material.orElse(null), count - 1);
            return true;
        }
        return false;
    }

    // ===== Item Bar (Durability Display) =====

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return getMaterialCount(stack) > 0;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        int count = getMaterialCount(stack);
        return Math.round(count * 13.0f / MAX_CAPACITY);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        int count = getMaterialCount(stack);
        float ratio = (float) count / MAX_CAPACITY;
        // Green when full, yellow at half, red when nearly empty
        if (ratio > 0.5f) {
            return 0x00FF00; // Green
        } else if (ratio > 0.25f) {
            return 0xFFFF00; // Yellow
        } else {
            return 0xFF0000; // Red
        }
    }

    // ===== Use Action (Fill/Repair) =====

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack kitStack = player.getStackInHand(hand);

        if (hand != Hand.MAIN_HAND) {
            return TypedActionResult.pass(kitStack);
        }

        ItemStack offhandStack = player.getStackInHand(Hand.OFF_HAND);

        // Check if we can fill the kit with materials from offhand
        if (!offhandStack.isEmpty() && canFillWith(kitStack, offhandStack)) {
            player.setCurrentHand(hand);
            return TypedActionResult.consume(kitStack);
        }

        return TypedActionResult.pass(kitStack);
    }

    /**
     * Check if the kit can be filled with the given material stack.
     */
    private boolean canFillWith(ItemStack kitStack, ItemStack materialStack) {
        if (isFull(kitStack)) {
            return false;
        }

        Optional<Identifier> storedMaterial = getStoredMaterial(kitStack);
        Identifier materialId = Registries.ITEM.getId(materialStack.getItem());

        // If kit has materials, must match
        if (storedMaterial.isPresent()) {
            return storedMaterial.get().equals(materialId);
        }

        // Empty kit - any valid repair material works
        return true;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return USE_DURATION_TICKS;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!(user instanceof PlayerEntity player)) {
            return stack;
        }

        if (!world.isClient) {
            ItemStack offhandStack = player.getStackInHand(Hand.OFF_HAND);

            if (!offhandStack.isEmpty()) {
                // Fill kit with materials from offhand
                Identifier materialId = Registries.ITEM.getId(offhandStack.getItem());
                int toAdd = Math.min(offhandStack.getCount(), MAX_CAPACITY - getMaterialCount(stack));
                int added = addMaterials(stack, materialId, toAdd);

                if (added > 0) {
                    offhandStack.decrement(added);
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            }
        }

        return stack;
    }

    // ===== Repair Logic =====

    /**
     * Attempt to repair the given item stack using materials from this kit.
     *
     * @param kitStack  The repair kit
     * @param toolStack The item to repair
     * @param player    The player performing the repair
     * @return true if repair was successful
     */
    public boolean attemptRepair(ItemStack kitStack, ItemStack toolStack, PlayerEntity player) {
        if (isEmpty(kitStack)) {
            return false;
        }

        if (!toolStack.isDamageable() || !toolStack.isDamaged()) {
            return false;
        }

        // Check if material matches the tool's repair ingredient
        Optional<Identifier> storedMaterial = getStoredMaterial(kitStack);
        if (storedMaterial.isEmpty()) {
            return false;
        }

        Optional<Ingredient> repairIngredient = RepairMaterialProviders.getRepairIngredient(toolStack);
        if (repairIngredient.isEmpty()) {
            return false;
        }

        // Check if stored material is valid for this tool
        Item materialItem = Registries.ITEM.get(storedMaterial.get());
        ItemStack materialStack = new ItemStack(materialItem);
        if (!repairIngredient.get().test(materialStack)) {
            return false;
        }

        // Calculate repair amount based on tier
        int repairAmount = tier.calculateRepairAmount(toolStack.getMaxDamage());

        // Apply repair
        int currentDamage = toolStack.getDamage();
        int newDamage = Math.max(0, currentDamage - repairAmount);
        toolStack.setDamage(newDamage);

        // Consume one material from kit
        useMaterial(kitStack);

        return true;
    }

    // ===== Tooltip =====

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // Tier info
        String tierName = tier.getName().substring(0, 1).toUpperCase() + tier.getName().substring(1);
        int repairPercent = (int) (tier.getRepairMultiplier() * 100);
        tooltip.add(Text.literal("Tier: " + tierName + " (" + repairPercent + "% repair)")
                .formatted(Formatting.GRAY));

        // Material info
        Optional<Identifier> material = getStoredMaterial(stack);
        int count = getMaterialCount(stack);

        if (material.isPresent()) {
            Item materialItem = Registries.ITEM.get(material.get());
            String materialName = materialItem.getName().getString();
            tooltip.add(Text.literal("Material: " + materialName)
                    .formatted(Formatting.AQUA));
            tooltip.add(Text.literal("Count: " + count + "/" + MAX_CAPACITY)
                    .formatted(Formatting.GREEN));
        } else {
            tooltip.add(Text.literal("Empty - add materials to fill")
                    .formatted(Formatting.DARK_GRAY));
        }

        // Usage hint
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("Hold in main hand with materials in offhand")
                .formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal("to fill. Use from offhand to repair tools.")
                .formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
    }
}
