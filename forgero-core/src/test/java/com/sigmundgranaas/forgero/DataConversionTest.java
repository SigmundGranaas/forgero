package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.resource.data.DataPool;
import com.sigmundgranaas.forgero.resource.data.StateConverter;
import com.sigmundgranaas.forgero.resource.data.v2.FilePackageLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.resource.data.Constant.CORE_PATH;

public class DataConversionTest {

    @Test
    void testConvertResources() {
        var dataPackage = new FilePackageLoader(CORE_PATH).get();
        var data = new DataPool(List.of(dataPackage)).assemble();
        StateConverter converter = new StateConverter(data.tree());
        var states = data.resources().stream().map(converter::convert).flatMap(Optional::stream).toList();

        Assertions.assertTrue(states.size() > 1);
        Assertions.assertTrue(states.stream().anyMatch(state -> state.name().equals("oak")));
    }
}
