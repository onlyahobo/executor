package com.example.m11skowr.executor.module;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
class ModuleConfiguration {

    @Bean
    ModuleFacade moduleFacade() {
        return new ModuleFacade(moduleService());
    }

    @Bean
    ModuleService moduleService() {
        return new ModuleService(moduleRepository(), animalExecutor());
    }

    @Bean
    ModuleRepository moduleRepository() {
        return new ModuleRepository();
    }

    @Bean
    Executor animalExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(0);
        executor.setThreadNamePrefix("AnimalThread-");
        executor.initialize();
        return executor;
    }

}
