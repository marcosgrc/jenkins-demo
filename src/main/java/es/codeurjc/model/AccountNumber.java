package es.codeurjc.model;

import java.util.Objects;

/**
 * Clase que representa un número de cuenta bancaria 
 * Soluciona el Bad Smell de "Primitive Obsession" al evitar el uso de Strings 
 * para representar conceptos con reglas de negocio específicas.
 */
public class AccountNumber {

    private final String value;

    /**
     * Constructor que valida el formato del número de cuenta.
     * @param value El String con el número de cuenta (ej. ES1234567890).
     * @throws IllegalArgumentException si el formato es nulo o inválido.
     */
    public AccountNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de cuenta no puede estar vacío");
        }

        // Validación de formato: Debe empezar por ES seguido de 10 dígitos (según RandomService)
        if (!value.matches("^ES\\d{10}$")) {
            throw new IllegalArgumentException("Formato de número de cuenta inválido. Debe ser ES seguido de 10 dígitos.");
        }

        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountNumber that = (AccountNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}