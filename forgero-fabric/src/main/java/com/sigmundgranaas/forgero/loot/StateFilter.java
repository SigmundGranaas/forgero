package com.sigmundgranaas.forgero.loot;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgero.state.Identifiable;
import com.sigmundgranaas.forgero.state.State;
import lombok.Builder;
import lombok.Data;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
public class StateFilter {
    @Builder.Default
    private int upperRarity = 0;
    @Builder.Default
    private int lowerRarity = 0;
    @Builder.Default
    private List<String> types = new ArrayList<>();
    @Builder.Default
    private List<String> ids = new ArrayList<>();

    @Builder.Default
    private List<String> exclusion = new ArrayList<>();

    @Builder.Default
    private List<String> include = new ArrayList<>();

    public static StateFilterBuilder builder() {
        return new StateFilterBuilder();
    }

    public List<Item> filter() {
        var states = new ArrayList<State>();
        types.stream()
                .map(type -> ForgeroStateRegistry.TREE.find(type).map(node -> node.getResources(State.class)).orElse(ImmutableList.<State>builder().build()))
                .flatMap(Collection::stream)
                .filter(this::filter)
                .forEach(states::add);

        ids.stream()
                .map(id -> ForgeroStateRegistry.STATES.get(id))
                .flatMap(Optional::stream)
                .filter(this::filter)
                .forEach(states::add);

        return states.stream().map(Identifiable::identifier).map(id -> Registry.ITEM.get(new Identifier(id))).toList();
    }

    private boolean filter(State state) {
        if (state.stream().applyAttribute(AttributeType.RARITY) < lowerRarity) {
            return false;
        }

        if (state.stream().applyAttribute(AttributeType.RARITY) > upperRarity) {
            return false;
        }

        if (exclusion.stream().anyMatch(exclusion -> stringMatch(exclusion, state))) {
            return false;
        }

        if (include.size() > 0 && include.stream().noneMatch(include -> stringMatch(include, state))) {
            return false;
        }

        return true;
    }

    private boolean stringMatch(String match, State state) {
        if (state.identifier().contains(match)) {
            return true;
        }
        if (state.type().typeName().contains(match)) {
            return true;
        }

        return false;
    }

}
