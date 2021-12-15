package com.sigmundgranaas.forgero.item.forgerotool.material;

import com.sigmundgranaas.forgero.item.forgerotool.Constants;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ForgeroToolMaterial implements ToolMaterial {
    BIRCH(2, 100, 1.0F, 1.0F, 15, Ingredient.ofItems(Items.BIRCH_PLANKS)),
    OAK(2, 100, 2.0F, 2.0F, 15, Ingredient.ofItems(Items.OAK_PLANKS)),
    SPRUCE(2, 100, 3.0F, 3.0F, 15, Ingredient.ofItems(Items.SPRUCE_PLANKS)),
    EMPTY_MATERIAL(1, 1, 1F, 1F, 1, Ingredient.ofItems(Items.AIR));


    private final int miningLevel;
    private final int itemDurability;
    private final float miningSpeed;
    private final float attackDamage;
    private final int enchantability;
    private final Ingredient repairIngredient;

    ForgeroToolMaterial(int miningLevel, int itemDurability, float miningSpeed, float attackDamage, int enchantability, Ingredient repairIngredient) {
        this.miningLevel = miningLevel;
        this.itemDurability = itemDurability;
        this.miningSpeed = miningSpeed;
        this.attackDamage = attackDamage;
        this.enchantability = enchantability;
        this.repairIngredient = repairIngredient;
    }

    public static List<ToolMaterial> getMaterialList() {
        List<ToolMaterial> materialList = new ArrayList<>();
        materialList.add(OAK);
        materialList.add(BIRCH);
        materialList.add(SPRUCE);
        materialList.add(ToolMaterials.STONE);
        materialList.add(ToolMaterials.IRON);
        materialList.add(ToolMaterials.GOLD);
        materialList.add(ToolMaterials.DIAMOND);
        materialList.add(ToolMaterials.NETHERITE);
        return materialList;
    }

    public static Map<String, ToolMaterial> getMaterialMap() {
        Map<String, ToolMaterial> materialList = new HashMap<>();
        materialList.put(OAK.toString(), OAK);
        materialList.put(BIRCH.toString(), BIRCH);
        materialList.put(SPRUCE.toString(), SPRUCE);
        materialList.put(ToolMaterials.STONE.toString(), ToolMaterials.STONE);
        materialList.put(ToolMaterials.IRON.toString(), ToolMaterials.IRON);
        materialList.put(ToolMaterials.GOLD.toString(), ToolMaterials.GOLD);
        materialList.put(ToolMaterials.DIAMOND.toString(), ToolMaterials.DIAMOND);
        materialList.put(ToolMaterials.NETHERITE.toString(), ToolMaterials.NETHERITE);
        return materialList;
    }

    public static List<Identifier> getMaterialRepresentations(String material) {
        ToolMaterial toolMaterial = getMaterialMap().get(material);
        return switch (toolMaterial.toString()) {
            case Constants.OAK -> List.of(new Identifier("minecraft:textures/item/oak_boat.png"), new Identifier("minecraft:textures/item/wooden_shovel.png"), new Identifier("minecraft:textures/block/oak_planks.png"));
            case Constants.SPRUCE -> List.of(new Identifier("minecraft:textures/item/spruce_boat.png"), new Identifier("minecraft:textures/block/spruce_planks.png"));
            case Constants.BIRCH -> List.of(new Identifier("minecraft:textures/item/birch_boat.png"), new Identifier("minecraft:textures/block/birch_planks.png"));
            case Constants.DIAMOND -> List.of(new Identifier("minecraft:textures/item/diamond_chestplate.png"),
                    new Identifier("minecraft:textures/item/diamond_helmet.png"),
                    new Identifier("minecraft:textures/item/diamond.png"), new Identifier("minecraft:textures/item/diamond_sword.png"), new Identifier("minecraft:textures/item/diamond_pickaxe.png"));
            case Constants.IRON -> List.of(new Identifier("minecraft:textures/item/iron_chestplate.png"),
                    new Identifier("minecraft:textures/item/iron_helmet.png"),
                    new Identifier("minecraft:textures/item/iron_ingot.png"), new Identifier("minecraft:textures/item/iron_pickaxe.png"));
            case Constants.NETHERITE -> List.of(new Identifier("minecraft:textures/item/netherite_chestplate.png"),
                    new Identifier("minecraft:textures/item/netherite_helmet.png"),
                    new Identifier("minecraft:textures/item/netherite_ingot.png"), new Identifier("minecraft:textures/item/netherite_pickaxe.png"));
            case Constants.GOLD -> List.of(new Identifier("minecraft:textures/item/golden_chestplate.png"),
                    new Identifier("minecraft:textures/item/golden_helmet.png"),
                    new Identifier("minecraft:textures/item/gold_ingot.png"), new Identifier("minecraft:textures/item/golden_sword.png"), new Identifier("minecraft:textures/item/golden_pickaxe.png"));
            case Constants.STONE -> List.of(new Identifier("minecraft:textures/block/stone.png"),
                    new Identifier("minecraft:textures/block/cobblestone.png"), new Identifier("minecraft:textures/item/stone_sword.png"), new Identifier("minecraft:textures/item/stone_pickaxe.png"));
            default -> List.of(new Identifier("minecraft:textures/item/oak_boat.png"));
        };

    }

    public static List<Identifier> getMaterialExclusions(String material) {
        ToolMaterial toolMaterial = getMaterialMap().get(material);
        return switch (toolMaterial.toString()) {
            case Constants.OAK -> List.of();
            case Constants.SPRUCE -> List.of(new Identifier("minecraft:textures/item/oak_boat.png"), new Identifier("minecraft:textures/item/birch_boat.png"));
            case Constants.BIRCH -> List.of(new Identifier("minecraft:textures/item/spruce_boat.png"), new Identifier("minecraft:textures/item/oak_boat.png"));
            case Constants.DIAMOND -> List.of(new Identifier("minecraft:textures/item/iron_sword.png"),
                    new Identifier("minecraft:textures/item/stone_sword.png"),
                    new Identifier("minecraft:textures/item/diamond.png"), new Identifier("minecraft:textures/item/iron_pickaxe.png"));
            case Constants.IRON -> List.of(new Identifier("minecraft:textures/item/diamond_sword.png"),
                    new Identifier("minecraft:textures/item/stone_sword.png"),
                    new Identifier("minecraft:textures/item/netherite_sword.png"), new Identifier("minecraft:textures/item/diamond_pickaxe.png"));
            case Constants.NETHERITE -> List.of(new Identifier("minecraft:textures/item/iron_sword.png"),
                    new Identifier("minecraft:textures/item/diamond_sword.png"),
                    new Identifier("minecraft:textures/item/stone_sword.png"), new Identifier("minecraft:textures/item/iron_pickaxe.png"));
            case Constants.GOLD -> List.of(new Identifier("minecraft:textures/item/iron_sword.png"),
                    new Identifier("minecraft:textures/item/diamond_sword.png"),
                    new Identifier("minecraft:textures/item/wooden_sword.png"), new Identifier("minecraft:textures/item/iron_pickaxe.png"));
            case Constants.STONE -> List.of(new Identifier("minecraft:textures/item/iron_sword.png"),
                    new Identifier("minecraft:textures/item/golden_sword.png"),
                    new Identifier("minecraft:textures/item/iron_pickaxe.png"));


            default -> List.of(new Identifier("minecraft:textures/item/oak_boat.png"));
        };

    }

    @Override
    public int getDurability() {
        return this.itemDurability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return this.miningSpeed;
    }

    @Override
    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public int getMiningLevel() {
        return this.miningLevel;
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient;
    }
}
