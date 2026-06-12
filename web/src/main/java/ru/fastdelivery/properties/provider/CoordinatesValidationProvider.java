package ru.fastdelivery.properties.provider;

import ru.fastdelivery.presentation.api.request.CoordinatesRequest;

public interface CoordinatesValidationProvider {
    void validate(CoordinatesRequest coordinates);
    int getMinDistanceKm();
}