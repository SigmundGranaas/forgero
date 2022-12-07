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

    public static StateFilterBuilder builder() {
        return new StateFilterBuilder();
    }

    public List<Item> filter() {
        var states = new ArrayList<State>();
        types.stream()
                .map(type -> ForgeroStateRegistry.TREE.find(type).map(node -> node.getResources(State.class)).orElse(ImmutableList.<State>builder().build()))
                .flatMap(Collection::stream)
                .filter(state -> state.stream().applyAttribute(AttributeType.RARITY) > lowerRarity)
                .filter(state -> state.stream().applyAttribute(AttributeType.RARITY) < upperRarity)
                .forEach(states::add);

        ids.stream()
                .map(id -> ForgeroStateRegistry.STATES.get(id))
                .flatMap(Optional::stream)
                .filter(state -> state.stream().applyAttribute(AttributeType.RARITY) > lowerRarity)
                .filter(state -> state.stream().applyAttribute(AttributeType.RARITY) < upperRarity)
                .forEach(states::add);

        return states.stream().map(Identifiable::identifier).map(id -> Registry.ITEM.get(new Identifier(id))).toList();
    }
}
