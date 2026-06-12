package ru.fastdelivery.usecase.distance;

import org.springframework.stereotype.Component;
import ru.fastdelivery.domain.common.coordinates.Coordinates;
import ru.fastdelivery.usecase.DistanceCalculator;

@Component
public class HaversineDistanceCalculator implements DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public double calculateDistance(Coordinates from, Coordinates to) {
        double lat1 = Math.toRadians(from.latitudeAsDouble());
        double lon1 = Math.toRadians(from.longitudeAsDouble());
        double lat2 = Math.toRadians(to.latitudeAsDouble());
        double lon2 = Math.toRadians(to.longitudeAsDouble());

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dlon / 2) * Math.sin(dlon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}