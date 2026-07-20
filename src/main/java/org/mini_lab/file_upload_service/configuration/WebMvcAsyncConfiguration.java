package org.mini_lab.file_upload_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcAsyncConfiguration implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(
            AsyncSupportConfigurer configurer
    ) {
        configurer.setTaskExecutor(streamingTaskExecutor());
        configurer.setDefaultTimeout(10 * 60 * 1000L);
    }

    @Bean
    public AsyncTaskExecutor streamingTaskExecutor() {
        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();

        executor.setThreadNamePrefix("file-download-");
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(50);
        executor.initialize();

        return executor;
    }
}