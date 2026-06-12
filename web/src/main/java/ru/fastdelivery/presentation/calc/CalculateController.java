package ru.fastdelivery.presentation.calc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.fastdelivery.domain.common.coordinates.Coordinates;
import ru.fastdelivery.domain.common.currency.CurrencyFactory;
import ru.fastdelivery.domain.common.dimensions.Dimensions;
import ru.fastdelivery.domain.common.price.Price;
import ru.fastdelivery.domain.common.weight.Weight;
import ru.fastdelivery.domain.delivery.pack.Pack;
import ru.fastdelivery.domain.delivery.shipment.Shipment;
import ru.fastdelivery.presentation.api.request.CalculatePackagesRequest;
import ru.fastdelivery.presentation.api.request.CargoPackage;
import ru.fastdelivery.presentation.api.request.CoordinatesRequest;
import ru.fastdelivery.presentation.api.response.CalculatePackagesResponse;
import ru.fastdelivery.properties.provider.CoordinatesValidationProvider;  // Изменен импорт
import ru.fastdelivery.usecase.DistanceCalculator;
import ru.fastdelivery.usecase.TariffCalculateUseCase;

@Slf4j
@RestController
@RequestMapping("/api/v1/calculate/")
@RequiredArgsConstructor
@Tag(name = "Расчеты стоимости доставки")
public class CalculateController {
    private final TariffCalculateUseCase tariffCalculateUseCase;
    private final CurrencyFactory currencyFactory;
    private final DistanceCalculator distanceCalculator;
    private final CoordinatesValidationProvider coordinatesValidationProvider;  // Изменен тип

    @PostMapping
    @Operation(summary = "Расчет стоимости по упаковкам груза")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input provided")
    })
    public CalculatePackagesResponse calculate(
            @Valid @RequestBody CalculatePackagesRequest request) {

        log.debug("Received calculation request: {}", request);

        var packs = request.packages().stream()
                .map(this::toPack)
                .toList();

        var shipment = new Shipment(packs, currencyFactory.create(request.currencyCode()));

        Price resultPrice;

        if (request.hasRoute()) {
            validateCoordinates(request.departure());
            validateCoordinates(request.destination());

            double distance = calculateDistance(request.departure(), request.destination());
            log.info("Calculated distance: {} km", distance);

            resultPrice = tariffCalculateUseCase.calculateFullPrice(shipment, distance);
        } else {
            log.debug("No route provided, using base price calculation");
            resultPrice = tariffCalculateUseCase.calc(shipment);
        }

        Price minimalPrice = tariffCalculateUseCase.minimalPrice();

        log.info("Calculation completed: totalPrice={}, minimalPrice={}",
                resultPrice.amount(), minimalPrice.amount());

        return new CalculatePackagesResponse(resultPrice, minimalPrice);
    }

    private Pack toPack(CargoPackage cargoPackage) {
        cargoPackage.validateDimensions();
        var weight = new Weight(cargoPackage.weight());
        if (cargoPackage.hasDimensions()) {
            var dimensions = new Dimensions(
                    cargoPackage.length().intValue(),
                    cargoPackage.width().intValue(),
                    cargoPackage.height().intValue()
            );
            return new Pack(weight, dimensions);
        }
        return new Pack(weight);
    }

    private void validateCoordinates(CoordinatesRequest coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        coordinatesValidationProvider.validate(coordinates);  // Используем интерфейс
    }

    private double calculateDistance(CoordinatesRequest departure, CoordinatesRequest destination) {
        var from = new Coordinates(departure.latitude(), departure.longitude());
        var to = new Coordinates(destination.latitude(), destination.longitude());
        return distanceCalculator.calculateDistance(from, to);
    }
}