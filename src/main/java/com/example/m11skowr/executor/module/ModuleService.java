package com.example.m11skowr.executor.module;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toUnmodifiableSet;

@RequiredArgsConstructor
class ModuleService {

    private final ModuleRepository moduleRepository;

    private final Executor executor;

    Set<String> getAnimals() {
        return supplyAsync(moduleRepository::getCats, executor).thenCombine(
            supplyAsync(moduleRepository::getDogs, executor), this::merge
        ).join();
    }

    private Set<String> merge(Set<String> one, Set<String> two) {
        return Stream.concat(one.stream(), two.stream()).collect(toUnmodifiableSet());
    }

}
