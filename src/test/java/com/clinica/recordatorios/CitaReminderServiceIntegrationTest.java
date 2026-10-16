package com.clinica.recordatorios;

import com.clinica.recordatorios.channel.gateway.NotificadorSmsGateway;
import com.clinica.recordatorios.domain.CanalNotificacion;
import com.clinica.recordatorios.domain.Cita;
import com.clinica.recordatorios.repository.CitaRepository;
import com.clinica.recordatorios.service.CitaReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@SuppressWarnings("removal") // @MockBean está deprecado desde Boot 3.4; se usa porque la actividad lo exige
class CitaReminderServiceIntegrationTest {

    @TestConfiguration
    static class ClockDeterministaConfig {

        // Se llama "clockFijo" y no "clock" para no chocar con el bean de ClockConfig:
        // Spring Boot no permite sobrescribir beans con el mismo nombre.
        @Bean
        @Primary
        Clock clockFijo() {
            return Clock.fixed(Instant.parse("2026-08-10T09:00:00Z"), ZoneId.of("UTC"));
        }
    }

    @Autowired
    private CitaReminderService citaReminderService;

    @Autowired
    private CitaRepository citaRepository;

    @MockBean
    private NotificadorSmsGateway notificadorSmsGateway;

    @BeforeEach
    void limpiarBaseDeDatos() {
        citaRepository.deleteAll();
    }

    @Test
    void enviarRecordatoriosPendientes_contextoCompletoDeSpring_persisteYNotificaCitasEnVentana() {
        citaRepository.save(new Cita(null, "Carla Ríos", "3011112222",
                LocalDateTime.of(2026, 8, 10, 18, 0), CanalNotificacion.SMS, false));

        int enviados = citaReminderService.enviarRecordatoriosPendientes();

        assertEquals(1, enviados);
        verify(notificadorSmsGateway, times(1)).enviarSms(eq("3011112222"), anyString());
        assertTrue(citaRepository.findAll().get(0).isRecordatorioEnviado());
    }

    @Test
    void enviarRecordatorioUrgente_contextoCompletoDeSpring_usaElCanalUrgenteDeCanalUrgenteConfig() {
        Cita cita = citaRepository.save(new Cita(null, "Pedro Díaz", "3015556666",
                LocalDateTime.of(2026, 8, 10, 10, 30), CanalNotificacion.EMAIL, false));

        citaReminderService.enviarRecordatorioUrgente(cita.getId());

        verify(notificadorSmsGateway, times(1)).enviarSms(eq("3015556666"), contains("URGENTE"));
        assertTrue(citaRepository.findById(cita.getId()).orElseThrow().isRecordatorioEnviado());
    }
}
