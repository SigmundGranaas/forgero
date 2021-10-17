package com.sigmundgranaas.forgero.item.forgerotool.tool.item;


import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;

public class ForgeroPickaxeItem extends PickaxeItem implements ForgeroMiningTool {
    private final ForgeroToolPartItem handle;
    private final ForgeroToolPartItem head;

    public ForgeroPickaxeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings, ForgeroToolPartItem head, ForgeroToolPartItem handle) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
        this.head = head;
        this.handle = handle;
    }

    public ForgeroToolPartItem getHandle() {
        return handle;
    }

    public ForgeroToolPartItem getHead() {
        return head;
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
    }

    @Override
    public String getToolTip() {
        return super.getMaterial().toString() + "_" + getToolTypeLowerCaseString();
    }

    @Override
    public Identifier getIdentifier() {
        return new Identifier(Forgero.MOD_NAMESPACE, getToolType().toString().toLowerCase(Locale.ROOT) + "_" + head.getToolPartTypeAndMaterialLowerCase() + "_" + handle.getToolPartTypeAndMaterialLowerCase());
    }

    @Override
    public ForgeroToolTypes getToolType() {
        return ForgeroToolTypes.PICKAXE;
    }

    @Override
    public String getToolTypeLowerCaseString() {
        return ForgeroToolTypes.PICKAXE.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public ForgeroToolPartItem getToolHead() {
        return getHead();
    }

    @Override
    public ForgeroToolPartItem getToolHandle() {
        return getHandle();
    }

}
