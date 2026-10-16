package com.clinica.recordatorios.channel;

import com.clinica.recordatorios.domain.CanalNotificacion;

/**
 * Factory Method: decide en tiempo de ejecución qué Strategy entregar según el canal preferido.
 */
public interface ReminderChannelFactory {

    ReminderChannel obtenerCanal(CanalNotificacion tipo);
}
