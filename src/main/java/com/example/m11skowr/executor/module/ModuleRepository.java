package com.example.m11skowr.executor.module;

import java.time.Duration;
import java.util.Set;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.time.Duration.ofSeconds;
import static java.util.Set.of;

/**
 * Copyright (c) Asseco Business Solutions S.A. All rights reserved.
 */

class ModuleRepository {

    Set<String> getCats() {
        out.println(currentThread().getName() + ": getting cats.");
        sleep(ofSeconds(5));
        return of("kitty1", "kitty2", "kitty3");
    }

    Set<String> getDogs() {
        out.println(currentThread().getName() + ": getting dogs.");
        sleep(ofSeconds(3));
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
