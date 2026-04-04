package com.sep.realvista.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures async request handling for SSE / Flux endpoints.
 *
 * <ul>
 *   <li>Replaces the default SimpleAsyncTaskExecutor with a bounded thread pool.</li>
 *   <li>Wraps it with {@link DelegatingSecurityContextAsyncTaskExecutor}
 *       so the SecurityContext (JWT principal) propagates to async dispatch threads.</li>
 * </ul>
 */
@Configuration
public class AsyncConfig implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(securityAsyncExecutor());
        configurer.setDefaultTimeout(120_000L);
    }

    private AsyncTaskExecutor securityAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-mvc-");
        executor.initialize();

        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}
