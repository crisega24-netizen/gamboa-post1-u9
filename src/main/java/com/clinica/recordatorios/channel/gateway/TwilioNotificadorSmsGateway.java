package com.clinica.recordatorios.channel.gateway;

import org.springframework.stereotype.Component;

@Component
public class TwilioNotificadorSmsGateway implements NotificadorSmsGateway {

    @Override
    public void enviarSms(String numeroDestino, String texto) {
        // Simulación de la llamada HTTP al proveedor: el laboratorio no usa credenciales ni red.
        System.out.printf("[SMS a %s] %s%n", numeroDestino, texto);
    }
}
