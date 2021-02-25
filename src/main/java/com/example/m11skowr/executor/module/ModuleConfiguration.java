package com.example.m11skowr.executor.module;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

import static java.lang.System.out;

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
        return new ModuleRepositoryImpl();
    }

    @Bean
    Executor animalExecutor() {
        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("AnimalThread-%d").build();

        RejectedExecutionHandler handler = (runnable, executor) -> {
            out.printf("Rejected AnimalThread task %s. Executor %s %n", runnable, executor);
            throw new RejectedExecutionException("Task " + runnable.toString() + " rejected from " + executor.toString());
        };

        return new ThreadPoolExecutor(
            2,
            2,
            0L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(18),
            factory,
            handler
        );
    }

}
