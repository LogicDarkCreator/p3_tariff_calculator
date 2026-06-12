package ru.fastdelivery.usecase.distance;

import org.springframework.stereotype.Component;
import ru.fastdelivery.domain.common.price.Price;
import ru.fastdelivery.usecase.DistancePriceProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DistancePriceCalculator implements DistancePriceProvider {

    @Override
    public Price calculateWithDistance(Price basePrice, double distanceKm, int minDistanceKm) {
        if (distanceKm <= minDistanceKm) {
            return basePrice;
        }

        double multiplier = distanceKm / minDistanceKm;
        BigDecimal multipliedAmount = basePrice.amount()
                .multiply(BigDecimal.valueOf(multiplier));

        // Округление до 2 знаков (копейки) в большую сторону
        BigDecimal roundedAmount = multipliedAmount.setScale(2, RoundingMode.CEILING);

        return new Price(roundedAmount, basePrice.currency());
    }
}