package com.clinica.recordatorios.config;

import com.clinica.recordatorios.channel.ReminderChannel;
import com.clinica.recordatorios.channel.SmsReminderChannel;
import com.clinica.recordatorios.channel.gateway.NotificadorSmsGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root del canal urgente: el único "new" de este canal vive aquí,
 * fuera de la lógica de negocio del servicio.
 */
@Configuration
public class CanalUrgenteConfig {

    @Bean
    public ReminderChannel canalUrgente(NotificadorSmsGateway gateway) {
        return new SmsReminderChannel(gateway);
    }
}
