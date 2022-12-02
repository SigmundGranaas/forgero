package com.sigmundgranaas.forgero.state.customvalue;

public record CustomFloatValue(String identifier, float customValue) implements CustomValue {
    @Override
    public String presentableValue() {
        return String.valueOf(customValue);
    }
}
