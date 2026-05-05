package com.practicasalma.proyectoalma.util.validacion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorTest {

    // -----------------------------------------------------------------------
    // esDni
    // -----------------------------------------------------------------------

    @Test
    void esDni_dniValidoRetornaTrue() {
        assertTrue(Validador.esDni("12345678Z"));
    }

    @Test
    void esDni_dniValidoEnMinusculasRetornaTrue() {
        assertTrue(Validador.esDni("12345678z"));
    }

    @Test
    void esDni_letraIncorrectaRetornaFalse() {
        assertFalse(Validador.esDni("12345678A"));
    }

    @Test
    void esDni_nulRetornaFalse() {
        assertFalse(Validador.esDni(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567Z", "123456789Z", "ABCDEFGHZ", "12345678", ""})
    void esDni_formatoInvalidoRetornaFalse(String dni) {
        assertFalse(Validador.esDni(dni));
    }

    // -----------------------------------------------------------------------
    // esNie
    // -----------------------------------------------------------------------

    @Test
    void esNie_nieValidoRetornaTrue() {
        assertTrue(Validador.esNie("X0000000T"));
    }

    @Test
    void esNie_conLetraYRetornaTrue() {
        assertTrue(Validador.esNie("Y0000000Z"));
    }

    @Test
    void esNie_letraIncorrectaRetornaFalse() {
        assertFalse(Validador.esNie("X0000000A"));
    }

    @Test
    void esNie_nulRetornaFalse() {
        assertFalse(Validador.esNie(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"A0000000T", "X000000T", "X00000000T", ""})
    void esNie_formatoInvalidoRetornaFalse(String nie) {
        assertFalse(Validador.esNie(nie));
    }

    // -----------------------------------------------------------------------
    // esTelefonoValido
    // -----------------------------------------------------------------------

    @Test
    void esTelefono_nueveDigitosValido() {
        assertTrue(Validador.esTelefonoValido("612345678"));
    }

    @Test
    void esTelefono_conEspaciosInternosValido() {
        assertTrue(Validador.esTelefonoValido("612 345 678"));
    }

    @Test
    void esTelefono_ochoDigitosInvalido() {
        assertFalse(Validador.esTelefonoValido("12345678"));
    }

    @Test
    void esTelefono_conLetrasInvalido() {
        assertFalse(Validador.esTelefonoValido("6123456AB"));
    }

    @Test
    void esTelefono_nulRetornaFalse() {
        assertFalse(Validador.esTelefonoValido(null));
    }

    // -----------------------------------------------------------------------
    // esCodigoPostalValido
    // -----------------------------------------------------------------------

    @Test
    void esCp_cincoCifrasValido() {
        assertTrue(Validador.esCodigoPostalValido("28001"));
    }

    @Test
    void esCp_cuatroCifrasInvalido() {
        assertFalse(Validador.esCodigoPostalValido("2800"));
    }

    @Test
    void esCp_seisCifrasInvalido() {
        assertFalse(Validador.esCodigoPostalValido("280010"));
    }

    @Test
    void esCp_conLetrasInvalido() {
        assertFalse(Validador.esCodigoPostalValido("2800A"));
    }

    @Test
    void esCp_nulRetornaFalse() {
        assertFalse(Validador.esCodigoPostalValido(null));
    }

    // -----------------------------------------------------------------------
    // esEmailValido
    // -----------------------------------------------------------------------

    @Test
    void esEmail_formatoCorrectovalido() {
        assertTrue(Validador.esEmailValido("usuario@dominio.com"));
    }

    @Test
    void esEmail_sinArrobaInvalido() {
        assertFalse(Validador.esEmailValido("usuariodominio.com"));
    }

    @Test
    void esEmail_sinDominioInvalido() {
        assertFalse(Validador.esEmailValido("usuario@"));
    }

    @Test
    void esEmail_nulRetornaFalse() {
        assertFalse(Validador.esEmailValido(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test@example.org", "nombre.apellido@empresa.es", "a+b@c.io"})
    void esEmail_variosFormatosValidosRetornaTrue(String email) {
        assertTrue(Validador.esEmailValido(email));
    }

    // -----------------------------------------------------------------------
    // esIbanValido
    // -----------------------------------------------------------------------

    @Test
    void esIban_ibanAlemanValidoRetornaTrue() {
        assertTrue(Validador.esIbanValido("DE89370400440532013000"));
    }

    @Test
    void esIban_conEspaciosValidoRetornaTrue() {
        assertTrue(Validador.esIbanValido("DE89 3704 0044 0532 0130 00"));
    }

    @Test
    void esIban_ibanEspanolValidoRetornaTrue() {
        assertTrue(Validador.esIbanValido("ES9121000418450200051332"));
    }

    @Test
    void esIban_digitoControlIncorrectoRetornaFalse() {
        assertFalse(Validador.esIbanValido("DE00370400440532013000"));
    }

    @Test
    void esIban_nulRetornaFalse() {
        assertFalse(Validador.esIbanValido(null));
    }

    @Test
    void esIban_cadenaVaciaRetornaFalse() {
        assertFalse(Validador.esIbanValido(""));
    }

    // -----------------------------------------------------------------------
    // esFechaNacimientoValida
    // -----------------------------------------------------------------------

    @Test
    void esFecha_fechaRazonablePasadaValida() {
        assertTrue(Validador.esFechaNacimientoValida(LocalDate.now().minusYears(10)));
    }

    @Test
    void esFecha_fechaFuturaInvalida() {
        assertFalse(Validador.esFechaNacimientoValida(LocalDate.now().plusDays(1)));
    }

    @Test
    void esFecha_masde120AnosInvalida() {
        assertFalse(Validador.esFechaNacimientoValida(LocalDate.now().minusYears(121)));
    }

    @Test
    void esFecha_exactamente120AnosValida() {
        assertTrue(Validador.esFechaNacimientoValida(LocalDate.now().minusYears(120).plusDays(1)));
    }

    @Test
    void esFecha_hoyInvalida() {
        assertFalse(Validador.esFechaNacimientoValida(LocalDate.now()));
    }

    @Test
    void esFecha_nulRetornaFalse() {
        assertFalse(Validador.esFechaNacimientoValida(null));
    }
}
