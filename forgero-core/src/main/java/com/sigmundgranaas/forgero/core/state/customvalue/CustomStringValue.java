package com.sigmundgranaas.forgero.core.state.customvalue;

public record CustomStringValue(String identifier, String value) implements CustomValue {

    @Override
    public String presentableValue() {
        return value;
    }
}
