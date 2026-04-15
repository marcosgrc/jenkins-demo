package es.codeurjc.unit;

import es.codeurjc.model.AccountNumber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class AccountNumberTest {

    @Test
    void shouldCreateValidAccountNumber() {
        // GIVEN
        String validId = "ES0123456789";
        // WHEN
        AccountNumber accountNumber = new AccountNumber(validId);
        // THEN
        assertEquals(validId, accountNumber.getValue());
    }

    @Test
    void shouldThrowExceptionWhenFormatIsWrong() {
        // Por ejemplo, si no empieza por ES
        assertThrows(IllegalArgumentException.class, () -> {
            new AccountNumber("FR1234567890");
        });
    }

    @Test
    void shouldThrowExceptionWhenIsTooShort() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AccountNumber("ES123");
        });
    }

    @Test
    void shouldThrowExceptionWhenIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AccountNumber(null);
        });
    }
}