package com.example.m11skowr.executor.module;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.Executor;

@TestConfiguration
class MyTestConfiguration {

    @Bean
    @Primary
    ModuleFacade testModuleFacade(@Qualifier("animalExecutor") Executor executor) {
        return new ModuleFacade(new ModuleService(testModuleRepository(), executor));
    }

    @Bean
    @Primary
    //https://objectpartners.com/2017/04/18/spring-integration-testing-with-spock-mocks/
    ModuleRepository testModuleRepository() {
        return new InMemoryModuleRepository();
    }

}
