package com.sigmundgranaas.forgero.minecraft.common.predicate.codecs;

import java.util.List;

public record GroupEntry<T>(String key, List<T> entries) {

}
