package com.sigmundgranaas.forgero.core.data.v1;

import com.sigmundgranaas.forgero.core.data.ResourceType;
import com.sigmundgranaas.forgero.core.data.factory.SchematicFactory;
import com.sigmundgranaas.forgero.core.data.v1.pojo.SchematicPOJO;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.sigmundgranaas.forgero.core.data.ForgeroDataResource.DEFAULT_DEPENDENCIES_LIST;
import static com.sigmundgranaas.forgero.core.data.ForgeroDataResource.DEFAULT_DEPENDENCIES_SET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchematicDataTest {
    public static SchematicPOJO createDefaultSchematicPojo() {
        var pojo = new SchematicPOJO();
        pojo.abstractResource = false;
        pojo.dependencies = DEFAULT_DEPENDENCIES_LIST;
        pojo.materialCount = 1;
        pojo.type = ForgeroToolPartTypes.HEAD;
        pojo.toolType = ForgeroToolTypes.PICKAXE;
        pojo.name = "default";
        pojo.model = "default";
        pojo.parent = null;
        pojo.resourceType = ResourceType.SCHEMATIC;
        pojo.properties = null;
        return pojo;
    }

    @Test
    void createSchematicFromPojo() {
        var pojo = createDefaultSchematicPojo();
        var factory = new SchematicFactory(List.of(pojo), DEFAULT_DEPENDENCIES_SET);
        var schematic = factory.buildResource(pojo);

        assertTrue(schematic.isPresent());
    }

    @Test
    void nameIsPreserved() {
        var pojo = createDefaultSchematicPojo();
        var factory = new SchematicFactory(List.of(pojo), DEFAULT_DEPENDENCIES_SET);
        var schematicOpt = factory.buildResource(pojo);
        assertTrue(schematicOpt.isPresent());

        assertEquals(pojo.name, schematicOpt.get().getResourceName());
    }

    @Test
    void dependenciesDefault() {
        var pojo = createDefaultSchematicPojo();
        pojo.dependencies = null;
        var factory = new SchematicFactory(List.of(pojo), DEFAULT_DEPENDENCIES_SET);
        var schematicOpt = factory.buildResource(pojo);

        assertTrue(schematicOpt.isPresent());
    }

    @Test
    void noFactoryDependenciesDefaults() {
        var pojo = createDefaultSchematicPojo();
        pojo.dependencies = null;
        var factory = new SchematicFactory(List.of(pojo), Collections.emptySet());
        var schematicOpt = factory.buildResource(pojo);

        assertTrue(schematicOpt.isPresent());
    }

    @Test
    void abstractResourceIsEmpty() {
        var pojo = createDefaultSchematicPojo();
        pojo.abstractResource = true;
        var factory = new SchematicFactory(List.of(pojo), Collections.emptySet());
        var schematicOpt = factory.buildResource(pojo);

        assertTrue(schematicOpt.isEmpty());
    }

    @Test
    void missingDependencyFails() {
        var pojo = createDefaultSchematicPojo();
        pojo.dependencies = List.of("test");
        var factory = new SchematicFactory(List.of(pojo), DEFAULT_DEPENDENCIES_SET);
        var schematicOpt = factory.buildResource(pojo);

        assertTrue(schematicOpt.isEmpty());
    }

    @Test
    void minecraftDependencySucceeds() {
        var pojo = createDefaultSchematicPojo();
        pojo.dependencies = List.of("minecraft");
        var factory = new SchematicFactory(List.of(pojo), DEFAULT_DEPENDENCIES_SET);
        var schematicOpt = factory.buildResource(pojo);

        assertTrue(schematicOpt.isPresent());
    }
}
