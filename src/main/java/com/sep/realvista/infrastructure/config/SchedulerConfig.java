package com.sep.realvista.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    /**
     * Task scheduler with a dedicated pool used by all {@code @Scheduled} beans.
     *
     * <ul>
     *   <li>Pool size: 2 – enough for the current set of scheduled tasks plus headroom.</li>
     *   <li>Thread name prefix: {@code scheduler-} – makes log diagnosis easy.</li>
     * </ul>
     *
     * @return configured {@link TaskScheduler}
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }
}
