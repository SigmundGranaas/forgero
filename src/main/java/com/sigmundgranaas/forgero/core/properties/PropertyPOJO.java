package com.sigmundgranaas.forgero.core.properties;

import com.sigmundgranaas.forgero.core.properties.attribute.CalculationOrder;

import java.util.ArrayList;
import java.util.List;

public class PropertyPOJO {
    private List<Attribute> attributes = new ArrayList<Attribute>();
    private List<Passive> passive = new ArrayList<Passive>();
    private List<Active> active = new ArrayList<Active>();

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public List<Passive> getPassive() {
        return passive;
    }

    public void setPassive(List<Passive> passive) {
        this.passive = passive;
    }

    public List<Active> getActive() {
        return active;
    }

    public void setActive(List<Active> active) {
        this.active = active;
    }

    public static class Active {
        private String type;
        private Double value;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }

    public static class Attribute {
        private AttributeType type;
        private CalculationOrder order;
        private NumericOperation operation;
        private Double value;
        private Condition condition;

        public AttributeType getType() {
            return type;
        }

        public void setType(AttributeType type) {
            this.type = type;
        }

        public CalculationOrder getOrder() {
            return order;
        }

        public void setOrder(CalculationOrder order) {
            this.order = order;
        }

        public NumericOperation getOperation() {
            return operation;
        }

        public void setOperation(NumericOperation operation) {
            this.operation = operation;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public Condition getCondition() {
            return condition;
        }

        public void setCondition(Condition condition) {
            this.condition = condition;
        }
    }


    public static class Condition {
        private TargetTypes target;
        private String tag;

        public TargetTypes getTarget() {
            return target;
        }

        public void setTarget(TargetTypes target) {
            this.target = target;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    public static class Passive {
        private String type;
        private Double value;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }


}
