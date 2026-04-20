package com.eventnode.eventnodeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la API EventNode.
 * <p>Habilita el contenedor Spring Boot y la ejecución de tareas programadas
 * ({@link com.eventnode.eventnodeapi.schedulers.EventoScheduler}).</p>
 */
@SpringBootApplication
@EnableScheduling
public class EventnodeApiApplication {

    /**
     * Arranca el contexto Spring (web, seguridad, JPA, mail, etc.) según {@code application.properties}
     * y perfiles activos.
     */
    public static void main(String[] args) {
        SpringApplication.run(EventnodeApiApplication.class, args);
    }

}
