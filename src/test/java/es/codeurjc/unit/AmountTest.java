package es.codeurjc.unit;

import es.codeurjc.model.Amount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class AmountTest {

    @Test
    void shouldCreateValidAmount() {
        // GIVEN
        double validValue = 100.0;
        // WHEN
        Amount amount = new Amount(validValue);
        // THEN
        assertEquals(validValue, amount.getValue());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Amount(0.0);
        });
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Amount(-50.0);
        });
        assertEquals("La cantidad no puede ser negativa", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAmountExceedsLimit() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Amount(25000.0);
        });
        assertEquals("La cantidad no puede superar los 20.000€", exception.getMessage());
    }
}