package com.ase.stammdatenverwaltung.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enable method-level security except when running with the dev profile.
 *
 * <p>This ensures controllers that use @PreAuthorize or other method annotations are enforced in
 * production and test environments, but not in development.
 */
@Configuration
@Profile("!dev")
@EnableMethodSecurity
public class MethodSecurityConfig {
  // No additional configuration required; annotation triggers method security
}
