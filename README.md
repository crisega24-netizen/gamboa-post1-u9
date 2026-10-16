# Recordatorios Citas Service

**Post-contenido Unidad 9 — Probar el Diseño: JUnit 5, Mockito, Test Doubles e Integración Continua**
Patrones de Diseño de Software · Ingeniería de Sistemas · UDES
Estudiante: Juan Sebastián Gamboa Cristancho

## Descripción

Microservicio Spring Boot que busca las citas médicas de las próximas 24 horas que aún no han sido notificadas y envía un recordatorio por el canal preferido del paciente (correo o SMS). El servicio central, `CitaReminderServiceImpl`, nace diseñado con dos patrones del curso, ambos inyectados por constructor (DIP):

- **Strategy (Unidad 4):** `ReminderChannel`, con las implementaciones intercambiables `EmailReminderChannel` y `SmsReminderChannel`.
- **Factory Method (Unidad 2):** `ReminderChannelFactory`, que entrega la estrategia correcta según el `CanalNotificacion` de la cita.

## Estructura del proyecto

```
src/main/java/com/clinica/recordatorios/
├── domain/        Cita (entidad JPA), CanalNotificacion (EMAIL, SMS)
├── repository/    CitaRepository (JpaRepository)
├── channel/       ReminderChannel (Strategy) + Email/Sms + ReminderChannelFactory (Factory)
│   └── gateway/   NotificadorSmsGateway + TwilioNotificadorSmsGateway
├── service/       CitaReminderService + CitaReminderServiceImpl
└── config/        ClockConfig (composition root del reloj)
src/test/java/com/clinica/recordatorios/
└── service/       CitaReminderServiceImplTest
```

## Parte 1 — Pruebas unitarias con JUnit 5 y Mockito

`CitaReminderServiceImplTest` aísla `CitaReminderServiceImpl` de sus tres colaboradores. El servicio se construye a mano en `@BeforeEach` con los dobles de prueba, sin levantar Spring, por lo que la suite corre en milisegundos.

| Prueba | Qué verifica |
|---|---|
| `enviarRecordatoriosPendientes_citaEnVentana_usaElCanalPreferido` | La cita EMAIL se envía por el canal entregado por la Factory, se marca como enviada y se guarda una vez |
| `enviarRecordatoriosPendientes_citaConCanalSms_pideALaFactoryElCanalSms` | Con una cita SMS, el servicio pide a la Factory el canal SMS y nunca el EMAIL |
| `enviarRecordatoriosPendientes_ventanaDeterministaSegunClockFijo_consultaLosLimitesExactos` | La ventana consultada es exactamente 10/08/2026 09:00 → 11/08/2026 09:00 gracias al Clock fijo |
| `enviarRecordatoriosPendientes_sinCitasPendientes_noConsultaLaFactory` | Sin citas pendientes no se consulta la Factory ni se guarda nada |

## Parte 2 — Refactor DIP, integración y CI/CD

### Diagnóstico antes del refactor (Paso 7)

La primera versión de `enviarRecordatorioUrgente(Long citaId)` compila y funciona, pero no se puede probar en aislamiento. La línea responsable es la 52 de `CitaReminderServiceImpl`:

```java
ReminderChannel canalUrgente = new SmsReminderChannel(new TwilioNotificadorSmsGateway());
```

El método decide por sí mismo **qué** canal usar y **cómo** construirlo. Como `SmsReminderChannel` y `TwilioNotificadorSmsGateway` se crean con `new` dentro del cuerpo del método, no existe ningún punto (constructor, parámetro o setter) por donde una prueba pueda entregar un doble. Por eso es imposible escribir la prueba del escenario «el gateway de SMS falla y la cita no debe quedar marcada como enviada»: la prueba no puede provocar el fallo y, en cambio, terminaría llamando al gateway real. Interceptar ese `new` con `Mockito.mockConstruction()` sería tratar el síntoma con una herramienta más avanzada, cuando el problema es de diseño: el método depende de clases concretas y no de la abstracción `ReminderChannel` (viola DIP).

El contraste con `enviarRecordatoriosPendientes()` es directo: ese método nunca construye sus colaboradores, los recibe por constructor (`CitaRepository`, `ReminderChannelFactory` y `Clock`). Por eso en la Parte 1 se pudieron reemplazar por un Mock, otro Mock y un Fake sin tocar el código de producción.

## Decisiones de diseño

### Mock, Stub o Fake para cada colaborador (Paso 5)

| Colaborador | Test Double | Justificación |
|---|---|---|
| `CitaRepository` | **Mock** | No basta con que `findByFechaHoraCitaBetweenAndRecordatorioEnviadoFalse` devuelva una lista (eso sería un Stub): la prueba también debe comprobar que `save()` se llama exactamente una vez con la cita ya marcada como enviada, y que no se llama cuando no hay citas. Cuando se necesita controlar la respuesta **y** verificar la interacción, el doble es un Mock. |
| `ReminderChannelFactory` / `ReminderChannel` | **Mock** | El comportamiento bajo prueba es justamente la selección de la estrategia: que el servicio pida el canal que corresponde al `canalPreferido` y que ese canal reciba el mensaje. Ese resultado no es un valor de retorno del servicio, solo se observa verificando la interacción (`verify(reminderChannelFactory).obtenerCanal(SMS)`, `verify(reminderChannelMock).enviar(...)`). |
| `Clock` | **Fake** (no Mock) | `Clock.fixed(...)` es una implementación real y completa de `java.time.Clock` que trae el propio JDK, con el instante congelado. Mockear `Clock` obligaría a stubbear `instant()` y `getZone()` por separado y arriesgar que `LocalDateTime.now(clock)` no se comporte como un reloj real. El Fake es más simple, más legible y fiel al comportamiento de producción, y hace que la ventana de 24 horas sea exacta y repetible en cualquier máquina y a cualquier hora. |

## Ejecutar las pruebas

Requiere JDK 21 y Maven 3.9+ (o el wrapper incluido).

```bash
./mvnw test      # pruebas unitarias
./mvnw verify    # unitarias + reporte y umbral de cobertura JaCoCo (target/site/jacoco/index.html)
```

## Evidencia

| # | Captura |
|---|---|
| 01 | ![Pruebas unitarias en verde](docs/capturas/01-pruebas-unitarias-parte1.png) |
