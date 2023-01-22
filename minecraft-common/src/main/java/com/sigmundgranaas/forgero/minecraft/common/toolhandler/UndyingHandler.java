package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.StateEncoder;
import net.minecraft.item.ItemStack;

import java.util.Optional;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

public class UndyingHandler implements RunnableHandler {
    public static String UNDYING_TYPE = "UNDYING_REVIVAL";
    private final PropertyContainer container;

    private final ItemStack stack;

    public UndyingHandler(PropertyContainer container, ItemStack stack) {
        this.container = container;
        this.stack = stack;
    }

    public static Optional<UndyingHandler> of(PropertyContainer container, ItemStack stack) {
        var undying = container.stream().stream().filter(prop -> prop.type().equals(UNDYING_TYPE)).findAny();
        return undying.map(present -> new UndyingHandler(container, stack));
    }

    public void handle() {
        if (container instanceof Composite composite) {
            var newComposite = composite.removeUpgrade("undying-totem");
            stack.getOrCreateNbt().put(FORGERO_IDENTIFIER, StateEncoder.ENCODER.encode(newComposite));
        }
    }

    @Override
    public void run() {
        handle();
    }
}
