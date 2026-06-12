package ru.fastdelivery.usecase;

import ru.fastdelivery.domain.common.coordinates.Coordinates;

/**
 * Расчёт расстояния между географическими координатами
 * по формуле гаверсинусов
 */
public interface DistanceCalculator {
    double calculateDistance(Coordinates from, Coordinates to);
}