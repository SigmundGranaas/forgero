package com.sigmundgranaas.forgeroforge.resources.dynamic;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.state.Composite;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.recipe.*;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public class PartToSchematicGenerator implements DynamicResourceGenerator {

    @Override
    public void generate(RuntimeResourcePack pack) {
        parts().forEach(comp -> createRecipe(comp).ifPresent(recipe -> pack.addRecipe(id(comp), recipe)));
    }

    private List<Composite> parts() {
        return ForgeroStateRegistry.STATES.all().stream()
                .filter(Composite.class::isInstance)
                .map(Composite.class::cast)
                .filter(comp -> comp.ingredients().stream().anyMatch(ingredient -> ingredient.name().contains("schematic")))
                .toList();
    }

    private Optional<JRecipe> createRecipe(Composite composite) {
        var schematic = composite.ingredients().stream().filter(ingredient -> ingredient.name().contains("schematic")).findFirst();
        if (schematic.isPresent()) {
            var ingredients = JIngredients.ingredients().add(JIngredient.ingredient().item(Items.PAPER)).add(JIngredient.ingredient().item(composite.identifier()));
            return Optional.of(JShapelessRecipe.shapeless(ingredients, JResult.result(schematic.get().identifier())));
        }
        return Optional.empty();

    }

    private Identifier id(Composite composite) {
        return new Identifier(composite.nameSpace(), composite.name() + "-schematic_recipe");
    }
}
