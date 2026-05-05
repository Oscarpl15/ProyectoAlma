package com.practicasalma.proyectoalma.util;

import com.practicasalma.proyectoalma.util.config.GestorConfig;
import java.time.LocalDate;

/**
 * Utilidades para calcular el año académico vigente.
 * <p>
 * El sistema distingue dos tipos de año académico:
 * <ul>
 *   <li><b>Alumnos</b>: el nuevo curso comienza en junio
 *       (p. ej., en mayo de 2025 el curso es 2024/2025; en junio ya es 2025/2026).</li>
 *   <li><b>Personal</b> (docentes y voluntarios): el nuevo curso comienza en julio.</li>
 * </ul>
 * El formato de salida es siempre {@code "AAAA/AAAA+1"}, p. ej., {@code "2025/2026"}.
 * </p>
 * <p>
 * Singleton estático — no debe instanciarse.
 * </p>
 */
public class UtilFecha {

    private UtilFecha() {}

    /**
     * Calcula el año académico actual para los alumnos.
     * El año nuevo comienza el 1 de junio.
     *
     * @return año académico en formato {@code "AAAA/AAAA+1"}
     */
    public static String calcularCursoAcademico() {
        LocalDate hoy = LocalDate.now();
        int mes = GestorConfig.getMatriculasMesInicio();
        int dia = GestorConfig.getMatriculasDiaInicio();
        LocalDate inicioNuevoCurso = LocalDate.of(hoy.getYear(), mes, dia);
        int anyo = hoy.getYear();
        if (!hoy.isBefore(inicioNuevoCurso)) {
            return anyo + "/" + (anyo + 1);
        }
        return (anyo - 1) + "/" + anyo;
    }

    /**
     * Calcula el año académico actual para docentes y voluntarios.
     * El año nuevo comienza el 1 de julio.
     *
     * @return año académico en formato {@code "AAAA/AAAA+1"}
     */
    public static String calcularCursoAcademicoPersonal() {
        LocalDate hoy = LocalDate.now();
        int anyoActual = hoy.getYear();
        int mesActual = hoy.getMonthValue();

        if (mesActual >= 7) {
            return anyoActual + "/" + (anyoActual + 1);
        }
        return (anyoActual - 1) + "/" + anyoActual;
    }
}
