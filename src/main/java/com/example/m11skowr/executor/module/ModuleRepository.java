package com.example.m11skowr.executor.module;

import java.time.Duration;
import java.util.Set;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.time.Duration.ofSeconds;
import static java.util.Set.of;

interface ModuleRepository {

    Set<String> getCats();

    Set<String> getDogs();

}

class ModuleRepositoryImpl implements ModuleRepository {

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
