package com.example.m11skowr.executor.module;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;

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
            out.printf("Rejected AnimalThread task %s on %s. Executor %s. %n", runnable, currentThread().getName(), executor);
            throw new RejectedExecutionException("Task " + runnable.toString() + " rejected from " + executor.toString());
        };

        return new ThreadPoolExecutor(
            2,
            2,
            0L, SECONDS,
            new LinkedBlockingQueue<>(18),
            factory,
            handler
        );
    }

}
