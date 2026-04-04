package com.practicasalma.proyectoalma.util;

// En esta clase se añadiran los validadores de formatos
public class Validador {

    public static boolean esDni(String dni) {

        if (dni == null) {
            return false;
        }

        dni = dni.trim().toUpperCase();

        if (!dni.matches("^[0-9]{8}[A-Z]$")) {
            return false;
        }

        String numerosStr = dni.substring(0, 8);
        char letraProporcionada = dni.charAt(8);

        int numeros = Integer.parseInt(numerosStr);
        String letrasValidas = "TRWAGMYFPDXBNJZSQVHLCKE";
        int indice = numeros % 23;
        char letraCalculada = letrasValidas.charAt(indice);

        return letraProporcionada == letraCalculada;
    }

    public static boolean esNie(String nie) {

        if (nie == null) {
            return false;
        }

        nie = nie.trim().toUpperCase();

        if (!nie.matches("^[XYZ][0-9]{7}[A-Z]$")) {
            return false;
        }

        String numeroParaCalculo = nie.substring(0, 8);
        numeroParaCalculo = numeroParaCalculo.replace('X', '0')
                .replace('Y', '1')
                .replace('Z', '2');

        char letraProporcionada = nie.charAt(8);

        int numeros = Integer.parseInt(numeroParaCalculo);
        String letrasValidas = "TRWAGMYFPDXBNJZSQVHLCKE";
        int indice = numeros % 23;
        char letraCalculada = letrasValidas.charAt(indice);

        return letraProporcionada == letraCalculada;
    }

    public static boolean esTelefonoValido(String telefono) {
        if (telefono == null) return false;
        String soloDigitos = telefono.replaceAll("\\s+", "");
        return soloDigitos.matches("^[0-9]{9}$");
    }

    public static boolean esEmailValido(String email) {

        if (email == null) {
            return false;
        }

        email = email.trim();

        String regexEmail = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        return email.matches(regexEmail);
    }
}
