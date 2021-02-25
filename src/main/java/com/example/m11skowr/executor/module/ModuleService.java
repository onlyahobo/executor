package com.example.m11skowr.executor.module;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import static java.lang.System.out;
import static java.util.Collections.emptySet;
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
        // With thenCombine() it is an animalThread; with thenCombineAsync forkJoin common pool thread
        //out.println("Combining on thread: " + currentThread().getName());
        return Stream.concat(one.stream(), two.stream()).collect(toUnmodifiableSet());
    }

    Set<String> getAnimals2() {
        try {
            return CompletableFuture.completedFuture(CatsAndDogs.builder())
                .thenCombine(supplyAsync(moduleRepository::getCats, executor).exceptionally(this::onException), CatsAndDogs.CatsAndDogsBuilder::cats)
                .thenCombine(supplyAsync(moduleRepository::getDogs, executor).exceptionally(this::onException), CatsAndDogs.CatsAndDogsBuilder::dogs)
                .get()
                .build()
                .getAnimals();
        } catch (ExecutionException | InterruptedException ex) {
            throw new RuntimeException("Could not supply instance availability.", ex);
        }
    }

    private Set<String> onException(Throwable ex) {
        out.printf("Default... %s", ex);
        return emptySet();
    }

    @Getter
    @Builder
    private static final class CatsAndDogs {

        private final Set<String> cats;

        private final Set<String> dogs;

        public Set<String> getAnimals() {
            return Stream.concat(cats.stream(), dogs.stream()).collect(toUnmodifiableSet());
        }
    }

}
