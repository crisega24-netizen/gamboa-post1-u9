package com.clinica.recordatorios.channel;

import com.clinica.recordatorios.domain.Cita;

/**
 * Strategy: cada canal de envío es una implementación intercambiable con la misma firma.
 */
public interface ReminderChannel {

    void enviar(Cita cita, String mensaje);
}
