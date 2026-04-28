package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.model.Socio;
import com.practicasalma.proyectoalma.util.config.GestorConfig;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RecordatorioCorreoService {

    private static final MonthDay FECHA_ENVIO_DEFECTO = MonthDay.of(1, 20);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM");

    public boolean enviarSiCorresponde() {
        LocalDate hoy = LocalDate.now();
        MonthDay fechaEnvio = obtenerFechaConfigurada();
        if (!fechaEnvio.equals(MonthDay.from(hoy))) {
            return false;
        }

        String ultimoAno = GestorConfig.getRecordatorioUltimoAno();
        String anoActual = String.valueOf(hoy.getYear());
        if (anoActual.equals(ultimoAno)) {
            return false;
        }

        List<String> destinatarios = obtenerCorreosSociosActivos();
        if (destinatarios.isEmpty()) {
            return false;
        }

        if (!GestorCorreo.estaConfigurado()) {
            return false;
        }

        String asunto = GestorConfig.getRecordatorioAsunto();
        if (asunto == null || asunto.isBlank()) {
            asunto = "Recordatorio anual";
        }

        String cuerpo = GestorConfig.getRecordatorioCuerpo();
        if (cuerpo == null || cuerpo.isBlank()) {
            cuerpo = "Recordatorio anual del 20 de enero.";
        }

        for (String destinatario : destinatarios) {
            GestorCorreo.mandarEmail(destinatario, asunto, cuerpo);
        }

        GestorConfig.setRecordatorioUltimoAno(anoActual);
        GestorCorreo.limpiarCredenciales();
        return true;
    }

    private MonthDay obtenerFechaConfigurada() {
        String raw = GestorConfig.getRecordatorioFecha();
        if (raw == null || raw.isBlank()) {
            return FECHA_ENVIO_DEFECTO;
        }
        try {
            return MonthDay.parse(raw.trim(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            return FECHA_ENVIO_DEFECTO;
        }
    }

    private List<String> obtenerCorreosSociosActivos() {
        SocioService socioService = new SocioService();
        List<Socio> socios = socioService.obtenerTodos();
        Set<String> correos = new LinkedHashSet<>();
        for (Socio socio : socios) {
            if (socio == null) {
                continue;
            }
            if (!Boolean.TRUE.equals(socio.getActivo())) {
                continue;
            }
            String correo = socio.getCorreo();
            if (correo == null || correo.isBlank()) {
                continue;
            }
            correos.add(correo.trim());
        }
        return new ArrayList<>(correos);
    }
}
