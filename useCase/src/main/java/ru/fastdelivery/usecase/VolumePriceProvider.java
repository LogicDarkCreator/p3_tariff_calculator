package ru.fastdelivery.usecase;

import ru.fastdelivery.domain.common.price.Price;

public interface VolumePriceProvider {
    Price costPerCubicMeter();
}
