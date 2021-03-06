package com.example.m11skowr.executor.module

import org.spockframework.spring.SpringSpy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.RejectedExecutionException

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly
import static java.lang.System.out
import static java.lang.Thread.currentThread
import static java.time.Duration.ofSeconds
import static java.util.Collections.synchronizedList
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD

@SpringBootTest
//Must clear spring context after each test, so that the following tests don't get values from cache
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = MyTestConfiguration.class)
class ExecutorTest extends Specification {

    @Autowired
    private ModuleFacade moduleFacade

    @SpringSpy
    // this gives an extreme overhead combined with *Thread.sleep() called in the bean it spies on
    private ModuleRepository moduleRepository

    def "Testing executor task queue overflow"() {
        setup:
        def toBeStarted = new CountDownLatch(1)
        def toBeDone = new CountDownLatch(11)
        def exceptions = synchronizedList([])

        def gettingAnimals = { ->
            out.println(currentThread().getName())
            awaitUninterruptibly(toBeStarted, ofSeconds(10))
            try {
                moduleFacade.getAnimals2()
            } catch (Exception ex) {
                exceptions.add(ex)
            } finally {
                toBeDone.countDown()
            }
        }

        when: "calling the *getAnimalsCacheableUnsynced() 11 times asynchronously"
        (1..11).each { new Thread(gettingAnimals).start() }
        toBeStarted.countDown()
        awaitUninterruptibly(toBeDone, ofSeconds(60))

        then:
        "there should be 20 calls to repository in total; since the same executor is used for both *getDogs() and *getCats() we cannot assume that each" +
                " method was called exactly 10 times. Two tasks will fail because of overflowing the task queue size but, again, it may not be cats and dogs that " +
                "will fail just once, but once of them can fail twice, the other one never failing."
        20 * moduleRepository._     // http://spockframework.org/spock/docs/1.3/all_in_one.html#_matching_any_method_call
        exceptions.size() == 2
        exceptions.get(0).getClass() == RejectedExecutionException.class
        exceptions.get(1).getClass() == RejectedExecutionException.class
    }

}
