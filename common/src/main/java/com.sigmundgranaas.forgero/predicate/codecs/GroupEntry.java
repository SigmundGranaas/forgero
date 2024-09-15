package com.sigmundgranaas.forgero.predicate.codecs;

import java.util.List;

public record GroupEntry<T>(String key, List<T> entries) {

}
