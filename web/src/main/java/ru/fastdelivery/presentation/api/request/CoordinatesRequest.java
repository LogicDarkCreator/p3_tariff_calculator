package ru.fastdelivery.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CoordinatesRequest(
        @Schema(description = "Широта", example = "55.446008")
        @NotNull
        BigDecimal latitude,

        @Schema(description = "Долгота", example = "65.339151")
        @NotNull
        BigDecimal longitude
) {
}