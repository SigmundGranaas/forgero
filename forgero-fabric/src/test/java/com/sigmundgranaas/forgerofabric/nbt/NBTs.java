package com.sigmundgranaas.forgerofabric.nbt;

import com.sigmundgranaas.forgerocommon.item.nbt.v2.NbtConstants;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import static com.sigmundgranaas.forgerocommon.item.nbt.v2.NbtConstants.COMPOSITE_IDENTIFIER;

public class NBTs {
    public static NbtCompound HANDLE_NBT = handle();
    public static NbtCompound PICKAXE_HEAD_NBT = pickaxeHead();
    public static NbtCompound BINDING_NBT = binding();
    public static NbtCompound PICKAXE_NBT = pickaxe();

    private static NbtCompound handle() {
        var compound = new NbtCompound();
        compound.putString(NbtConstants.NAME_IDENTIFIER, "oak-handle");
        compound.putString(NbtConstants.ID_IDENTIFIER, "forgero:oak-handle");
        compound.putString(NbtConstants.STATE_TYPE_IDENTIFIER, COMPOSITE_IDENTIFIER);
        return compound;
    }

    private static NbtCompound pickaxeHead() {
        var compound = new NbtCompound();
        compound.putString(NbtConstants.NAME_IDENTIFIER, "iron-pickaxe_head");
        compound.putString(NbtConstants.ID_IDENTIFIER, "forgero:iron-pickaxe_head");
        compound.putString(NbtConstants.STATE_TYPE_IDENTIFIER, COMPOSITE_IDENTIFIER);
        NbtList ingredients = new NbtList();
        ingredients.add(iron());
        ingredients.add(pickaxeHeadSchematic());
        compound.put(NbtConstants.INGREDIENTS_IDENTIFIER, ingredients);
        return compound;
    }

    private static NbtCompound binding() {
        var compound = new NbtCompound();
        compound.putString(NbtConstants.NAME_IDENTIFIER, "iron-binding");
        compound.putString(NbtConstants.ID_IDENTIFIER, "forgero:iron-binding");
        compound.putString(NbtConstants.TYPE_IDENTIFIER, "TOOL_BINDING");
        compound.putString(NbtConstants.STATE_TYPE_IDENTIFIER, COMPOSITE_IDENTIFIER);
        NbtList ingredients = new NbtList();
        ingredients.add(iron());
        ingredients.add(bindingSchematic());
        compound.put(NbtConstants.INGREDIENTS_IDENTIFIER, ingredients);
        return compound;
    }

    private static NbtCompound pickaxe() {
        var compound = new NbtCompound();
        compound.putString(NbtConstants.NAME_IDENTIFIER, "iron-pickaxe");
        compound.putString(NbtConstants.ID_IDENTIFIER, "forgero:iron-pickaxe");
        compound.putString(NbtConstants.STATE_TYPE_IDENTIFIER, NbtConstants.COMPOSITE_IDENTIFIER);

        NbtList ingredients = new NbtList();
        ingredients.add(HANDLE_NBT);
        ingredients.add(PICKAXE_HEAD_NBT);
        compound.put(NbtConstants.INGREDIENTS_IDENTIFIER, ingredients);

        NbtList upgrades = new NbtList();
        upgrades.add(BINDING_NBT);
        compound.put(NbtConstants.UPGRADES_IDENTIFIER, upgrades);

        return compound;
    }

    private static NbtCompound oak() {
        var compound = new NbtCompound();
        compound.putString(NbtConstants.NAME_IDENTIFIER, "oak");
        compound.putString(NbtConstants.ID_IDENTIFIER, "forgero:oak");
        compound.putString(NbtConstants.STATE_TYPE_IDENTIFIER, NbtConstants.STATE_IDENTIFIER);
        return compound;
    }

    private static NbtCompound iron() {
        var compound = new NbtCompound();
        compound.putString(NbtConstants.NAME_IDENTIFIER, "iron");
        compound.putString(NbtConstants.ID_IDENTIFIER, "forgero:iron");
        compound.putString(NbtConstants.STATE_TYPE_IDENTIFIER, NbtConstants.STATE_IDENTIFIER);
        return compound;
    }

    private static NbtCompound pickaxeHeadSchematic() {
        var compound = new NbtCompound();
        compound.putString(NbtConstants.NAME_IDENTIFIER, "pickaxe_head-schematic");
        compound.putString(NbtConstants.ID_IDENTIFIER, "forgero:pickaxe_head-schematic");
        compound.putString(NbtConstants.STATE_TYPE_IDENTIFIER, NbtConstants.STATE_IDENTIFIER);
        return compound;
    }

    private static NbtCompound bindingSchematic() {
        var compound = new NbtCompound();
        compound.putString(NbtConstants.NAME_IDENTIFIER, "binding-schematic");
        compound.putString(NbtConstants.ID_IDENTIFIER, "forgero:binding-schematic");
        compound.putString(NbtConstants.STATE_TYPE_IDENTIFIER, NbtConstants.STATE_IDENTIFIER);
        return compound;
    }

    private static NbtCompound redstoneGem() {
        var compound = new NbtCompound();
        compound.putString(NbtConstants.NAME_IDENTIFIER, "iron-pickaxe_head");
        compound.putString(NbtConstants.ID_IDENTIFIER, "forgero:iron-pickaxe_head");
        compound.putString(NbtConstants.STATE_TYPE_IDENTIFIER, NbtConstants.COMPOSITE_IDENTIFIER);
        return compound;
    }
}
