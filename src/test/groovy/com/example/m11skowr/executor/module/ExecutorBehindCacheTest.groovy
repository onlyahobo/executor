package com.example.m11skowr.executor.module

import com.example.m11skowr.executor.module.ModuleFacade
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Specification

import static java.lang.System.out
import static java.lang.Thread.currentThread
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD

//https://www.baeldung.com/spring-spock-testing

@SpringBootTest
//Must clear spring context after each test, so that the following tests don't get values from cache
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class ExecutorBehindCacheTest extends Specification {

    @Autowired
    private ModuleFacade moduleFacade

    /*
        gettingAnimals runnable is not internally synchronized so all threads may enter it as the cpu wishes.
        with the animalExecutor defined like this: cetCorePoolSize(2), maxPoolSize(2), queueCapacity(18) and 10 threads calling moduleFacade.getAnimals()
        all threads will enter (as the cache is not yet set) and queue in the executor as only 2 will proceed simultaneously, the other 18 waiting in the queue.
        No thread will be rejected with this configuration, but if we decrease the queue size to 17 one thread will get rejected, and so on...
    * */

    def "Testing that threads enter in Cacheable method before it gets filled (async) 1"() {
        setup:
        def gettingAnimals = { ->
            out.println(currentThread().getName())
            moduleFacade.getAnimalsCacheableUnsynced()
        }

        when:
        (1..10).each { new Thread(gettingAnimals).start() }
        Thread.sleep(60000)

        and:
        out.println("Second round has started (should get from cache)...")
        def animals = moduleFacade.getAnimalsCacheableUnsynced()

        then:
        1 == 1
        out.println(animals)
    }

    def "With Cacheable(sync = true), it clearly shows we get inside Cacheable method only once; other calls get results from cache"() {
        setup:
        def gettingAnimals = { ->
            out.println(currentThread().getName())
            moduleFacade.getAnimalsCacheableSynced()
        }

        // moduleService.getAnimals() >>> Set.of("lassie")

        when:
        (1..10).each { new Thread(gettingAnimals).start() }
        Thread.sleep(60000)

        then:
        1 == 1
    }

    def "With java synchronization, it clearly shows we get inside Cacheable method only once; other calls get results from cache"() {
        setup:
        def gettingAnimals = { ->
            synchronized (this) {
                out.println(currentThread().getName())
                moduleFacade.getAnimalsCacheableUnsynced()
            }
        }

        when:
        (1..10).each { new Thread(gettingAnimals).start() }
        Thread.sleep(10000)

        and:
        out.println("Second round has started (should get from cache)...")
        def animals = moduleFacade.getAnimalsCacheableUnsynced()

        then:
        1 == 1
        out.println(animals)
    }


    def "Testing that threads enter in Cacheable method before it gets filled (sync)"() {
        when:
        out.println("First round has started...")
        moduleFacade.getAnimalsCacheableUnsynced()

        and:
        out.println("Second round has started (should get from cache)...")
        moduleFacade.getAnimalsCacheableUnsynced()

        then:
        1 == 1
    }

}
