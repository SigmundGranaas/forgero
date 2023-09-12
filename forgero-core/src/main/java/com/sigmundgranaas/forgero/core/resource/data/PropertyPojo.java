package com.sigmundgranaas.forgero.core.resource.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.context.Context;
import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.ActivePropertyType;
import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.TargetTypes;
import com.sigmundgranaas.forgero.core.property.active.BreakingDirection;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.property.passive.PassivePropertyType;
import com.sigmundgranaas.forgero.core.util.Identifiers;

/**
 * POJO used for parsing all properties from JSON files.
 */
public class PropertyPojo {
	@SerializedName(value = "passiveProperties", alternate = {"passive", "passive_properties"})
	public List<PropertyPojo.Passive> passiveProperties;
	@SerializedName(value = "active")
	public List<PropertyPojo.Active> active;
	@SerializedName(value = "features")
	public List<PropertyPojo.Feature> features;
	@SerializedName("attributes")
	private List<PropertyPojo.Attribute> attributes;
	@SerializedName("grouped_attributes")
	private List<PropertyPojo.Attribute> groupedAttributes;

	public List<Attribute> getAttributes() {
		var list = new ArrayList<Attribute>();
		list.addAll(Objects.requireNonNullElse(attributes, Collections.emptyList()));
		list.addAll(Objects.requireNonNullElse(groupedAttributes, Collections.emptyList()));
		return list;
	}

	public void setAttributes(List<PropertyPojo.Attribute> attributes) {
		var newList = new ArrayList<>(attributes);
		this.attributes = newList;
		this.groupedAttributes = Collections.emptyList();
	}

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
		public String type;
		public CalculationOrder order = CalculationOrder.BASE;
		public NumericOperation operation = NumericOperation.ADDITION;
		public float value;
		public PropertyPojo.Condition condition;
		public Category category = Category.UNDEFINED;
		public Context context = Contexts.UNDEFINED;
		public float max;
		public float min;

		@Override
		public String toString() {
			return "Attribute{" +
					"priority=" + priority +
					", id='" + id + '\'' +
					", type='" + type + '\'' +
					", order=" + order +
					", operation=" + operation +
					", value=" + value +
					", condition=" + condition +
					", category=" + category +
					", context=" + context +
					'}';
		}
	}

	public static class Feature {
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
