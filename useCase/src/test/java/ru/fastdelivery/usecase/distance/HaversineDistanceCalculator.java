package ru.fastdelivery.usecase.distance;

import org.junit.jupiter.api.Test;
import ru.fastdelivery.domain.common.coordinates.Coordinates;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class HaversineDistanceCalculatorTest {

    private final HaversineDistanceCalculator calculator = new HaversineDistanceCalculator();

    @Test
    void shouldCalculateDistanceBetweenMoscowAndStPetersburg() {
        var moscow = new Coordinates(new BigDecimal("55.7558"), new BigDecimal("37.6176"));
        var spb = new Coordinates(new BigDecimal("59.9311"), new BigDecimal("30.3609"));

        double distance = calculator.calculateDistance(moscow, spb);

        // Расстояние между Москвой и Санкт-Петербургом ~ 634 км
        assertThat(distance).isCloseTo(634, within(5.0));
    }

    @Test
    void shouldReturnZeroForSamePoint() {
        var point = new Coordinates(new BigDecimal("55.446008"), new BigDecimal("65.339151"));

        double distance = calculator.calculateDistance(point, point);

        assertThat(distance).isCloseTo(0, within(0.001));
    }

    @Test
    void shouldCalculateDistanceBetweenKurganAndOmsk() {
        var kurgan = new Coordinates(new BigDecimal("55.446008"), new BigDecimal("65.339151"));
        var omsk = new Coordinates(new BigDecimal("54.9885"), new BigDecimal("73.3242"));

        double distance = calculator.calculateDistance(kurgan, omsk);

        // Расстояние между Курганом и Омском ~ 500-520 км
        assertThat(distance).isBetween(500.0, 530.0);
    }
}