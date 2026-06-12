package ru.fastdelivery.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.fastdelivery.domain.common.currency.Currency;
import ru.fastdelivery.domain.common.dimensions.Dimensions;
import ru.fastdelivery.domain.common.price.Price;
import ru.fastdelivery.domain.common.weight.Weight;
import ru.fastdelivery.domain.delivery.pack.Pack;
import ru.fastdelivery.domain.delivery.shipment.Shipment;
import ru.fastdelivery.usecase.distance.DistancePriceCalculator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TariffCalculateUseCaseTest {

    @Mock
    private WeightPriceProvider weightPriceProvider;

    @Mock
    private VolumePriceProvider volumePriceProvider;

    private DistancePriceProvider distancePriceProvider;
    private TariffCalculateUseCase useCase;
    private Currency rub;

    @BeforeEach
    void setUp() {
        rub = new Currency("RUB");
        distancePriceProvider = new DistancePriceCalculator();
        useCase = new TariffCalculateUseCase(
                weightPriceProvider,
                volumePriceProvider,
                distancePriceProvider,
                new HaversineDistanceCalculator(),
                450
        );

        when(weightPriceProvider.costPerKg())
                .thenReturn(new Price(new BigDecimal("400"), rub));
        when(weightPriceProvider.minimalPrice())
                .thenReturn(new Price(new BigDecimal("350"), rub));
        when(volumePriceProvider.costPerCubicMeter())
                .thenReturn(new Price(new BigDecimal("5000"), rub));
    }

    @Test
    void shouldCalculatePriceByWeightOnly() {
        var weight = new Weight(BigInteger.valueOf(10_000)); // 10 kg
        var pack = new Pack(weight);
        var shipment = new Shipment(List.of(pack), rub);

        var price = useCase.calc(shipment);

        // 10 kg * 400 RUB/kg = 4000 RUB
        assertThat(price.amount()).isEqualByComparingTo(new BigDecimal("4000"));
    }

    @Test
    void shouldUseMinimalPriceWhenWeightIsSmall() {
        var weight = new Weight(BigInteger.valueOf(500)); // 0.5 kg
        var pack = new Pack(weight);
        var shipment = new Shipment(List.of(pack), rub);

        var price = useCase.calc(shipment);

        // 0.5 kg * 400 = 200, но минимальная цена 350
        assertThat(price.amount()).isEqualByComparingTo(new BigDecimal("350"));
    }

    @Test
    void shouldCalculatePriceByVolumeWhenHigher() {
        var weight = new Weight(BigInteger.valueOf(1_000)); // 1 kg - цена 400 руб
        var dimensions = new Dimensions(500, 500, 500); // 0.125 m3 - цена 625 руб
        var pack = new Pack(weight, dimensions);
        var shipment = new Shipment(List.of(pack), rub);

        var price = useCase.calc(shipment);

        // Объёмная стоимость выше, должна быть выбрана она
        assertThat(price.amount()).isEqualByComparingTo(new BigDecimal("625"));
    }

    @Test
    void shouldCalculatePriceByWeightWhenHigher() {
        var weight = new Weight(BigInteger.valueOf(10_000)); // 10 kg - цена 4000 руб
        var dimensions = new Dimensions(100, 100, 100); // 0.001 m3 - цена 5 руб
        var pack = new Pack(weight, dimensions);
        var shipment = new Shipment(List.of(pack), rub);

        var price = useCase.calc(shipment);

        assertThat(price.amount()).isEqualByComparingTo(new BigDecimal("4000"));
    }

    @Test
    void shouldCalculatePriceWithDistanceFactor() {
        var weight = new Weight(BigInteger.valueOf(5_000)); // 5 kg - базовая цена 2000 руб
        var pack = new Pack(weight);
        var shipment = new Shipment(List.of(pack), rub);
        double distance = 500; // 500 км > 450 км

        var price = useCase.calculateFullPrice(shipment, distance);

        // 2000 * (500/450) = 2222.222... -> округление до 2222.23
        assertThat(price.amount()).isEqualByComparingTo(new BigDecimal("2222.23"));
    }

    @Test
    void shouldNotAddDistanceFactorWhenDistanceLessThanMin() {
        var weight = new Weight(BigInteger.valueOf(5_000)); // 5 kg - базовая цена 2000 руб
        var pack = new Pack(weight);
        var shipment = new Shipment(List.of(pack), rub);
        double distance = 300;

        var price = useCase.calculateFullPrice(shipment, distance);

        assertThat(price.amount()).isEqualByComparingTo(new BigDecimal("2000"));
    }
}