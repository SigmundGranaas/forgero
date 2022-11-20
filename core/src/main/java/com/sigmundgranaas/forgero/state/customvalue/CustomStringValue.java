package com.sigmundgranaas.forgero.state.customvalue;

public record CustomStringValue(String identifier, String value) implements CustomValue {

    @Override
    public String presentableValue() {
        return value;
    }
}
