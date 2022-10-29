package com.sigmundgranaas.forgero.resource.data.v1.pojo;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.property.*;
import com.sigmundgranaas.forgero.property.active.BreakingDirection;
import com.sigmundgranaas.forgero.property.attribute.Category;
import com.sigmundgranaas.forgero.property.passive.PassivePropertyType;

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

    public static class Active {
        public ActivePropertyType type;
        public int depth;
        public String tag;
        public String description;
        public BreakingDirection direction;
        public String[] pattern;
    }

    public static class Attribute {
        public AttributeType type;
        public CalculationOrder order;
        public NumericOperation operation;
        public float value;
        public PropertyPojo.Condition condition;
        public Category category;
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
