package com.sigmundgranaas.forgero.minecraft.common.item.tooltip;

import com.sigmundgranaas.forgero.core.state.State;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedWriteHelper implements Writer{
    public static Map<State, List<Text>> tooltipCache = new ConcurrentHashMap<>();
    private final State state;
    private final Writer writer;

    public CachedWriteHelper(State state, Writer writer) {
        this.state = state;
        this.writer = writer;
    }

    public static CachedWriteHelper of(State state) {
        return new CachedWriteHelper(state, StateWriter.of(state));
    }

    public static CachedWriteHelper of(State state, Writer writer) {
        return new CachedWriteHelper(state, writer);
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        if(tooltipCache.containsKey(state)){
            tooltip.addAll(tooltipCache.get(state));
        }else{
            var list = new ArrayList<Text>();
            writer.write(list, context);
            tooltipCache.put(state, list);
            tooltip.addAll(list);
        }
    }
}
