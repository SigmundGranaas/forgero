package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.model.ModelRegistry;
import com.sigmundgranaas.forgero.resource.data.v2.data.ModelData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sigmundgranaas.forgero.testutil.Tools.IRON_PICKAXE;

public class ModelMatcherTest {
    @Test
    void getProperModel() {
        var pickaxe = IRON_PICKAXE;
        var pickaxeModel = ModelData.builder().target(List.of("PICKAXE")).modelType("COMPOSITE").build();
        var registry = new ModelRegistry();
        registry.register(pickaxeModel);
        var model = registry.find(pickaxe);
        Assertions.assertTrue(model.isPresent());
    }
}
