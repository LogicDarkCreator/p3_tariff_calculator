package ru.fastdelivery.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.fastdelivery.domain.common.price.Price;
import ru.fastdelivery.domain.delivery.shipment.Shipment;
import ru.fastdelivery.domain.delivery.pack.Pack;

import javax.inject.Named;
import java.math.BigDecimal;

@Named
public class TariffCalculateUseCase {
    private static final Logger log = LoggerFactory.getLogger(TariffCalculateUseCase.class);

    private final WeightPriceProvider weightPriceProvider;
    private final VolumePriceProvider volumePriceProvider;
    private final DistancePriceProvider distancePriceProvider;
    private final int minDistanceKm;

    public TariffCalculateUseCase(
            WeightPriceProvider weightPriceProvider,
            VolumePriceProvider volumePriceProvider,
            DistancePriceProvider distancePriceProvider) {
        this(weightPriceProvider, volumePriceProvider, distancePriceProvider, 450);
    }

    public TariffCalculateUseCase(
            WeightPriceProvider weightPriceProvider,
            VolumePriceProvider volumePriceProvider,
            DistancePriceProvider distancePriceProvider,
            int minDistanceKm) {
        this.weightPriceProvider = weightPriceProvider;
        this.volumePriceProvider = volumePriceProvider;
        this.distancePriceProvider = distancePriceProvider;
        this.minDistanceKm = minDistanceKm;
    }

    private Price calculateByWeight(Shipment shipment) {
        var weightAllPackagesKg = shipment.weightAllPackages().kilograms();
        var minimalPrice = weightPriceProvider.minimalPrice();

        var costByWeight = weightPriceProvider.costPerKg().multiply(weightAllPackagesKg);
        return costByWeight.amount().compareTo(minimalPrice.amount()) > 0 ? costByWeight : minimalPrice;
    }

    private Price calculateByVolume(Shipment shipment) {
        if (!shipment.hasDimensions()) {
            return null;
        }

        var totalVolumeM3 = shipment.packages().stream()
                .filter(Pack::hasDimensions)
                .map(pack -> pack.dimensions().volumeInCubicMeters())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalVolumeM3.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        var volumePrice = volumePriceProvider.costPerCubicMeter().multiply(totalVolumeM3);
        log.debug("Volume-based price: {} for volume {} m3", volumePrice.amount(), totalVolumeM3);
        return volumePrice;
    }

    private Price calculateBasePrice(Shipment shipment) {
        var priceByWeight = calculateByWeight(shipment);
        var priceByVolume = calculateByVolume(shipment);

        if (priceByVolume == null) {
            log.debug("Using weight-based price: {}", priceByWeight.amount());
            return priceByWeight;
        }

        if (!priceByWeight.currency().equals(priceByVolume.currency())) {
            log.error("Currency mismatch: weight={}, volume={}",
                    priceByWeight.currency().getCode(),
                    priceByVolume.currency().getCode());
            throw new IllegalStateException("Currency mismatch between weight and volume pricing");
        }

        Price result = priceByWeight.amount().compareTo(priceByVolume.amount()) > 0
                ? priceByWeight
                : priceByVolume;

        log.debug("Base price selected: {} (weight: {}, volume: {})",
                result.amount(), priceByWeight.amount(), priceByVolume.amount());
        return result;
    }

    public Price calculateFullPrice(Shipment shipment, double distanceKm) {
        var basePrice = calculateBasePrice(shipment);
        log.info("Base price: {}, distance: {} km", basePrice.amount(), distanceKm);

        var finalPrice = distancePriceProvider.calculateWithDistance(basePrice, distanceKm, minDistanceKm);
        log.info("Final price after distance adjustment: {}", finalPrice.amount());
        return finalPrice;
    }

    public Price calc(Shipment shipment) {
        Price result = calculateBasePrice(shipment);
        log.info("Calculated price: {}", result.amount());
        return result;
    }

    public Price minimalPrice() {
        return weightPriceProvider.minimalPrice();
    }
}