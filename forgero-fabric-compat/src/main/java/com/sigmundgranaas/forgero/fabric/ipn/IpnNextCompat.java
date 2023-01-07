package com.sigmundgranaas.forgero.fabric.ipn;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.state.State;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import org.anti_ad.mc.ipnext.config.AutoRefillSettings;
import org.anti_ad.mc.ipnext.event.AutoRefillHandler;

import java.util.function.Supplier;

public class IpnNextCompat {
    public static void blackListForgero(){
        ServerWorldEvents.LOAD.register((server, world) -> {
            var states =  ForgeroStateRegistry.STATES.all().stream().map(Supplier::get).map(State::identifier).toList();
            var joined = String.join(",", states);
            var oldValue = AutoRefillSettings.INSTANCE.getAUTOREFILL_BLACKLIST().getValue();
            AutoRefillSettings.INSTANCE.getAUTOREFILL_BLACKLIST().setValue(String.format("%s,%s", oldValue, joined));
            AutoRefillSettings.INSTANCE.getAUTOREFILL_BLACKLIST().getChangeHandler().invoke();
            AutoRefillHandler.INSTANCE.blackListChanged();
        });
    }
}
