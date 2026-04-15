package es.codeurjc.model;

/**
 * Clase que representa un monto de dinero 
 * Soluciona el Bad Smell de "Primitive Obsession" al evitar el uso de tipos primitivos (double)
 * para representar conceptos con reglas de negocio específicas.
 */
public class Amount {

    private final double value;

    /**
     * Constructor que valida las reglas de negocio de la aplicación.
     * * @param value El valor numérico del monto.
     * @throws IllegalArgumentException si el monto es negativo o supera los 20.000€.
     */

    public Amount(double value) {
        // Regla Tarea 4: No se permiten cantidades negativas
        if (value < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }

        // Regla Tarea 4: No se permiten transferencias superiores a 20.000€
        if (value > 20000) {
            throw new IllegalArgumentException("La cantidad no puede superar los 20.000€");
        }

        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("%.2f", value);
    }
}