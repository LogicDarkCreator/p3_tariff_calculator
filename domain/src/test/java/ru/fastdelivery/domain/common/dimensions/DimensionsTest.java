package ru.fastdelivery.domain.common.dimensions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DimensionsTest {

    @Test
    void shouldCreateDimensions() {
        var dimensions = new Dimensions(345, 589, 234);
        assertThat(dimensions.length()).isEqualTo(345);
        assertThat(dimensions.width()).isEqualTo(589);
        assertThat(dimensions.height()).isEqualTo(234);
    }

    @Test
    void shouldThrowExceptionWhenNegative() {
        assertThatThrownBy(() -> new Dimensions(-1, 100, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("length cannot be below Zero!");
    }

    @Test
    void shouldThrowExceptionWhenExceeds1500() {
        assertThatThrownBy(() -> new Dimensions(1501, 100, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("length cannot be more than 1500 mm!");
    }

    @Test
    void shouldNormalizeDimensions() {
        var dimensions = new Dimensions(345, 589, 234);
        var normalized = dimensions.normalize();

        assertThat(normalized.length()).isEqualTo(350);
        assertThat(normalized.width()).isEqualTo(600);
        assertThat(normalized.height()).isEqualTo(250);
    }

    @ParameterizedTest
    @CsvSource({
            "300, 200, 100, 0.0060",
            "350, 600, 250, 0.0525",
            "500, 500, 500, 0.1250"
    })
    void shouldCalculateVolumeCorrectly(int length, int width, int height, String expectedVolume) {
        var dimensions = new Dimensions(length, width, height);
        var volume = dimensions.volumeInCubicMeters();

        assertThat(volume).isEqualByComparingTo(new BigDecimal(expectedVolume));
    }
}