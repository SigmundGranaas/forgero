package com.sigmundgranaas.forgero.minecraft.common.registry;

import java.util.ArrayList;
import java.util.List;

public class RegistryHandler {
    public static RegistryHandler HANDLER = new RegistryHandler();
    private final List<Runnable> entries;

    public RegistryHandler() {
        this.entries = new ArrayList<>();
    }

    public void accept(Runnable handler){
        this.entries.add(handler);
    }
    public void run(){
        entries.forEach(Runnable::run);
    }
}
