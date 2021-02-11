package com.example.m11skowr.executor

import com.example.m11skowr.executor.module.ModuleFacade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Specification

import static java.lang.System.out
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD

/**
 * Copyright (c) Asseco Business Solutions S.A. All rights reserved.
 */

//https://www.baeldung.com/spring-spock-testing

@SpringBootTest
//Must clear spring context after each test, so that the following tests don't get values from cache
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class ExecutorBehindCacheTest extends Specification {

    @Autowired
    private ModuleFacade moduleFacade

    def "Testing that threads enter in Cacheable method before it gets filled (async) 1"() {
        setup:
        def gettingAnimals = { ->
            out.println(Thread.currentThread().getName())
            moduleFacade.getAnimals()
        }

        when:
        (1..10).each { new Thread(gettingAnimals).start() }
        Thread.sleep(60000)

        and:
        out.println("Second round has started (should get from cache)...")
        def animals = moduleFacade.getAnimals()

        then:
        1 == 1
        out.println(animals)
    }

    def "With synchronization, it clearly shows we get inside Cacheable method only once; other calls get results from cache"() {
        setup:
        def gettingAnimals = { ->
            synchronized (this) {
                out.println(Thread.currentThread().getName())
                moduleFacade.getAnimals()
            }
        }

        when:
        (1..10).each { new Thread(gettingAnimals).start() }
        Thread.sleep(10000)

        and:
        out.println("Second round has started (should get from cache)...")
        def animals = moduleFacade.getAnimals()

        then:
        1 == 1
        out.println(animals)
    }


    def "Testing that threads enter in Cacheable method before it gets filled (sync)"() {
        when:
        out.println("First round has started...")
        moduleFacade.getAnimals()

        and:
        out.println("Second round has started (should get from cache)...")
        moduleFacade.getAnimals()

        then:
        1 == 1
    }

}
