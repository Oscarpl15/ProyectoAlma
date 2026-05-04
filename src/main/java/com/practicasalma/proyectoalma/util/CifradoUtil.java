package com.practicasalma.proyectoalma.util;

import com.practicasalma.proyectoalma.exception.ConfiguracionException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utilidad de cifrado AES-128 para datos sensibles almacenados en BD.
 * <p>
 * Usa una clave fija interna. Protege contra acceso directo al fichero .db
 * sin acceso al código fuente. No debe instanciarse.
 * </p>
 */
public class CifradoUtil {

    // 16 bytes exactos requeridos por AES-128
    private static final byte[] CLAVE = "AlmaFund2024Key!".getBytes(StandardCharsets.UTF_8);
    private static final String ALGORITMO = "AES";

    private CifradoUtil() {}

    /**
     * Cifra un texto plano con AES-128 y lo devuelve en Base64.
     *
     * @param texto texto a cifrar
     * @return texto cifrado en Base64, o cadena vacía si la entrada es nula o vacía
     */
    public static String cifrar(String texto) {
        if (texto == null || texto.isBlank()) return "";
        try {
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(CLAVE, ALGORITMO));
            return Base64.getEncoder().encodeToString(cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new ConfiguracionException("Error al cifrar los datos de configuración.", e);
        }
    }

    /**
     * Descifra un texto cifrado con AES-128 en Base64.
     *
     * @param textoCifrado texto en Base64 a descifrar
     * @return texto original, o cadena vacía si la entrada es nula, vacía o no descifrable
     */
    public static String descifrar(String textoCifrado) {
        if (textoCifrado == null || textoCifrado.isBlank()) return "";
        try {
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(CLAVE, ALGORITMO));
            return new String(cipher.doFinal(Base64.getDecoder().decode(textoCifrado)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
