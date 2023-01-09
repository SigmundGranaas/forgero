package com.sigmundgranaas.forgero.core.resource.data.processor;

import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IngredientInflater {

    private final int entrySize;

    private final List<IngredientData> ingredients;

    public IngredientInflater(int entrySize, List<IngredientData> ingredients) {
        this.entrySize = entrySize;
        this.ingredients = ingredients;
    }

    public IngredientInflater addIngredient(int index, IngredientData data) {
        this.ingredients.add(index, data);
        return this;
    }

    public IngredientInflater copy() {
        return new IngredientInflater(entrySize, new ArrayList<>(ingredients));
    }

    public boolean hasEmptyIngredientSlots() {
        return ingredients.size() != entrySize;
    }

    public List<IngredientInflater> addEntries(int index, List<IngredientData> data) {
        return data.stream().map(entry -> copy().addIngredient(index, entry)).toList();
    }

    public List<IngredientData> getIngredients() {
        return ingredients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngredientInflater that = (IngredientInflater) o;

        if (entrySize == that.entrySize) {
            for (int i = 0; i < ingredients.size(); i++) {
                if (that.ingredients.get(i) != ingredients.get(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entrySize, ingredients);
    }
}
