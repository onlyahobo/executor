package com.example.m11skowr.executor.module

import org.spockframework.spring.SpringSpy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.task.TaskRejectedException
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

import static java.lang.System.out
import static java.lang.Thread.currentThread
import static java.util.concurrent.TimeUnit.SECONDS
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
        def countDownLatch = new CountDownLatch(11)
        def exceptions = []

        def gettingAnimals = { ->
            out.println(currentThread().getName())
            Thread.sleep(1000)
            try {
                moduleFacade.getAnimals()
            } catch (Exception ex) {
                synchronized (this) {
                    out.println(ex)
                    exceptions.add(ex)
                }
            } finally {
                countDownLatch.countDown()
            }
        }

        when: "calling the *getAnimalsCacheableUnsynced() 11 times asynchronously"
        (1..11).each { new Thread(gettingAnimals).start() }
        countDownLatch.await(60, SECONDS)

        then:
        "there should be 20 calls to repository in total; since the same executor is used for both *getDogs() and *getCats() we cannot assume that each" +
                " method was called exactly 10 times. Two tasks will fail because of overflowing the task queue size but, again, it may not be cats and dogs that " +
                "will fail just once, but once of them can fail twice, the other one never failing."
        20 * moduleRepository._     // http://spockframework.org/spock/docs/1.3/all_in_one.html#_matching_any_method_call
        synchronized (this) {
            exceptions.size() == 2
        }
        exceptions.get(0).getClass() == TaskRejectedException.class
        exceptions.get(1).getClass() == TaskRejectedException.class
    }

}
