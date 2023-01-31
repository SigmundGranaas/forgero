package com.sigmundgranaas.forgero.minecraft.common.item.tooltip;

import com.sigmundgranaas.forgero.core.property.v2.cache.ContainsFeatureCache;
import com.sigmundgranaas.forgero.core.property.v2.cache.PropertyTargetCacheKey;
import com.sigmundgranaas.forgero.core.soul.SoulContainer;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.LeveledState;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.item.tooltip.writer.*;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.UnbreakableHandler;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static com.sigmundgranaas.forgero.core.condition.Conditions.BROKEN_TYPE_KEY;
import static com.sigmundgranaas.forgero.core.type.Type.*;

public class StateWriter implements Writer {
    protected final State state;

    public StateWriter(State state) {
        this.state = state;
    }

    public static Writer of(State state) {
        Writer writer;
        if (state.test(AXE)) {
            writer = new AxeWriter(state);
        } else if (state.test(TOOL)) {
            writer = new ToolWriter(state);
        } else if (state.test(SWORD_BLADE)) {
            writer = new SwordBladeWriter(state);
        } else if (state.test(AXE_HEAD)) {
            writer = new SwordBladeWriter(state);
        } else if (state.test(TOOL_PART_HEAD)) {
            writer = new AxeHeadWriter(state);
        } else if (state.test(SWORD_GUARD)) {
            writer = new SwordGuardWriter((state));
        } else if (state.test(PART)) {
            writer = new PartWriter((state));
        } else if (state.test(SCHEMATIC)) {
            writer = new SchematicWriter(state);
        } else if (state.test(GEM) && state instanceof LeveledState leveledState) {
            writer = new GemWriter(leveledState);
        } else {
            writer = new StateWriter(state);
        }
        return CachedWriteHelper.of(state, writer);
    }

    @Override
    public void write(List<Text> tooltip, TooltipContext context) {
        if (this.state instanceof SoulContainer container) {
            new SoulWriter(container.getSoul()).write(tooltip, context);
        }

        if (this.state instanceof Composite composite) {
            new CompositeWriter(composite).write(tooltip, context);
        }

        var passive = state.stream().getPassiveProperties().toList();

        if (passive.size() > 0) {
            MutableText attributes = Text.literal(" ").append(Text.translatable(Writer.toTranslationKey("properties"))).append(": ").formatted(Formatting.GRAY);
            tooltip.add(attributes);

            writePassives(tooltip, context);
            writeActive(tooltip, context);
        }

        if (UnbreakableHandler.isUnbreakable(state)) {
            tooltip.add(Text.literal(" ").append(Text.translatable("item.forgero.unbreakable")));
        }
        var key = PropertyTargetCacheKey.of(state, BROKEN_TYPE_KEY);
        if (ContainsFeatureCache.check(key)) {
            tooltip.add(Text.literal(" ").append(Text.translatable("item.forgero.broken")));
        }
    }

    private void writePassives(List<Text> tooltip, TooltipContext context) {
        var writer = new PassiveWriter();
        state.stream().getPassiveProperties().forEach(writer::addPassive);
        writer.write(tooltip, context);
    }

    private void writeActive(List<Text> tooltip, TooltipContext context) {
        var writer = new ActiveWriter();
        writer.write(tooltip, context);
    }

}
