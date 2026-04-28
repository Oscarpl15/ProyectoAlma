package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.dao.AlumnoDAO;
import com.practicasalma.proyectoalma.dao.DocenteDAO;
import com.practicasalma.proyectoalma.dao.SocioDAO;
import com.practicasalma.proyectoalma.model.Alumno;
import com.practicasalma.proyectoalma.model.Docente;
import com.practicasalma.proyectoalma.model.Donacion;
import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.util.config.GestorBBDD;
import com.practicasalma.proyectoalma.util.config.GestorConfig;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.function.Predicate;

public class DatosDemoService {

    public static void main(String[] args) {
        String rutaBBDD = GestorConfig.getRutaBBDD();
        if (rutaBBDD == null || rutaBBDD.isBlank()) {
            throw new IllegalStateException("No hay base de datos configurada. Configurala primero desde la app.");
        }
        if (!new File(rutaBBDD).exists()) {
            throw new IllegalStateException("No existe la base de datos en la ruta configurada: " + rutaBBDD);
        }

        GestorBBDD.inicializar(rutaBBDD);
        new DatosDemoService().generarDatosDemoParaGraficas();
        System.out.println("Datos demo generados: 20 alumnos, 20 socios y 4 docentes.");
    }

    private static final String[] NOMBRES = {
            "Lucia", "Mateo", "Sofia", "Hugo", "Valeria", "Leo", "Ines", "Pablo", "Nora", "Izan",
            "Carla", "Raul", "Amina", "Youssef", "Lina", "Thiago", "Elena", "Sergio", "Noa", "Adrian"
    };

    private static final String[] APELLIDOS = {
            "Garcia", "Lopez", "Martinez", "Sanchez", "Fernandez", "Perez", "Gomez", "Ruiz", "Diaz", "Moreno",
            "Jimenez", "Romero", "Navarro", "Torres", "Vega", "Molina", "Castro", "Ortega", "Serrano", "Ramos"
    };

    private static final String[] NACIONALIDADES = {
            "Espanola", "Marroqui", "Colombiana", "Rumana", "Venezolana", "Ucraniana"
    };

    private static final String[] GENEROS = {
            "Femenino", "Masculino", "No binario"
    };

    private static final String[] CURSOS = {
            "1º Infantil", "2º Infantil", "3º Infantil",
            "1º Primaria", "2º Primaria", "3º Primaria", "3º Primaria",
            "4º Primaria", "4º Primaria", "5º Primaria", "6º Primaria"
    };

    private static final String[] GRUPOS = {
            "g1 lunes/miercoles", "g2 lunes/miercoles", "g1 martes/jueves", "g2 martes/jueves"
    };

    private static final String[] TIPOS_SOCIO = {
            "Física", "Física", "Empresa", "Asociación", "Física", "Empresa"
    };

    private static final String[] PERIODICIDADES = {
            "Mensual", "Mensual", "Trimestral", "Anual", "Puntual", "Mensual"
    };

    private static final String[] TITULACIONES = {
            "Magisterio Primaria", "Educacion Social", "Pedagogia", "Psicologia"
    };

    private final AlumnoService alumnoService = new AlumnoService();
    private final SocioService socioService = new SocioService();
    private final DocenteService docenteService = new DocenteService();

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();
    private final SocioDAO socioDAO = new SocioDAO();
    private final DocenteDAO docenteDAO = new DocenteDAO();

    public void generarDatosDemoParaGraficas() {
        generarDatosDemo(20, 20, 4);
    }

    public void generarDatosDemo(int totalAlumnos, int totalSocios, int totalDocentes) {
        if (totalAlumnos < 0 || totalSocios < 0 || totalDocentes < 0) {
            throw new IllegalArgumentException("Las cantidades no pueden ser negativas.");
        }

        generarAlumnos(totalAlumnos);
        generarSocios(totalSocios);
        generarDocentes(totalDocentes);
    }

    private void generarAlumnos(int total) {
        int base = semillaInicial(101);
        for (int i = 0; i < total; i++) {
            Alumno alumno = new Alumno(
                    nombreDesdeIndice(i),
                    apellidosDesdeIndice(i),
                    "Calle Escuela " + (10 + i),
                    LocalDate.now().minusYears(7 + (i % 7)).minusDays(i * 9L)
            );

            alumno.setDocumentoIdentidad(generarDniUnico(alumnoDAO::existeDni, base + i));
            alumno.setTelefono(generarTelefono(i));
            alumno.setColegio("Colegio " + (char) ('A' + (i % 5)));
            alumno.setNacionalidad(NACIONALIDADES[i % NACIONALIDADES.length]);
            alumno.setGenero(GENEROS[i % GENEROS.length]);
            alumno.setCiudad("Madrid");
            alumno.setCodigoPostal(String.format("28%03d", 10 + i));

            alumno.setAutorizaUsoDatos(i % 2 == 0);
            alumno.setAutorizaActividades(i % 3 != 0);
            alumno.setAutorizaComunicaciones(i % 4 != 0);
            alumno.setAutorizaImagen(i % 5 != 0);
            alumno.setAutorizaIrseSolo(i % 3 == 0);

            alumno.setSeguimientoServiciosSociales(i % 4 == 0);
            alumno.setSeguimientoSaf(i % 5 == 0);
            alumno.setDerivacionSS(i % 6 == 0);
            alumno.setDerivacionSaf(i % 7 == 0);
            alumno.setDerivacionColegio(i % 3 == 0);
            alumno.setDerivacionEoep(i % 8 == 0);

            String curso = CURSOS[i % CURSOS.length];
            String grupo = GRUPOS[i % GRUPOS.length];
            alumnoService.matricularNuevoAlumno(alumno, curso, grupo);
        }
    }

    private void generarSocios(int total) {
        int base = semillaInicial(202);
        int anyoActual = LocalDate.now().getYear();

        for (int i = 0; i < total; i++) {
            String tipo = TIPOS_SOCIO[i % TIPOS_SOCIO.length];
            String periodicidad = PERIODICIDADES[i % PERIODICIDADES.length];
            double cuota = 10.0 + (i % 6) * 5.0;

            Socio socio = new Socio(
                    nombreDesdeIndice(i + 50),
                    apellidosDesdeIndice(i + 50),
                    "Avenida Solidaria " + (20 + i),
                    generarDniUnico(socioDAO::existeDni, base + i),
                    tipo,
                    cuota,
                    periodicidad
            );

            socio.setTelefono(generarTelefono(i + 100));
            socio.setCorreo("socio.demo" + i + "@mail.com");
            socio.setNacionalidad(NACIONALIDADES[i % NACIONALIDADES.length]);
            socio.setGenero(GENEROS[i % GENEROS.length]);
            socio.setCiudad("Madrid");
            socio.setCodigoPostal(String.format("28%03d", 120 + i));
            socio.setActivo(i % 6 != 0);

            int donaciones = 1 + (i % 3);
            for (int d = 0; d < donaciones; d++) {
                Month mes = Month.of(((i * 2 + d * 3) % 12) + 1);
                LocalDate fecha = LocalDate.of(anyoActual, mes, Math.min(25, 5 + d * 7));
                BigDecimal importe = BigDecimal.valueOf(15 + (i % 8) * 10L + d * 5L);

                Donacion donacion = new Donacion(fecha, importe, "Puntual".equals(periodicidad), socio);
                donacion.setFormaDonacion(i % 2 == 0 ? "Transferencia" : "Efectivo");
                socio.addDonacion(donacion);
            }

            if (i % 4 == 0) {
                Donacion donacionAnterior = new Donacion(
                        LocalDate.of(anyoActual - 1, Month.NOVEMBER, 15),
                        BigDecimal.valueOf(25 + i),
                        true,
                        socio
                );
                donacionAnterior.setFormaDonacion("Transferencia");
                socio.addDonacion(donacionAnterior);
            }

            socioService.guardarSocio(socio);
        }
    }

    private void generarDocentes(int total) {
        int base = semillaInicial(303);

        for (int i = 0; i < total; i++) {
            Docente docente = new Docente(
                    nombreDesdeIndice(i + 90),
                    apellidosDesdeIndice(i + 90),
                    "Calle Docencia " + (30 + i),
                    generarDniUnico(docenteDAO::existeDni, base + i),
                    generarTelefono(i + 200),
                    "docente.demo" + i + "@mail.com",
                    LocalDate.now().minusYears(26 + i).minusDays(i * 31L)
            );

            docente.setNacionalidad(NACIONALIDADES[i % NACIONALIDADES.length]);
            docente.setGenero(GENEROS[i % GENEROS.length]);
            docente.setCiudad("Madrid");
            docente.setCodigoPostal(String.format("28%03d", 220 + i));
            docente.setTitulacion(TITULACIONES[i % TITULACIONES.length]);
            docente.setAutoDelitosSexuales(i % 2 == 0);
            docente.setAutoProteccionDatos(true);
            docente.setActivo(i % 4 != 0);

            docenteService.guardarDocente(docente);
        }
    }

    private int semillaInicial(int offset) {
        long valor = System.currentTimeMillis() + offset;
        int base = (int) (Math.abs(valor) % 80000000L);
        return 10000000 + base;
    }

    private String generarDniUnico(Predicate<String> existeDni, int semilla) {
        int candidato = Math.max(10000000, semilla);
        if (candidato > 99999999) {
            candidato = 10000000 + (candidato % 90000000);
        }

        for (int intentos = 0; intentos < 90000000; intentos++) {
            String dni = construirDni(candidato);
            if (!existeDni.test(dni)) {
                return dni;
            }
            candidato++;
            if (candidato > 99999999) {
                candidato = 10000000;
            }
        }

        throw new IllegalStateException("No se pudo generar un DNI unico.");
    }

    private String construirDni(int numero) {
        String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        char letra = letras.charAt(numero % 23);
        return String.format("%08d%c", numero, letra);
    }

    private String nombreDesdeIndice(int i) {
        return NOMBRES[Math.floorMod(i, NOMBRES.length)];
    }

    private String apellidosDesdeIndice(int i) {
        String primero = APELLIDOS[Math.floorMod(i, APELLIDOS.length)];
        String segundo = APELLIDOS[Math.floorMod(i + 7, APELLIDOS.length)];
        return primero + " " + segundo;
    }

    private String generarTelefono(int i) {
        int base = 600000000 + (Math.floorMod(i, 99999));
        return String.valueOf(base);
    }
}
