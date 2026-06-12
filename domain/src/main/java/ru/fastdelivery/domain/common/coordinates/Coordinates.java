package ru.fastdelivery.domain.common.coordinates;

import java.math.BigDecimal;

public record Coordinates(
        BigDecimal latitude,
        BigDecimal longitude
) {
    public Coordinates {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Latitude and longitude must not be null");
        }
    }

    public double latitudeAsDouble() {
        return latitude.doubleValue();
    }

    public double longitudeAsDouble() {
        return longitude.doubleValue();
    }
}
