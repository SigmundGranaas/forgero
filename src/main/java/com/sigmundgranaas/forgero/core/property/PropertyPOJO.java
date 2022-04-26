package com.sigmundgranaas.forgero.core.property;

import com.sigmundgranaas.forgero.core.property.active.ActivePropertyType;

import java.util.List;

/**
 * POJO used for parsing all properties from JSON files.
 */
public class PropertyPOJO {
    public List<PropertyPOJO.Attribute> attributes;
    public List<PropertyPOJO.Passive> passive;
    public List<PropertyPOJO.Active> active;

    public static class Active {
        public ActivePropertyType type;
        public int depth;
        public String tag;
        public String description;
        public String[] pattern;
    }

    public static class Attribute {
        public AttributeType type;
        public CalculationOrder order;
        public NumericOperation operation;
        public float value;
        public PropertyPOJO.Condition condition;

    }


    public static class Condition {
        public TargetTypes target;
        public List<String> tag;

    }

    public static class Passive {
        public String type;
        public float value;

    }
}
