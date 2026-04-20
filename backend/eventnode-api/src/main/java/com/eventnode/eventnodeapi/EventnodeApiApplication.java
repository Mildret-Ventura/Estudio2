package com.eventnode.eventnodeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class
EventnodeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventnodeApiApplication.class, args);
    }

}
