package com.sigmundgranaas.forgero.core.data.pojo;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.property.*;
import com.sigmundgranaas.forgero.core.property.active.BreakingDirection;
import com.sigmundgranaas.forgero.core.property.passive.PassivePropertyType;

import java.util.List;

/**
 * POJO used for parsing all properties from JSON files.
 */
public class PropertyPOJO {
    @SerializedName("attributes")
    public List<PropertyPOJO.Attribute> attributes;
    @SerializedName(value = "passiveProperties", alternate = "passive")
    public List<PropertyPOJO.Passive> passiveProperties;
    @SerializedName(value = "active")
    public List<PropertyPOJO.Active> active;

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
        public PropertyPOJO.Condition condition;

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
