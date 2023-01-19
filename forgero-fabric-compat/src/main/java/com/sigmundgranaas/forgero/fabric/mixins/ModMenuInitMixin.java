package com.sigmundgranaas.forgero.fabric.mixins;

import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import java.util.HashMap;


@Mixin(ModMenu.class)
public class ModMenuInitMixin {

    @Shadow(remap = false)
    private static ImmutableMap<String, ConfigScreenFactory<?>> configScreenFactories;

    @Inject(method = "onInitializeClient", at = @At(value = "TAIL"), remap = false)
    private void modify(CallbackInfo ci) {
        var map = new HashMap<>(configScreenFactories);
        String compat = "forgero-fabric-compat";
        map.put("forgero", map.get(compat));
        map.remove(compat);
        configScreenFactories = ImmutableMap.copyOf(map);
    }
}
