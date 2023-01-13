package com.sigmundgranaas.forgero.core.resource.data;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.property.*;
import com.sigmundgranaas.forgero.core.property.active.BreakingDirection;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.property.passive.PassivePropertyType;
import com.sigmundgranaas.forgero.core.util.Identifiers;

import java.util.List;

/**
 * POJO used for parsing all properties from JSON files.
 */
public class PropertyPojo {
    @SerializedName("attributes")
    public List<PropertyPojo.Attribute> attributes;
    @SerializedName(value = "passiveProperties", alternate = {"passive", "passive_properties"})
    public List<PropertyPojo.Passive> passiveProperties;
    @SerializedName(value = "active")
    public List<PropertyPojo.Active> active;

    @SerializedName(value = "properties")
    public List<PropertyPojo.Property> properties;

    public static class Active {
        public ActivePropertyType type;
        public int depth;
        public String tag;
        public String description;
        public BreakingDirection direction;
        public String[] pattern;
    }

    public static class Attribute {
        public int priority = 0;
        public String id = Identifiers.EMPTY_IDENTIFIER;
        public AttributeType type;
        public CalculationOrder order;
        public NumericOperation operation;
        public float value;
        public PropertyPojo.Condition condition;
        public Category category;
    }

    public static class Property {
        public int priority = 0;
        public String id = Identifiers.EMPTY_IDENTIFIER;
        public String type;
        public String name;
        public float value;
        public int level;
        public List<String> tags;
        public BreakingDirection direction;
        public String[] pattern;
        public String description;
    }

    public static class Passive {
        public PassivePropertyType type;
        public String tag;
    }

    public static class Condition {
        public TargetTypes target;
        public List<String> tag;
    }
}
