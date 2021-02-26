package com.example.m11skowr.executor.module;

import java.time.Duration;
import java.util.Set;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.time.Duration.ofSeconds;
import static java.util.Set.of;

class InMemoryModuleRepository implements ModuleRepository {

    static final Set<String> CATS = of("kitty1", "kitty2", "kitty3");

    static final Set<String> DOGS = of("lassie1", "lassie2", "lassie3");

    @Override
    public Set<String> getCats() {
        out.println(currentThread().getName() + ": getting cats.");
        sleep(ofSeconds(1));
        return CATS;
    }

    @Override
    public Set<String> getDogs() {
        out.println(currentThread().getName() + ": getting dogs.");
        sleep(ofSeconds(2));
        return DOGS;
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
