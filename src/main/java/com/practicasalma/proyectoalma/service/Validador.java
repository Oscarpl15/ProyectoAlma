package com.practicasalma.proyectoalma.service;

// En esta clase se añadiran los validadores de formatos
public class Validador {

    public boolean esDni(String dni) {

        if (dni == null) {
            return false;
        }

        dni.trim().toUpperCase();

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
}
