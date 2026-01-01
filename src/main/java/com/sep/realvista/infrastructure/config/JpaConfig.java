package com.sep.realvista.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Configuration for auditing support.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}

