package com.example.m11skowr.executor.module;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Executor;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.time.Duration.ofSeconds;
import static java.util.Set.of;

/**
 * Copyright (c) Asseco Business Solutions S.A. All rights reserved.
 */

@TestConfiguration
class MyTestConfiguration {

    @Bean
    @Primary
    ModuleFacade testModuleFacade(@Qualifier("animalExecutor") Executor executor) {
        return new ModuleFacade(new ModuleService(testModuleRepository(), executor));
    }

    @Bean
    @Primary
    ModuleRepository testModuleRepository() {
        return new TestModuleRepository();
    }

    static class TestModuleRepository implements ModuleRepository {

        @Override
        public Set<String> getCats() {
            out.println(currentThread().getName() + ": getting cats.");
            sleep(ofSeconds(1));
            return of("kitty1", "kitty2", "kitty3");
        }

        @Override
        public Set<String> getDogs() {
            out.println(currentThread().getName() + ": getting dogs.");
            sleep(ofSeconds(2));
            return of("lassie1", "lassie2", "lassie3");
        }

        private void sleep(Duration duration) {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
