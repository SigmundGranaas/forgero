package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.registry.StateFinder;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.MaterialBased;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.SOUL_IDENTIFIER;


public class ToolParser extends CompositeParser {
    public ToolParser(StateFinder supplier) {
        super(supplier);
    }

    @Override
    public Optional<State> parse(NbtCompound compound) {
        List<State> parts = new ArrayList<>();
        parseParts(parts::add, compound);

        if (parts.size() == 2) {
            var id = compound.getString(NbtConstants.ID_IDENTIFIER);
            var stateOpt = supplier.find(id);
            var head = parts.stream().filter(part -> part.test(Type.TOOL_PART_HEAD) || part.test(Type.SWORD_BLADE)).findFirst();
            var handle = parts.stream().filter(part -> part.test(Type.HANDLE)).findFirst();
            if (head.isPresent() && handle.isPresent()) {
                ConstructedTool.ToolBuilder builder = null;
                if (head.get() instanceof MaterialBased based) {
                    builder = ConstructedTool.ToolBuilder.builder(head.get(), handle.get(), based.baseMaterial());
                } else if (head.get() instanceof Composite composite) {
                    var material = composite.components().stream()
                            .filter(comp -> comp.test(Type.MATERIAL))
                            .findFirst();
                    if (material.isPresent()) {
                        builder = ConstructedTool.ToolBuilder.builder(head.get(), handle.get(), material.get());
                    }
                }
                if (builder != null) {
                    if (stateOpt.isPresent() && stateOpt.get() instanceof Composite upgradeable) {
                        builder.addSlotContainer(new SlotContainer(upgradeable.slots()));
                    }
                    parseUpgrades(builder::addUpgrade, compound);
                    if (compound.contains(SOUL_IDENTIFIER)) {
                        var soul = SoulParser.PARSER.parse(compound);
                        if (soul.isPresent()) {
                            return Optional.of(builder.soul(soul.get()).build());
                        }
                    }
                    return Optional.of(builder.build());
                }
            } else if (ForgeroStateRegistry.CONTAINER_TO_STATE.containsKey(id)) {
                return supplier.find(ForgeroStateRegistry.CONTAINER_TO_STATE.get(id));
            }
        }

        return Optional.empty();
    }
}
