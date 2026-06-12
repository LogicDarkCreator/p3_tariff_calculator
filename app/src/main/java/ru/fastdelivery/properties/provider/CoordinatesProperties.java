package ru.fastdelivery.properties.provider;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.fastdelivery.presentation.api.request.CoordinatesRequest;

@Component
@ConfigurationProperties("coordinates")
@Setter
public class CoordinatesProperties implements CoordinatesValidationProvider {
    private double minLatitude = 45.0;
    private double maxLatitude = 65.0;
    private double minLongitude = 30.0;
    private double maxLongitude = 96.0;
    private int minDistanceKm = 450;

    @Override
    public void validate(CoordinatesRequest coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        double lat = coordinates.latitude().doubleValue();
        double lon = coordinates.longitude().doubleValue();

        if (lat < minLatitude || lat > maxLatitude) {
            throw new IllegalArgumentException(
                    String.format("Latitude must be between %.1f and %.1f, but got %.4f",
                            minLatitude, maxLatitude, lat)
            );
        }
        if (lon < minLongitude || lon > maxLongitude) {
            throw new IllegalArgumentException(
                    String.format("Longitude must be between %.1f and %.1f, but got %.4f",
                            minLongitude, maxLongitude, lon)
            );
        }
    }

    @Override
    public int getMinDistanceKm() {
        return minDistanceKm;
    }
}