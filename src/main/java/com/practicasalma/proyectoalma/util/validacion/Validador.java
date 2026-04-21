package com.practicasalma.proyectoalma.util.validacion;

import java.time.LocalDate;

public class Validador {

    public static boolean esDni(String dni) {
        if (dni == null) return false;
        dni = dni.trim().toUpperCase();
        if (!dni.matches("^[0-9]{8}[A-Z]$")) return false;
        int numeros = Integer.parseInt(dni.substring(0, 8));
        String letrasValidas = "TRWAGMYFPDXBNJZSQVHLCKE";
        return dni.charAt(8) == letrasValidas.charAt(numeros % 23);
    }

    public static boolean esNie(String nie) {
        if (nie == null) return false;
        nie = nie.trim().toUpperCase();
        if (!nie.matches("^[XYZ][0-9]{7}[A-Z]$")) return false;
        String numStr = nie.substring(0, 8).replace('X', '0').replace('Y', '1').replace('Z', '2');
        int numeros = Integer.parseInt(numStr);
        String letrasValidas = "TRWAGMYFPDXBNJZSQVHLCKE";
        return nie.charAt(8) == letrasValidas.charAt(numeros % 23);
    }

    public static boolean esTelefonoValido(String telefono) {
        if (telefono == null) return false;
        return telefono.replaceAll("\\s+", "").matches("^[0-9]{9}$");
    }

    public static boolean esCodigoPostalValido(String cp) {
        if (cp == null) return false;
        return cp.trim().matches("^[0-9]{5}$");
    }

    public static boolean esEmailValido(String email) {
        if (email == null) return false;
        return email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean esIbanValido(String iban) {
        if (iban == null) return false;
        String normalizado = iban.trim().toUpperCase().replaceAll("\\s+", "");
        if (!normalizado.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$")) return false;
        String reordenado = normalizado.substring(4) + normalizado.substring(0, 4);
        StringBuilder sb = new StringBuilder();
        for (char c : reordenado.toCharArray()) {
            sb.append(Character.isLetter(c) ? (c - 'A' + 10) : c);
        }
        java.math.BigInteger valor = new java.math.BigInteger(sb.toString());
        return valor.mod(java.math.BigInteger.valueOf(97)).intValue() == 1;
    }

    public static boolean esFechaNacimientoValida(LocalDate fecha) {
        if (fecha == null) return false;
        LocalDate hoy = LocalDate.now();
        return !fecha.isAfter(hoy) && fecha.isAfter(hoy.minusYears(120));
    }
}
