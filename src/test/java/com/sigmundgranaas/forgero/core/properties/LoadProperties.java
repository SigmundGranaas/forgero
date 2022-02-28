package com.sigmundgranaas.forgero.core.properties;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.properties.attribute.AttributeBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

public class LoadProperties {

    @Test
    void loadedPropertyIsNotNull() {
        Assertions.assertNotNull(getExamplePropertyFromFile());
    }

    @Test
    void assertLoadedPOJOHasProperties() {
        PropertyPOJO pojo = getExamplePropertyFromFile();
        Assertions.assertEquals(4, pojo.attributes.size());
    }

    @Test
    void createAttributeFromJSON() {
        PropertyPOJO pojo = getExamplePropertyFromFile();
        Attribute attribute = AttributeBuilder.createAttributeFromPojo(pojo.attributes.get(0));
        Assertions.assertEquals(attribute.getAttributeType(),pojo.attributes.get(0).type);
    }

    PropertyPOJO getExamplePropertyFromFile(){
        InputStream stream = LoadProperties.class.getResourceAsStream("/properties/exampleproperties.json");
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new InputStreamReader(stream));
        return gson.fromJson(reader, PropertyPOJO.class);
    }


}
