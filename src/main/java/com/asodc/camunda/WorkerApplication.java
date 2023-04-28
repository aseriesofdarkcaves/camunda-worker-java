package com.asodc.camunda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry-point for the application.
 * I didn't use @EnableZeebeClient as it's deprecated and apparently already implicit.
 */
@SpringBootApplication
public class WorkerApplication {
    /**
     * Main method.
     *
     * @param args arguments to pass to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }
}
