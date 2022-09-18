package com.sigmundgranaas.forgero.item.items;

import com.sigmundgranaas.forgero.ForgeroRegistry;
import com.sigmundgranaas.forgero.resource.data.v1.pojo.GemPojo;
import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.property.Property;
import com.sigmundgranaas.forgero.property.PropertyContainer;
import com.sigmundgranaas.forgero.resource.ForgeroResourceType;
import com.sigmundgranaas.forgero.item.ForgeroItem;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.item.adapter.DescriptionWriter;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroGemAdapterImpl;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GemItem extends Item implements ForgeroItem<GemItem, GemPojo>, PropertyContainer {
    private final Gem gem;

    public GemItem(Settings settings, Gem gem) {
        super(settings);
        this.gem = gem;
    }

    public Gem getGem() {
        return ForgeroRegistry.GEM.getResource(gem);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        Gem newGem = new FabricToForgeroGemAdapterImpl().getGem(stack).orElse(this.gem);
        newGem.createToolPartDescription(new DescriptionWriter(tooltip));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        if (stack.hasNbt() && NBTFactory.INSTANCE.createGemFromNbt(stack.getOrCreateNbt()).orElse(gem).getLevel() > 7) {
            return true;
        } else {
            return super.hasGlint(stack);
        }
    }

    @Override
    public String getStringIdentifier() {
        return gem.getStringIdentifier();
    }

    @Override
    public String getResourceName() {
        return gem.getResourceName();
    }

    @Override
    public ForgeroResourceType getResourceType() {
        return ForgeroResourceType.GEM;
    }

    @Override
    public GemPojo toDataResource() {
        return getGem().toDataResource();
    }

    @Override
    public GemItem getItem() {
        return this;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        int containerResult = PropertyContainer.super.compareTo(o);
        if (containerResult != 0) {
            return containerResult;
        } else {
            return ForgeroItem.super.compareTo(o);
        }
    }

    @Override
    public @NotNull List<Property> getProperties() {
        return getGem().getProperties();
    }
}
