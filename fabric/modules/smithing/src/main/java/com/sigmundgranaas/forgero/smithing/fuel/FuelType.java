package com.sigmundgranaas.forgero.smithing.fuel;

public enum FuelType {
    COAL(800, 100),
    CHARCOAL(1000, 120);

    private final int maxTemperature;
    private final int heatValue;

    FuelType(int maxTemperature, int heatValue) {
        this.maxTemperature = maxTemperature;
        this.heatValue = heatValue;
    }

    public int getMaxTemperature() { 
        return maxTemperature; 
    }
    
    public int getHeatValue() { 
        return heatValue; 
    }
}
