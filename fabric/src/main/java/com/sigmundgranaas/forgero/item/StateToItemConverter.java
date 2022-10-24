package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.item.items.DefaultStateItem;
import com.sigmundgranaas.forgero.item.items.tool.DynamicAxeItem;
import com.sigmundgranaas.forgero.item.items.tool.DynamicPickaxeItem;
import com.sigmundgranaas.forgero.item.items.tool.DynamicSwordItem;
import com.sigmundgranaas.forgero.state.State;
import com.sigmundgranaas.forgero.type.Type;
import com.sigmundgranaas.forgero.util.match.Context;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Identifier;

public class StateToItemConverter {
    private final State state;

    public StateToItemConverter(State state) {
        this.state = state;
    }

    public static StateToItemConverter of(State state) {
        return new StateToItemConverter(state);
    }

    public Item convert() {
        var context = Context.of();
        if (state.type().test(Type.of("SWORD"), context)) {
            return new DynamicSwordItem(ToolMaterials.WOOD, 1, 1, new FabricItemSettings().group(getItemGroup()), state);
        } else if (state.type().test(Type.of("PICKAXE"), context)) {
            return new DynamicPickaxeItem(ToolMaterials.WOOD, 1, 1, new FabricItemSettings().group(getItemGroup()), state);
        } else if (state.type().test(Type.of("AXE"), context)) {
            return new DynamicAxeItem(ToolMaterials.WOOD, 1, 1, new FabricItemSettings().group(getItemGroup()), state);
        } else if (state.type().test(Type.of("HOE"), context)) {
            return new DynamicAxeItem(ToolMaterials.WOOD, 1, 1, new FabricItemSettings().group(getItemGroup()), state);
        } else if (state.type().test(Type.of("SHOVEL"), context)) {
            return new DynamicAxeItem(ToolMaterials.WOOD, 1, 1, new FabricItemSettings().group(getItemGroup()), state);
        }
        return defaultStateItem();
    }

    public Identifier id() {
        return new Identifier(state.nameSpace(), state.name());
    }

    private Item defaultStateItem() {
        return new DefaultStateItem(new FabricItemSettings().group(ItemGroups.FORGERO_TOOL_PARTS), state);
    }

    private ItemGroup getItemGroup() {
        return ItemGroups.FORGERO_TOOL_PARTS;
    }
}
