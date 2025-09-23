package com.ase.stammdatenverwaltung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main application class for the Stammdatenverwaltung (Master Data Management) service. This Spring
 * Boot application provides REST APIs for managing master data.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class StammdatenverwaltungApplication {

  /**
   * Main method to start the Spring Boot application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(StammdatenverwaltungApplication.class, args);
  }
}
