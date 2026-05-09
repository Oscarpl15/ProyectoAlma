package com.practicasalma.proyectoalma.service;

import com.practicasalma.proyectoalma.util.config.GestorConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackupServiceTest {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final BackupService service = new BackupService();

    // ── esCopiaDebida ──────────────────────────────────────────────────────────

    @Test
    void esCopiaDebida_frecuenciaCero_retornaFalse() {
        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getBackupFrecuencia).thenReturn(0);
            assertFalse(service.esCopiaDebida());
        }
    }

    @Test
    void esCopiaDebida_frecuenciaPositivaSinFechaPrevia_retornaTrue() {
        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getBackupFrecuencia).thenReturn(7);
            mc.when(GestorConfig::getBackupUltimaFecha).thenReturn(null);
            assertTrue(service.esCopiaDebida());
        }
    }

    @Test
    void esCopiaDebida_frecuenciaPositivaFechaVacia_retornaTrue() {
        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getBackupFrecuencia).thenReturn(7);
            mc.when(GestorConfig::getBackupUltimaFecha).thenReturn("  ");
            assertTrue(service.esCopiaDebida());
        }
    }

    @Test
    void esCopiaDebida_diasExactosCumplidos_retornaTrue() {
        String fechaHaceNDias = LocalDate.now().minusDays(7).format(FORMATO);
        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getBackupFrecuencia).thenReturn(7);
            mc.when(GestorConfig::getBackupUltimaFecha).thenReturn(fechaHaceNDias);
            assertTrue(service.esCopiaDebida());
        }
    }

    @Test
    void esCopiaDebida_diasNoAlcanzados_retornaFalse() {
        String fechaHacePocosDias = LocalDate.now().minusDays(3).format(FORMATO);
        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getBackupFrecuencia).thenReturn(7);
            mc.when(GestorConfig::getBackupUltimaFecha).thenReturn(fechaHacePocosDias);
            assertFalse(service.esCopiaDebida());
        }
    }

    @Test
    void esCopiaDebida_ultimaFechaHoy_retornaFalse() {
        String hoy = LocalDate.now().format(FORMATO);
        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getBackupFrecuencia).thenReturn(7);
            mc.when(GestorConfig::getBackupUltimaFecha).thenReturn(hoy);
            assertFalse(service.esCopiaDebida());
        }
    }

    @Test
    void esCopiaDebida_fechaConFormatoInvalido_retornaTrue() {
        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getBackupFrecuencia).thenReturn(7);
            mc.when(GestorConfig::getBackupUltimaFecha).thenReturn("no-es-una-fecha");
            assertTrue(service.esCopiaDebida());
        }
    }

    // ── realizarCopia ──────────────────────────────────────────────────────────

    @Test
    void realizarCopia_creaCarpetaCopiaSeguridad(@TempDir Path tempDir) throws IOException {
        Path destino = crearDirectorio(tempDir, "backup");
        Path archivoBBDD = crearArchivoBBDD(tempDir);

        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getRutaBBDD).thenReturn(archivoBBDD.toString());
            mc.when(GestorConfig::getRutaDocumentos).thenReturn(null);

            service.realizarCopia(destino.toString());

            String fechaHoy = LocalDate.now().format(FORMATO);
            assertTrue(destino.resolve("CopiaSeguridad_" + fechaHoy).toFile().isDirectory());
        }
    }

    @Test
    void realizarCopia_copiaCarpetaBBDD(@TempDir Path tempDir) throws IOException {
        Path destino = crearDirectorio(tempDir, "backup");
        Path archivoBBDD = crearArchivoBBDD(tempDir);

        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getRutaBBDD).thenReturn(archivoBBDD.toString());
            mc.when(GestorConfig::getRutaDocumentos).thenReturn(null);

            service.realizarCopia(destino.toString());

            String fechaHoy = LocalDate.now().format(FORMATO);
            Path archivoCopiado = destino.resolve("CopiaSeguridad_" + fechaHoy).resolve("datos").resolve("bbdd.db");
            assertTrue(archivoCopiado.toFile().exists());
        }
    }

    @Test
    void realizarCopia_copiaCarpetaDocumentos(@TempDir Path tempDir) throws IOException {
        Path destino = crearDirectorio(tempDir, "backup");
        Path carpetaDocs = crearDirectorio(tempDir, "Documentos");
        Files.createFile(carpetaDocs.resolve("informe.pdf"));

        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getRutaBBDD).thenReturn(null);
            mc.when(GestorConfig::getRutaDocumentos).thenReturn(carpetaDocs.toString());

            service.realizarCopia(destino.toString());

            String fechaHoy = LocalDate.now().format(FORMATO);
            Path docCopiado = destino.resolve("CopiaSeguridad_" + fechaHoy).resolve("Documentos").resolve("informe.pdf");
            assertTrue(docCopiado.toFile().exists());
        }
    }

    @Test
    void realizarCopia_borraCopiaAnterior(@TempDir Path tempDir) throws IOException {
        Path destino = crearDirectorio(tempDir, "backup");
        Path copiaAnterior = destino.resolve("CopiaSeguridad_01.01.2020");
        Files.createDirectories(copiaAnterior);
        Files.createFile(copiaAnterior.resolve("archivo.db"));

        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getRutaBBDD).thenReturn(null);
            mc.when(GestorConfig::getRutaDocumentos).thenReturn(null);

            service.realizarCopia(destino.toString());

            assertFalse(copiaAnterior.toFile().exists());
        }
    }

    @Test
    void realizarCopia_soloQuedaLaCopiaActual(@TempDir Path tempDir) throws IOException {
        Path destino = crearDirectorio(tempDir, "backup");
        Files.createDirectories(destino.resolve("CopiaSeguridad_01.01.2020"));
        Files.createDirectories(destino.resolve("CopiaSeguridad_15.03.2023"));

        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getRutaBBDD).thenReturn(null);
            mc.when(GestorConfig::getRutaDocumentos).thenReturn(null);

            service.realizarCopia(destino.toString());

            File[] copiasResultantes = destino.toFile().listFiles(f -> f.getName().startsWith("CopiaSeguridad_"));
            assertNotNull(copiasResultantes);
            assertEquals(1, copiasResultantes.length);
            assertEquals("CopiaSeguridad_" + LocalDate.now().format(FORMATO), copiasResultantes[0].getName());
        }
    }

    @Test
    void realizarCopia_fuenteBBDDInexistente_noLanzaExcepcion(@TempDir Path tempDir) throws IOException {
        Path destino = crearDirectorio(tempDir, "backup");

        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getRutaBBDD).thenReturn(tempDir.resolve("no_existe").resolve("bbdd.db").toString());
            mc.when(GestorConfig::getRutaDocumentos).thenReturn(null);

            assertDoesNotThrow(() -> service.realizarCopia(destino.toString()));
        }
    }

    @Test
    void realizarCopia_fuenteDocsInexistente_noLanzaExcepcion(@TempDir Path tempDir) throws IOException {
        Path destino = crearDirectorio(tempDir, "backup");

        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            mc.when(GestorConfig::getRutaBBDD).thenReturn(null);
            mc.when(GestorConfig::getRutaDocumentos).thenReturn(tempDir.resolve("no_existe").toString());

            assertDoesNotThrow(() -> service.realizarCopia(destino.toString()));
        }
    }

    // ── registrarFechaHoy ──────────────────────────────────────────────────────

    @Test
    void registrarFechaHoy_guardaFechaDeHoyEnConfig() {
        String fechaEsperada = LocalDate.now().format(FORMATO);
        try (MockedStatic<GestorConfig> mc = mockStatic(GestorConfig.class)) {
            service.registrarFechaHoy();
            mc.verify(() -> GestorConfig.setBackupUltimaFecha(fechaEsperada));
        }
    }

    // ── Auxiliares ────────────────────────────────────────────────────────────

    private Path crearDirectorio(Path base, String nombre) throws IOException {
        Path dir = base.resolve(nombre);
        Files.createDirectories(dir);
        return dir;
    }

    private Path crearArchivoBBDD(Path base) throws IOException {
        Path carpetaBBDD = base.resolve("datos");
        Files.createDirectories(carpetaBBDD);
        Path archivo = carpetaBBDD.resolve("bbdd.db");
        Files.createFile(archivo);
        return archivo;
    }
}
