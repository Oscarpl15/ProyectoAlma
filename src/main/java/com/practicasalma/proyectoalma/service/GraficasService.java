package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Donacion;
import com.practicasalma.proyectoalma.model.Matricula;
import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.util.UtilFecha;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

public class GraficasService {

    private static final Locale LOCALE_ES = new Locale("es", "ES");

    private final AlumnoService alumnoService = new AlumnoService();
    private final SocioService socioService = new SocioService();

    public List<String> obtenerAnosAcademicosAlumnos() {
        TreeSet<String> anos = new TreeSet<>(Comparator.reverseOrder());

        for (Alumno alumno : alumnoService.obtenerTodos()) {
            if (alumno.getMatriculas() == null) {
                continue;
            }
            for (Matricula matricula : alumno.getMatriculas()) {
                if (matricula.getAnyoAcademico() != null && !matricula.getAnyoAcademico().isBlank()) {
                    anos.add(matricula.getAnyoAcademico().trim());
                }
            }
        }

        anos.add(UtilFecha.calcularCursoAcademico());
        return new ArrayList<>(anos);
    }

    public List<String> obtenerAnosSocios() {
        TreeSet<String> anos = new TreeSet<>(Comparator.reverseOrder());

        for (Socio socio : socioService.obtenerTodosConDonaciones()) {
            if (socio.getFechaAlta() != null) {
                anos.add(String.valueOf(socio.getFechaAlta().getYear()));
            }
            if (socio.getDonaciones() == null) {
                continue;
            }
            for (Donacion donacion : socio.getDonaciones()) {
                if (donacion.getFecha() != null) {
                    anos.add(String.valueOf(donacion.getFecha().getYear()));
                }
            }
        }

        anos.add(String.valueOf(java.time.LocalDate.now().getYear()));
        return new ArrayList<>(anos);
    }

    public ResultadoGrafica generarGraficaAlumnos(String tipoDato, String anoAcademico) {
        List<Alumno> alumnos = alumnoService.obtenerTodos();

        Map<String, Integer> conteos = new HashMap<>();
        int totalAlumnosFiltrados = 0;

        if ("CURSO".equals(tipoDato)) {
            for (Alumno alumno : alumnos) {
                if (alumno.getMatriculas() == null) {
                    continue;
                }
                for (Matricula matricula : alumno.getMatriculas()) {
                    if (!Objects.equals(normalizar(matricula.getAnyoAcademico()), normalizar(anoAcademico))) {
                        continue;
                    }
                    String clave = valorOMarcaVacio(matricula.getCurso(), "Sin curso");
                    conteos.merge(clave, 1, Integer::sum);
                    totalAlumnosFiltrados++;
                }
            }
        } else {
            for (Alumno alumno : alumnos) {
                if (!estaMatriculadoEnAno(alumno, anoAcademico)) {
                    continue;
                }

                String clave;
                if ("NACIONALIDAD".equals(tipoDato)) {
                    clave = valorOMarcaVacio(alumno.getNacionalidad(), "Sin nacionalidad");
                } else {
                    clave = valorOMarcaVacio(alumno.getGenero(), "Sin genero");
                }

                conteos.merge(clave, 1, Integer::sum);
                totalAlumnosFiltrados++;
            }
        }

        LinkedHashMap<String, Number> valores = ordenarPorValorDesc(conteos);

        List<String> resumen = construirResumenAlumnos(tipoDato, valores, totalAlumnosFiltrados);
        return new ResultadoGrafica(valores, resumen, "Alumnos - " + tipoDato + " (" + anoAcademico + ")");
    }

    public ResultadoGrafica generarGraficaSocios(String ano, String tipoSocio, String periodicidad) {
        int anoNumerico;
        try {
            anoNumerico = Integer.parseInt(ano);
        } catch (Exception e) {
            anoNumerico = java.time.LocalDate.now().getYear();
        }

        LinkedHashMap<String, Number> ingresosPorMes = inicializarMesesEnCero();
        List<Socio> socios = socioService.obtenerTodosConDonaciones();

        BigDecimal totalDonado = BigDecimal.ZERO;
        int sociosFiltrados = 0;
        int donacionesAno = 0;

        for (Socio socio : socios) {
            if (!cumpleTipoSocio(socio, tipoSocio) || !cumplePeriodicidad(socio, periodicidad)) {
                continue;
            }

            sociosFiltrados++;

            if ((socio.getDonaciones() == null || socio.getDonaciones().isEmpty())
                    && socio.getCuota() != null
                    && socio.getCuota() > 0
                    && socio.getFechaAlta() != null
                    && socio.getFechaAlta().getYear() <= anoNumerico) {

                String periodicidadSocio = normalizar(socio.getPeriodicidad());
                BigDecimal cuota = BigDecimal.valueOf(socio.getCuota());

                if ("mensual".equals(periodicidadSocio)) {
                    for (Month month : Month.values()) {
                        String mes = nombreMesAbreviado(month);
                        double actual = ingresosPorMes.get(mes).doubleValue();
                        ingresosPorMes.put(mes, actual + cuota.doubleValue());
                        totalDonado = totalDonado.add(cuota);
                        donacionesAno++;
                    }
                } else if ("trimestral".equals(periodicidadSocio)) {
                    int[] meses = {1, 4, 7, 10};
                    for (int mesNumero : meses) {
                        String mes = nombreMesAbreviado(Month.of(mesNumero));
                        double actual = ingresosPorMes.get(mes).doubleValue();
                        ingresosPorMes.put(mes, actual + cuota.doubleValue());
                        totalDonado = totalDonado.add(cuota);
                        donacionesAno++;
                    }
                } else if ("anual".equals(periodicidadSocio)) {
                    String mes = nombreMesAbreviado(Month.JANUARY);
                    double actual = ingresosPorMes.get(mes).doubleValue();
                    ingresosPorMes.put(mes, actual + cuota.doubleValue());
                    totalDonado = totalDonado.add(cuota);
                    donacionesAno++;
                } else if ("puntual".equals(periodicidadSocio)) {
                    String mes = nombreMesAbreviado(Month.JANUARY);
                    double actual = ingresosPorMes.get(mes).doubleValue();
                    ingresosPorMes.put(mes, actual + cuota.doubleValue());
                    totalDonado = totalDonado.add(cuota);
                    donacionesAno++;
                }
            }

            if (socio.getDonaciones() == null) {
                continue;
            }

            for (Donacion donacion : socio.getDonaciones()) {
                if (donacion.getFecha() == null || donacion.getImporte() == null) {
                    continue;
                }
                if (donacion.getFecha().getYear() != anoNumerico) {
                    continue;
                }

                String mes = nombreMesAbreviado(donacion.getFecha().getMonth());
                double actual = ingresosPorMes.get(mes).doubleValue();
                double nuevo = actual + donacion.getImporte().doubleValue();
                ingresosPorMes.put(mes, nuevo);

                totalDonado = totalDonado.add(donacion.getImporte());
                donacionesAno++;
            }
        }

        List<String> resumen = new ArrayList<>();
        resumen.add("Total dinero donado: " + totalDonado.stripTrailingZeros().toPlainString() + " EUR");
        resumen.add("Socios filtrados: " + sociosFiltrados);
        resumen.add("Donaciones del ano: " + donacionesAno);

        return new ResultadoGrafica(ingresosPorMes, resumen, "Socios - Ingresos por mes (" + anoNumerico + ")");
    }

    private List<String> construirResumenAlumnos(String tipoDato, LinkedHashMap<String, Number> valores, int total) {
        List<String> resumen = new ArrayList<>();
        resumen.add("Total alumnos: " + total);

        if (valores.isEmpty()) {
            resumen.add("Sin datos para el filtro seleccionado.");
            resumen.add("-");
            return resumen;
        }

        if ("GENERO".equals(tipoDato)) {
            int masculinos = obtenerValorGenero(valores, "masculino", "varon", "hombre");
            int femeninos = obtenerValorGenero(valores, "femenino", "mujer");
            resumen.add("Varones: " + masculinos + " | Mujeres: " + femeninos);
        } else {
            Map.Entry<String, Number> primero = valores.entrySet().iterator().next();
            resumen.add(primero.getKey() + ": " + primero.getValue().intValue());
        }

        StringBuilder resto = new StringBuilder();
        int index = 0;
        for (Map.Entry<String, Number> entry : valores.entrySet()) {
            if (index >= 2) {
                break;
            }
            if (index > 0) {
                resto.append(" | ");
            }
            resto.append(entry.getKey()).append(": ").append(entry.getValue().intValue());
            index++;
        }
        resumen.add(resto.toString());
        return resumen;
    }

    private int obtenerValorGenero(Map<String, Number> valores, String... aliases) {
        int total = 0;
        for (Map.Entry<String, Number> entry : valores.entrySet()) {
            String key = normalizar(entry.getKey());
            for (String alias : aliases) {
                if (key.contains(alias)) {
                    total += entry.getValue().intValue();
                    break;
                }
            }
        }
        return total;
    }

    private boolean estaMatriculadoEnAno(Alumno alumno, String anoAcademico) {
        if (alumno.getMatriculas() == null) {
            return false;
        }
        for (Matricula matricula : alumno.getMatriculas()) {
            if (Objects.equals(normalizar(matricula.getAnyoAcademico()), normalizar(anoAcademico))) {
                return true;
            }
        }
        return false;
    }

    private boolean cumpleTipoSocio(Socio socio, String tipoSocio) {
        if (tipoSocio == null || "TODOS".equalsIgnoreCase(tipoSocio)) {
            return true;
        }
        return normalizar(socio.getTipoEntidad()).equals(normalizar(tipoSocio));
    }

    private boolean cumplePeriodicidad(Socio socio, String periodicidad) {
        if (periodicidad == null || "TODAS".equalsIgnoreCase(periodicidad)) {
            return true;
        }
        return normalizar(socio.getPeriodicidad()).equals(normalizar(periodicidad));
    }

    private LinkedHashMap<String, Number> inicializarMesesEnCero() {
        LinkedHashMap<String, Number> mapa = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            mapa.put(nombreMesAbreviado(month), 0d);
        }
        return mapa;
    }

    private String nombreMesAbreviado(Month month) {
        String txt = month.getDisplayName(TextStyle.SHORT, LOCALE_ES);
        txt = txt.replace(".", "");
        return txt.substring(0, 1).toUpperCase(LOCALE_ES) + txt.substring(1).toLowerCase(LOCALE_ES);
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim().toLowerCase(LOCALE_ES);
    }

    private String valorOMarcaVacio(String valor, String defecto) {
        if (valor == null || valor.trim().isEmpty()) {
            return defecto;
        }
        return valor.trim();
    }

    private LinkedHashMap<String, Number> ordenarPorValorDesc(Map<String, Integer> conteos) {
        return conteos.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .collect(LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        LinkedHashMap::putAll);
    }

    public static class ResultadoGrafica {
        private final LinkedHashMap<String, Number> valores;
        private final List<String> resumen;
        private final String titulo;

        public ResultadoGrafica(LinkedHashMap<String, Number> valores, List<String> resumen, String titulo) {
            this.valores = valores;
            this.resumen = resumen;
            this.titulo = titulo;
        }

        public LinkedHashMap<String, Number> getValores() {
            return valores;
        }

        public List<String> getResumen() {
            return resumen;
        }

        public String getTitulo() {
            return titulo;
        }
    }
}
