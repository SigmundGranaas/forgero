package com.sigmundgranaas.forgero.property.active;

import com.sigmundgranaas.forgero.resource.data.PropertyPojo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class ActivePropertyRegistry {
    private static final List<PropertyEntry> entries = new ArrayList<>();

    public static void register(PropertyEntry entry) {
        entries.add(entry);
    }

    public static List<PropertyEntry> getEntries() {
        return entries;
    }

    public record PropertyEntry(Predicate<PropertyPojo.Active> predicate,
                                Function<PropertyPojo.Active, ActiveProperty> factory) {
    }
}
