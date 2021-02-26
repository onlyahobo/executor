package com.example.m11skowr.executor.module

import org.spockframework.spring.SpringSpy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

import static com.example.m11skowr.executor.module.InMemoryModuleRepository.CATS
import static com.example.m11skowr.executor.module.InMemoryModuleRepository.DOGS
import static java.lang.System.out
import static java.lang.Thread.currentThread
import static java.util.concurrent.TimeUnit.SECONDS
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD

//https://www.baeldung.com/spring-spock-testing

// @formatter:off
@SpringBootTest
//Must clear spring context after each test, so that the following tests don't get values from cache
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = MyTestConfiguration.class)
class ExecutorBehindCacheTest extends Specification {

    @Autowired
    private ModuleFacade moduleFacade

    @SpringSpy  // this gives an extreme overhead combined with *Thread.sleep() called in the bean it spies on
    private ModuleRepository moduleRepository

    def "Testing if threads enter in Cacheable method before the cache gets filled (sync)"() {
        when: "calling the *getAnimalsCacheableUnsynced() for the first time"
        out.println("First round has started...")
        def animals1 = moduleFacade.getAnimalsCacheableUnsynced()

        and: "calling it for the second time"
        out.println("Second stage has started (should get from cache)...")
        def animals2 = moduleFacade.getAnimalsCacheableUnsynced()

        then: "the repository methods should have been invoked only once as the first call should reach it and the next call should get animals from cache"
        1 * moduleRepository.getCats()
        1 * moduleRepository.getDogs()
        animals1 == animals2
        animals1 == CATS + DOGS
        out.println(animals1)
    }

    /*
        gettingAnimals runnable is not internally synchronized so all threads may enter it as the cpu wishes.
        with the animalExecutor defined like this: cetCorePoolSize(2), maxPoolSize(2), queueCapacity(18) and 10 threads calling moduleFacade.getAnimals()
        all threads will enter (as the cache is not yet set) and queue in the executor as only 2 will proceed simultaneously, the other 18 waiting in the queue.
        No thread will be rejected with this configuration, but if we decrease the queue size to 17 one thread will get rejected, and so on...
    * */

    def "Testing if threads enter in Cacheable method before the cache gets filled (async)"() {
        setup:
        def countDownLatch = new CountDownLatch(10)

        def gettingAnimals = { ->
            out.println(currentThread().getName())
            moduleFacade.getAnimalsCacheableUnsynced()
            countDownLatch.countDown()
        }

        when: "calling the *getAnimalsCacheableUnsynced() 10 times asynchronously"
        (1..10).each { new Thread(gettingAnimals).start() }
        countDownLatch.await(60, SECONDS)

        and: "now calling it the 11th time"
        out.println("Second stage has started (should get from cache)...")
        def animals = moduleFacade.getAnimalsCacheableUnsynced()

        then: "both *getDogs() and *getCats() should have been called 10 times; the 11th call to the facade should retrieve animals from cache"
        10 * moduleRepository.getCats()
        10 * moduleRepository.getDogs()
        animals == CATS + DOGS
        out.println(animals)
    }

    def "Testing if threads enter in Cacheable method before the cache gets filled (async + cache with 'sync' set to true)"() {
        setup:
        def countDownLatch = new CountDownLatch(10)

        def gettingAnimals = { ->
            out.println(currentThread().getName())
            moduleFacade.getAnimalsCacheableSynced()
            countDownLatch.countDown()
        }

        when: "calling the *getAnimalsCacheableSynced 10 times asynchronously"
        (1..10).each { new Thread(gettingAnimals).start() }
        countDownLatch.await(10, SECONDS)

        then: "we should get inside Cacheable method only once; other calls should wait (never reaching the repository) and get results from cache once it is set"
        1 * moduleRepository.getCats()
        1 * moduleRepository.getDogs()
    }

    def "Testing if threads enter in Cacheable method before the cache gets filled (async + synchronized)"() {
        setup:
        def countDownLatch = new CountDownLatch(10)

        def gettingAnimals = { ->
            synchronized (this) {
                out.println(currentThread().getName())
                moduleFacade.getAnimalsCacheableUnsynced()
                countDownLatch.countDown()
            }
        }

        when: "calling the *getAnimalsCacheableSynced 10 times synchronously (see 'synchronized' in setup)"
        (1..10).each { new Thread(gettingAnimals).start() }
        countDownLatch.await(10, SECONDS)

        then: "we should have got inside Cacheable method only once; other calls get results from cache"
        1 * moduleRepository.getCats()
        1 * moduleRepository.getDogs()
    }

}
// @formatter:on
