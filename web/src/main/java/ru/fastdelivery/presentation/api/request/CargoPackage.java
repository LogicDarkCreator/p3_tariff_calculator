package ru.fastdelivery.presentation.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;

public record CargoPackage(
        @Schema(description = "Вес упаковки, граммы", example = "4564")
        @NotNull
        @Min(0)
        BigInteger weight,

        @Schema(description = "Длина упаковки, мм", example = "345")
        @Min(0)
        @Max(1500)
        BigInteger length,  // Исправлено: BigInteger вместо Integer

        @Schema(description = "Ширина упаковки, мм", example = "589")
        @Min(0)
        @Max(1500)
        BigInteger width,   // Исправлено: BigInteger вместо Integer

        @Schema(description = "Высота упаковки, мм", example = "234")
        @Min(0)
        @Max(1500)
        BigInteger height   // Исправлено: BigInteger вместо Integer
) {
        public boolean hasDimensions() {
                return length != null && width != null && height != null;
        }

        public void validateDimensions() {
                int dimensionsCount = (length != null ? 1 : 0) +
                        (width != null ? 1 : 0) +
                        (height != null ? 1 : 0);
                if (dimensionsCount > 0 && dimensionsCount < 3) {
                        throw new IllegalArgumentException(
                                "If any dimension is provided, all dimensions (length, width, height) must be provided"
                        );
                }
        }
}