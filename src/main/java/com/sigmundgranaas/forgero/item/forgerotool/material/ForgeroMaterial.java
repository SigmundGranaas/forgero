package com.sigmundgranaas.forgero.item.forgerotool.material;

import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;

import java.util.ArrayList;
import java.util.List;

public enum ForgeroMaterial implements ToolMaterial {
    Birch(2, 100, 1.0F, 1.0F, 15, Ingredient.ofItems(Items.BIRCH_PLANKS)),
    Oak(2, 100, 2.0F, 2.0F, 15, Ingredient.ofItems(Items.OAK_PLANKS)),
    Spruce(2, 100, 3.0F, 3.0F, 15, Ingredient.ofItems(Items.SPRUCE_PLANKS));

    private final int miningLevel;
    private final int itemDurability;
    private final float miningSpeed;
    private final float attackDamage;
    private final int enchantability;
    private final Ingredient repairIngredient;

    ForgeroMaterial(int miningLevel, int itemDurability, float miningSpeed, float attackDamage, int enchantability, Ingredient repairIngredient) {
        this.miningLevel = miningLevel;
        this.itemDurability = itemDurability;
        this.miningSpeed = miningSpeed;
        this.attackDamage = attackDamage;
        this.enchantability = enchantability;
        this.repairIngredient = repairIngredient;
    }

    public static List<ToolMaterial> getMaterialList() {
        List<ToolMaterial> materialList = new ArrayList<>();
        materialList.add(Birch);
        materialList.add(Oak);
        materialList.add(Spruce);
        materialList.add(ToolMaterials.NETHERITE);
        materialList.add(ToolMaterials.DIAMOND);
        materialList.add(ToolMaterials.IRON);
        materialList.add(ToolMaterials.GOLD);
        materialList.add(ToolMaterials.STONE);
        return materialList;
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

    ;
}
