package com.sigmundgranaas.forgero.smithing.fuel;

import java.util.HashMap;
import java.util.Map;

public class BloomeryFuelSystem {
    private Map<FuelType, Integer> fuelAmounts = new HashMap<>();
    private int currentTemperature = 0;

    public void addFuel(FuelType fuelType, int amount) {
        fuelAmounts.put(fuelType, fuelAmounts.getOrDefault(fuelType, 0) + amount);
        updateTemperature();
    }

    private void updateTemperature() {
        int totalHeat = 0;
        int maxPossibleTemp = 0;

        for (Map.Entry<FuelType, Integer> entry : fuelAmounts.entrySet()) {
            FuelType fuel = entry.getKey();
            int amount = entry.getValue();

            totalHeat += fuel.getHeatValue() * amount;
            maxPossibleTemp = Math.max(maxPossibleTemp, fuel.getMaxTemperature());
        }

        currentTemperature = Math.min(totalHeat, maxPossibleTemp);
    }

    public int getCurrentTemperature() {
        return currentTemperature;
    }

    public boolean canReachTemperature(int targetTemp) {
        return fuelAmounts.keySet().stream()
            .anyMatch(fuel -> fuel.getMaxTemperature() >= targetTemp);
    }

    public boolean consumeFuel() {
        for (FuelType fuelType : fuelAmounts.keySet()) {
            int amount = fuelAmounts.get(fuelType);
            if (amount > 0) {
                fuelAmounts.put(fuelType, amount - 1);
                updateTemperature();
                return true;
            }
        }
        return false;
    }
}
