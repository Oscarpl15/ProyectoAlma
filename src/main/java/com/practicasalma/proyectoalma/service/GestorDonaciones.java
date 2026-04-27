package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.model.Donacion;
import com.practicasalma.proyectoalma.model.Socio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Genera automáticamente las donaciones periódicas pendientes al arrancar la aplicación.
 * Solo actúa sobre socios activos con periodicidad regular (Mensual, Trimestral, Anual)
 * y cuota mayor que cero.
 */
public class GestorDonaciones {

    private static final List<String> PERIODICIDADES_REGULARES = List.of("Mensual", "Trimestral", "Anual");

    public static void generarDonacionesPendientes() {
        SocioService socioService = new SocioService();
        List<Socio> socios;
        try {
            socios = socioService.obtenerTodosConDonaciones();
        } catch (Exception e) {
            System.err.println("GestorDonaciones: error al cargar socios: " + e.getMessage());
            return;
        }

        LocalDate hoy = LocalDate.now();

        for (Socio socio : socios) {
            if (!Boolean.TRUE.equals(socio.getActivo())) continue;
            if (!PERIODICIDADES_REGULARES.contains(socio.getPeriodicidad())) continue;
            if (socio.getCuota() == null || socio.getCuota() <= 0) continue;
            if (socio.getDonaciones() == null || socio.getDonaciones().isEmpty()) continue;

            LocalDate ultimaFecha = socio.getDonaciones().stream()
                    .filter(d -> d.getFecha() != null)
                    .map(Donacion::getFecha)
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            if (ultimaFecha == null) continue;

            LocalDate proxima = calcularProximaFecha(ultimaFecha, socio.getPeriodicidad());
            boolean tienePendientes = false;

            while (!proxima.isAfter(hoy)) {
                Donacion d = new Donacion(proxima, BigDecimal.valueOf(socio.getCuota()), socio.getPeriodicidad(), socio);
                d.setFormaDonacion(socio.getFormaPago());
                socio.addDonacion(d);
                proxima = calcularProximaFecha(proxima, socio.getPeriodicidad());
                tienePendientes = true;
            }

            if (tienePendientes) {
                try {
                    socioService.actualizarSocio(socio);
                } catch (Exception e) {
                    System.err.println("GestorDonaciones: error al actualizar socio " + socio.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    private static LocalDate calcularProximaFecha(LocalDate ultima, String periodicidad) {
        return switch (periodicidad) {
            case "Mensual" -> ultima.plusMonths(1);
            case "Trimestral" -> ultima.plusMonths(3);
            case "Anual" -> ultima.plusYears(1);
            default -> ultima.plusMonths(1);
        };
    }
}
