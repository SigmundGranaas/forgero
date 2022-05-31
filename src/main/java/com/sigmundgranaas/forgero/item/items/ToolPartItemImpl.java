package com.sigmundgranaas.forgero.item.items;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.item.adapter.DescriptionWriter;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolPartAdapter;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class ToolPartItemImpl extends Item implements ToolPartItem {
    private final PrimaryMaterial material;
    private final ForgeroToolPartTypes type;
    private final ForgeroToolPart part;

    public ToolPartItemImpl(Settings settings, PrimaryMaterial material, ForgeroToolPartTypes type, ForgeroToolPart part) {
        super(settings);
        this.material = material;
        this.type = type;
        this.part = part;
    }


    @Override
    public Text getName() {
        MutableText text;
        if (!part.getSchematic().getResourceName().equals("default")) {
            text = Text.translatable(String.format("item.%s.%s", ForgeroInitializer.MOD_NAMESPACE, part.getSchematic().getResourceName())).append(" ");
            text.append(Text.translatable(String.format("item.%s.%s", ForgeroInitializer.MOD_NAMESPACE, material.getResourceName().toLowerCase(Locale.ROOT))))
                    .append(" ");
        } else {
            text = Text.translatable(String.format("item.%s.%s", ForgeroInitializer.MOD_NAMESPACE, material.getResourceName().toLowerCase(Locale.ROOT))).append(" ");
        }
        if (getToolPartType() == ForgeroToolPartTypes.HEAD) {
            String headType = switch (((ToolPartHead) part).getToolType()) {
                case AXE -> "axehead";
                case PICKAXE -> "pickaxehead";
                case SHOVEL -> "shovelhead";
                case SWORD -> "swordhead";
                case HOE -> "hoehead";
            };
            text.append(Text.translatable(String.format("item.%s.%s", ForgeroInitializer.MOD_NAMESPACE, headType))).append(" ");
        } else {
            text.append(Text.translatable(String.format("item.%s.%s", ForgeroInitializer.MOD_NAMESPACE, part.getToolPartType().getName())));
        }
        return text;
    }

    @Override
    public Text getName(ItemStack stack) {
        return getName();
    }

    public ForgeroToolPartTypes getToolPartType() {
        return type;
    }

    public String getToolPartIdentifierString() {
        return part.getToolPartIdentifier();
    }

    @Override
    public Identifier getIdentifier() {
        return new Identifier(ForgeroInitializer.MOD_NAMESPACE, getToolPartIdentifierString());
    }

    @Override
    public PrimaryMaterial getPrimaryMaterial() {
        return material;
    }

    @Override
    public ForgeroToolPartTypes getType() {
        return type;
    }

    @Override
    public ForgeroToolPart getPart() {
        return part;
    }

    @Override
    public ToolPartItemImpl getItem() {
        return this;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        ForgeroToolPart toolPart = FabricToForgeroToolPartAdapter.createAdapter().getToolPart(stack).orElse(part);
        toolPart.createToolPartWithPropertiesDescription(new DescriptionWriter(tooltip));
    }
}
