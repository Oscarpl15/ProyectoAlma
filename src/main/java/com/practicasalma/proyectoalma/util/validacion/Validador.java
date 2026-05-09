package com.practicasalma.proyectoalma.util.validacion;

import java.time.LocalDate;

/**
 * Colección de métodos estáticos para validar formatos de datos personales y bancarios.
 * <p>
 * Todos los métodos devuelven {@code false} si el argumento es {@code null}, por lo que
 * es seguro llamarlos sin comprobación previa de nulidad. Las validaciones de DNI/NIE
 * aplican el algoritmo oficial del Ministerio del Interior español.
 * </p>
 */
public class Validador {

    /**
     * Valida un DNI español mediante el algoritmo oficial de módulo 23.
     * <p>
     * Algoritmo: el número de 8 dígitos se divide entre 23; el resto es el índice
     * en la cadena {@code "TRWAGMYFPDXBNJZSQVHLCKE"} que debe coincidir con la letra del DNI.
     * </p>
     *
     * @param dni DNI a validar (admite espacios iniciales/finales y minúsculas)
     * @return {@code true} si el formato y la letra de control son correctos
     */
    public static boolean esDni(String dni) {
        if (dni == null) return false;
        dni = dni.trim().toUpperCase();
        if (!dni.matches("^[0-9]{8}[A-Z]$")) return false;
        int numeros = Integer.parseInt(dni.substring(0, 8));
        String letrasValidas = "TRWAGMYFPDXBNJZSQVHLCKE";
        return dni.charAt(8) == letrasValidas.charAt(numeros % 23);
    }

    /**
     * Valida un NIE español (para extranjeros residentes) mediante el algoritmo oficial.
     * <p>
     * La letra inicial (X, Y o Z) se sustituye por su equivalente numérico
     * (X→0, Y→1, Z→2) y se aplica el mismo algoritmo de módulo 23 que el DNI.
     * </p>
     *
     * @param nie NIE a validar (admite espacios iniciales/finales y minúsculas)
     * @return {@code true} si el formato y la letra de control son correctos
     */
    public static boolean esNie(String nie) {
        if (nie == null) return false;
        nie = nie.trim().toUpperCase();
        if (!nie.matches("^[XYZ][0-9]{7}[A-Z]$")) return false;
        // Sustituye la letra inicial por su dígito equivalente para aplicar la fórmula DNI
        String numStr = nie.substring(0, 8).replace('X', '0').replace('Y', '1').replace('Z', '2');
        int numeros = Integer.parseInt(numStr);
        String letrasValidas = "TRWAGMYFPDXBNJZSQVHLCKE";
        return nie.charAt(8) == letrasValidas.charAt(numeros % 23);
    }

    /**
     * Valida un NIF español de persona jurídica (empresas, asociaciones, fundaciones…).
     * <p>
     * Formato: letra de entidad + 7 dígitos + carácter de control (letra o dígito).
     * El carácter de control se calcula sumando los dígitos en posiciones impares y
     * el doble de los dígitos en posiciones pares (restando 9 si el doble ≥ 10),
     * y aplicando módulo 10 al total. Según la letra inicial, el control es siempre
     * dígito (A, B, E, H), siempre letra (K, P, Q, S) o cualquiera de los dos.
     * </p>
     *
     * @param nif NIF a validar (admite espacios iniciales/finales y minúsculas)
     * @return {@code true} si el formato y el carácter de control son correctos
     */
    public static boolean esNifValido(String nif) {
        if (nif == null) return false;
        String n = nif.trim().toUpperCase();
        if (!n.matches("^[A-Z]\\d{7}[A-Z0-9]$")) return false;

        String digitos = n.substring(1, 8);
        int suma = 0;
        for (int i = 0; i < digitos.length(); i++) {
            int d = digitos.charAt(i) - '0';
            if (i % 2 == 0) {
                int doble = d * 2;
                suma += doble >= 10 ? doble - 9 : doble;
            } else {
                suma += d;
            }
        }
        int digitoControl = (10 - (suma % 10)) % 10;
        char letraControl = "JABCDEFGHI".charAt(digitoControl);
        char ultimo = n.charAt(8);

        char primera = n.charAt(0);
        if ("ABEH".indexOf(primera) >= 0) return ultimo == ('0' + digitoControl);
        if ("KPQS".indexOf(primera) >= 0) return ultimo == letraControl;
        return ultimo == ('0' + digitoControl) || ultimo == letraControl;
    }

    /**
     * Valida un número de teléfono español (9 dígitos, se ignoran los espacios internos).
     *
     * @param telefono teléfono a validar
     * @return {@code true} si contiene exactamente 9 dígitos al eliminar espacios
     */
    public static boolean esTelefonoValido(String telefono) {
        if (telefono == null) return false;
        return telefono.replaceAll("\\s+", "").matches("^[0-9]{9}$");
    }

    /**
     * Valida un código postal español (exactamente 5 dígitos).
     *
     * @param cp código postal a validar
     * @return {@code true} si tiene 5 dígitos
     */
    public static boolean esCodigoPostalValido(String cp) {
        if (cp == null) return false;
        return cp.trim().matches("^[0-9]{5}$");
    }

    /**
     * Valida una dirección de correo electrónico con formato básico RFC.
     *
     * @param email correo a validar
     * @return {@code true} si tiene la forma {@code usuario@dominio.ext}
     */
    public static boolean esEmailValido(String email) {
        if (email == null) return false;
        return email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Valida un IBAN mediante el algoritmo ISO 13616 (módulo 97).
     * <p>
     * Pasos del algoritmo:
     * <ol>
     *   <li>Reordenar: mover los 4 primeros caracteres al final.</li>
     *   <li>Sustituir cada letra por su valor numérico (A=10, B=11, … Z=35).</li>
     *   <li>Calcular el número resultante módulo 97; debe ser igual a 1.</li>
     * </ol>
     * </p>
     *
     * @param iban IBAN a validar (admite espacios entre grupos)
     * @return {@code true} si el IBAN es estructuralmente válido según ISO 13616
     */
    public static boolean esIbanValido(String iban) {
        if (iban == null) return false;
        String normalizado = iban.trim().toUpperCase().replaceAll("\\s+", "");
        if (!normalizado.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$")) return false;

        // Reordenar: los 4 primeros caracteres van al final
        String reordenado = normalizado.substring(4) + normalizado.substring(0, 4);

        // Sustituir letras por números (A=10 … Z=35)
        StringBuilder sb = new StringBuilder();
        for (char c : reordenado.toCharArray()) {
            if (Character.isLetter(c)) sb.append(c - 'A' + 10);
            else sb.append(c);
        }

        java.math.BigInteger valor = new java.math.BigInteger(sb.toString());
        return valor.mod(java.math.BigInteger.valueOf(97)).intValue() == 1;
    }

    /**
     * Valida que una fecha de nacimiento sea coherente: no puede ser futura
     * ni anterior a 120 años desde hoy.
     *
     * @param fecha fecha de nacimiento a validar
     * @return {@code true} si la fecha es pasada y está dentro del rango razonable
     */
    public static boolean esFechaNacimientoValida(LocalDate fecha) {
        if (fecha == null) return false;
        LocalDate hoy = LocalDate.now();
        return fecha.isBefore(hoy) && fecha.isAfter(hoy.minusYears(120));
    }
}
