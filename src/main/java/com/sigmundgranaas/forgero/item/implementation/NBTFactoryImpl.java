package com.sigmundgranaas.forgero.item.implementation;

import com.sigmundgranaas.forgero.core.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolWithBinding;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.tool.toolpart.*;
import com.sigmundgranaas.forgero.core.tool.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class NBTFactoryImpl implements NBTFactory {

    public static NBTFactory INSTANCE;
    private final Map<String, ForgeroTool> toolCache;

    public NBTFactoryImpl() {
        this.toolCache = new HashMap<>();
    }

    public static NBTFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NBTFactoryImpl();
        }
        return INSTANCE;
    }

    @Override
    public @NotNull
    ForgeroToolPart createToolPartFromNBT(@NotNull NbtCompound compound) {
        String primaryMaterialString = compound.getString(ToolPartItem.PRIMARY_MATERIAL_IDENTIFIER);
        PrimaryMaterial primary = (PrimaryMaterial) MaterialCollection.INSTANCE.getMaterial(ForgeroIdentifierFactory.INSTANCE.createForgeroMaterialIdentifier(primaryMaterialString));
        String secondaryMaterialString = compound.getString(ToolPartItem.SECONDARY_MATERIAL_IDENTIFIER);
        String toolPartTypeIdentifier = compound.getString(NBTFactory.TOOL_PART_TYPE_NBT_IDENTIFIER);
        ForgeroToolPartTypes toolPartTypes = ForgeroToolPartTypes.valueOf(toolPartTypeIdentifier);
        ForgeroToolTypes head = ForgeroToolTypes.valueOf(compound.getString(NBTFactory.HEAD_NBT_IDENTIFIER));

        return switch (toolPartTypes) {
            case HANDLE -> ForgeroToolPartFactory.INSTANCE.createToolPartHandleBuilder(primary).createToolPart();
            case BINDING -> ForgeroToolPartFactory.INSTANCE.createToolPartBindingBuilder(primary).createToolPart();
            case HEAD -> ForgeroToolPartFactory.INSTANCE.createToolPartHeadBuilder(primary, head).createToolPart();

        };
    }

    @Override
    public @NotNull
    ForgeroTool createToolFromNBT(@NotNull ForgeroToolItem baseTool, @NotNull NbtCompound compound) {
        Optional<String> hash = Optional.ofNullable(compound.getCompound(NBTFactory.FORGERO_TOOL_NBT_IDENTIFIER)).map(toolCompound -> toolCompound.getString(NBTFactory.HASH_NBT_IDENTIFIER));
        if (hash.isPresent() && toolCache.containsKey(hash.get())) {
            return toolCache.get(hash.get());
        }

        ToolPartHead head = (ToolPartHead) createToolPartFromNBT(compound.getCompound(ToolPartItem.HEAD_IDENTIFIER));
        ToolPartHandle handle = (ToolPartHandle) createToolPartFromNBT(compound.getCompound(ToolPartItem.HANDLE_IDENTIFIER));
        Optional<ToolPartBinding> binding = Optional.empty();
        if (compound.contains(ToolPartItem.BINDING_IDENTIFIER)) {
            binding = Optional.of((ToolPartBinding) createToolPartFromNBT(compound.getCompound(ToolPartItem.BINDING_IDENTIFIER)));
        }
        ForgeroTool tool;
        if (binding.isPresent()) {
            tool = ForgeroToolFactory.INSTANCE.createForgeroTool(head, handle, binding.get());
        } else {
            tool = ForgeroToolFactory.INSTANCE.createForgeroTool(head, handle);
        }
        return tool;
    }


    @Override
    public @NotNull
    NbtCompound createNBTFromTool(@NotNull ForgeroTool baseTool) {
        NbtCompound baseCompound = new NbtCompound();
        baseCompound.put(HEAD_NBT_IDENTIFIER, createNBTFromToolPart(baseTool.getToolHead()));
        baseCompound.put(HANDLE_NBT_IDENTIFIER, createNBTFromToolPart(baseTool.getToolHandle()));
        if (baseTool instanceof ForgeroToolWithBinding) {
            baseCompound.put(BINDING_NBT_IDENTIFIER, createNBTFromToolPart(((ForgeroToolWithBinding) baseTool).getBinding()));
        }
        String hash = UUID.randomUUID().toString();
        baseCompound.putString(HASH_NBT_IDENTIFIER, hash);
        toolCache.put(hash, baseTool);
        return baseCompound;
    }

    @Override
    public @NotNull
    NbtCompound createNBTFromToolPart(@NotNull ForgeroToolPart toolPart) {
        NbtCompound baseCompound = new NbtCompound();
        baseCompound.putString(PRIMARY_MATERIAL_NBT_IDENTIFIER, toolPart.getPrimaryMaterial().getName());
        baseCompound.putString(SECONDARY_MATERIAL_NBT_IDENTIFIER, toolPart.getSecondaryMaterial().getName());
        baseCompound.putString(GEM_NBT_IDENTIFIER, "NOT_IMPLEMENTED");
        baseCompound.putString(TOOL_PART_TYPE_NBT_IDENTIFIER, toolPart.getToolPartType().toString());
        baseCompound.putString(TOOL_PART_IDENTIFIER, toolPart.getToolPartIdentifier());
        if (toolPart.getToolPartType() == ForgeroToolPartTypes.HEAD) {
            baseCompound.putString(TOOL_PART_HEAD_TYPE_NBT_IDENTIFIER, ((ToolPartHead) toolPart).getHeadType().toString());
        }
        return baseCompound;
    }
}

