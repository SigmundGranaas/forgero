package com.sigmundgranaas.forgero.core.resource.data.deserializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sigmundgranaas.forgero.core.context.Context;
import com.sigmundgranaas.forgero.core.context.Contexts;
import com.sigmundgranaas.forgero.core.property.CalculationOrder;
import com.sigmundgranaas.forgero.core.property.NumericOperation;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;

public class AttributeGroupDeserializer implements JsonDeserializer<List<PropertyPojo.Attribute>> {

	@Override
	public List<PropertyPojo.Attribute> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		List<PropertyPojo.Attribute> attributeList = new ArrayList<>();

		JsonArray groupArray = json.getAsJsonArray();
		for (JsonElement groupElement : groupArray) {
			JsonObject groupObject = groupElement.getAsJsonObject();

			// Use Gson's JsonElement.getAs methods with a default value to handle missing properties
			String contextValue = groupObject.has("context") ? groupObject.get("context").getAsString() : Contexts.UNDEFINED.value();
			String operationValue = groupObject.has("operation") ? groupObject.get("operation").getAsString() : NumericOperation.ADDITION.toString();
			String orderValue = groupObject.has("order") ? groupObject.get("order").getAsString() : CalculationOrder.BASE.toString();
			String categoryValue = groupObject.has("category") ? groupObject.get("category").getAsString() : Category.UNDEFINED.toString();
			int priorityValue = groupObject.has("priority") ? groupObject.get("priority").getAsInt() : 0;

			if (groupObject.has("attributes")) {
				JsonArray attributes = groupObject.get("attributes").getAsJsonArray();
				for (JsonElement attributeElement : attributes) {
					PropertyPojo.Attribute attribute = context.deserialize(attributeElement, PropertyPojo.Attribute.class);

					// Set default values for each property in case they are missing
					attribute.context = groupObject.has("context") ? Context.of(contextValue) : Contexts.UNDEFINED;
					attribute.operation = groupObject.has("operation") ? NumericOperation.valueOf(operationValue) : NumericOperation.ADDITION;
					attribute.order = groupObject.has("order") ? CalculationOrder.valueOf(orderValue) : CalculationOrder.BASE;
					attribute.category = groupObject.has("category") ? Category.valueOf(categoryValue) : Category.UNDEFINED;
					attribute.priority = priorityValue;

					attributeList.add(attribute);
				}
			} else {
				PropertyPojo.Attribute attribute = context.deserialize(groupElement, PropertyPojo.Attribute.class);
				attributeList.add(attribute);
			}
		}

		return attributeList;
	}
}
