package com.sigmundgranaas.forgero.core.resource.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.context.Context;
import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.active.BreakingDirection;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.util.Identifiers;

/**
 * POJO used for parsing all properties from JSON files.
 */
public class PropertyPojo {
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

	public static class Attribute {
		public int priority = 0;
		public String id = Identifiers.EMPTY_IDENTIFIER;
		public String type;
		public CalculationOrder order = CalculationOrder.BASE;
		public NumericOperation operation = NumericOperation.ADDITION;
		public float value;
		public JsonElement predicate;
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
					", predicate=" + predicate +
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
}
