package com.clinica.recordatorios.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String pacienteNombre;

    /** Correo o teléfono, según el canal preferido. */
    @Column(nullable = false)
    private String pacienteContacto;

    @Column(nullable = false)
    private LocalDateTime fechaHoraCita;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalNotificacion canalPreferido;

    @Column(nullable = false)
    private boolean recordatorioEnviado;
}
