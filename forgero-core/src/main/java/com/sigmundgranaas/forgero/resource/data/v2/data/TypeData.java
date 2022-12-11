package com.sigmundgranaas.forgero.resource.data.v2.data;

import java.util.List;
import java.util.Optional;

public record TypeData(String name, Optional<String> parent, List<String> children) {
}
