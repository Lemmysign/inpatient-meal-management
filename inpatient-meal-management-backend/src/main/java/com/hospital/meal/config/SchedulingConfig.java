package com.hospital.meal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class SchedulingConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

        // Pool size for scheduled tasks
        taskScheduler.setPoolSize(3);

        // Thread name prefix
        taskScheduler.setThreadNamePrefix("scheduled-");

        // Remove tasks from queue on cancel
        taskScheduler.setRemoveOnCancelPolicy(true);

        // Wait for tasks to complete on shutdown
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.setAwaitTerminationSeconds(60);

        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
    }
}