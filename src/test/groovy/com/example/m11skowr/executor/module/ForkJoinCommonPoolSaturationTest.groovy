package com.example.m11skowr.executor.module


import spock.lang.Specification

import java.util.concurrent.CountDownLatch

import static com.example.m11skowr.executor.module.InMemoryModuleRepository.CATS
import static com.example.m11skowr.executor.module.InMemoryModuleRepository.DOGS
import static com.example.m11skowr.executor.module.MyTestConfiguration.moduleFacadeUnitTests
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

class ForkJoinCommonPoolSaturationTest extends Specification {

    /*
        thenCombine() is performed on the previous completion stage's executor, on ForkJoin common pool is none.
        thenCombineAsync() is performed on ForkJoin commonPool if no executor is passed to it.
    * */

    def "Saturate the ForkJoin commonPool with tasks, perform some other commonPool task (*thenCombineAsync() on CompletableFuture) and see it hang..."() {
        setup: "creating as many sleeping threads as is the capacity of commonPool"
        def forkJoinCommonPoolParallelism = commonPool().getParallelism()
        assert forkJoinCommonPoolParallelism == getRuntime().availableProcessors() - 1
        def start = new CountDownLatch(1)
        def readyToSleep = new CountDownLatch(forkJoinCommonPoolParallelism)
        def forkJoinCommonPoolThreadSleepTime = ofSeconds(10)

        def justSleeping = {
            readyToSleep.countDown()
            awaitUninterruptibly(start)
            out.println(currentThread().getName() + " I'm sleeping...")
            sleep(forkJoinCommonPoolThreadSleepTime.toMillis())
            out.println(currentThread().getName() + " I'm awake!")
        }

        def threads = range(0, forkJoinCommonPoolParallelism).collect { new Thread(justSleeping) }

        when: "submitting them all to the executor and waiting till they're ready..."
        threads.each { commonPool().submit(it) }
        awaitUninterruptibly(readyToSleep, ofSeconds(5))    //Must have a wait timeout, since we cannot guarantee the pool accepts all the tasks now...

        and: "starting recording time and releasing the blocked threads (letting them sleep) at the same time..."
        def beginTime = currentTimeMillis()     // time start
        start.countDown()                       // there go the threads...

        and: "getting the animals and stopping recording when they arrive..."
        def animals = moduleFacadeUnitTests().getAnimals3()
        def animalProcessingTime = currentTimeMillis() - beginTime

        then:
        "animals should be returned no earlier than the commonPool worker sleepTime; this of course depends on thread scheduling but the timeouts " +
                "in InMemoryModuleRepository should ensure that all the sleeping threads will pass through the start latch in time before the main thread " +
                "arrives at the *thenCombineAsync() method"
        animalProcessingTime >= forkJoinCommonPoolThreadSleepTime.toMillis()
        animals == CATS + DOGS
        out.printf("Animals got in %s seconds and %s milliseconds. %n", ofMillis(animalProcessingTime).toSeconds(), ofMillis(animalProcessingTime).toMillisPart())
    }

}
