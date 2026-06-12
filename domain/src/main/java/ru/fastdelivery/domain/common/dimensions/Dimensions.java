package ru.fastdelivery.domain.common.dimensions;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Dimensions(
        int length,
        int width,
        int height
) {
    private static final int ROUND_TO = 50;
    private static final BigDecimal MM_TO_M3 = BigDecimal.valueOf(1_000_000_000L);

    public Dimensions {
        validateDimension(length, "length");
        validateDimension(width, "width");
        validateDimension(height, "height");
    }

    private void validateDimension(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " cannot be below Zero!");
        }
        if (value > 1500) {
            throw new IllegalArgumentException(name + " cannot be more than 1500 mm!");
        }
    }

    public Dimensions normalize() {
        return new Dimensions(
                roundUpToMultiple(length, ROUND_TO),
                roundUpToMultiple(width, ROUND_TO),
                roundUpToMultiple(height, ROUND_TO)
        );
    }

    private int roundUpToMultiple(int value, int multiple) {
        return ((value + multiple - 1) / multiple) * multiple;
    }

    public BigDecimal volumeInCubicMeters() {
        var normalized = normalize();
        long volumeMm3 = (long) normalized.length * normalized.width * normalized.height;
        return BigDecimal.valueOf(volumeMm3).divide(MM_TO_M3, 4, RoundingMode.HALF_UP);
    }
}