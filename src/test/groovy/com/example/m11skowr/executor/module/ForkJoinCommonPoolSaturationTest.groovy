package com.example.m11skowr.executor.module

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

import static com.example.m11skowr.executor.module.InMemoryModuleRepository.CATS
import static com.example.m11skowr.executor.module.InMemoryModuleRepository.DOGS
import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly
import static java.lang.Runtime.getRuntime
import static java.lang.System.currentTimeMillis
import static java.lang.System.out
import static java.lang.Thread.currentThread
import static java.lang.Thread.sleep
import static java.time.Duration.ofMillis
import static java.time.Duration.ofSeconds
import static java.util.concurrent.ForkJoinPool.commonPool
import static java.util.stream.IntStream.range

@SpringBootTest
@ContextConfiguration(classes = MyTestConfiguration.class)
class ForkJoinCommonPoolSaturationTest extends Specification {

    @Autowired
    private ModuleFacade moduleFacade

    /*
        thenCombine() is performed on the passed to CompletableFuture executor's thread, and on ForkJoin commonPool is none passed.
        thenCombineAsync() is performed on on ForkJoin commonPool.
    * */

    def "Saturate the ForkJoin commonPool with tasks, perform some other commonPool task (*thenCombineAsync() on CompletableFuture) and see it hang..."() {
        setup: "creating as many sleeping threads as is the capacity of commonPool"
        def threadCount = getRuntime().availableProcessors()
        def start = new CountDownLatch(1)
        def readyToSleep = new CountDownLatch(threadCount)
        def forkJoinCommonPoolThreadSleepTime = ofSeconds(30)

        def justSleeping = {
            readyToSleep.countDown()
            awaitUninterruptibly(start)
            out.println(currentThread().getName() + " I'm sleeping...")
            sleep(forkJoinCommonPoolThreadSleepTime.toMillis())
            out.println(currentThread().getName() + " I'm awake!")
        }

        def threads = range(0, threadCount).collect { new Thread(justSleeping) }

        when: "submitting them all to the executor and waiting till they're ready..."
        threads.each { commonPool().submit(it) }
        awaitUninterruptibly(readyToSleep, ofSeconds(5))    //Must have a wait timeout, since we cannot guarantee the pool accepts all the tasks now...

        and: "starting recording time and releasing the sleeping threads at the same time..."
        def beginTime = currentTimeMillis()     // time start
        start.countDown()                       // off with threads...

        and: "getting the animals and stopping recording when they arrive..."
        def animals = moduleFacade.getAnimals3()
        def animalProcessingTime = currentTimeMillis() - beginTime

        then:
        animalProcessingTime >= forkJoinCommonPoolThreadSleepTime.toMillis()
        animals == CATS + DOGS
        out.printf("Animals got in %s seconds. %n", ofMillis(animalProcessingTime).toSeconds())
    }

}
