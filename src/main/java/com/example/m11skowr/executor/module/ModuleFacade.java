package com.example.m11skowr.executor.module;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;

import java.util.Set;

import static com.example.m11skowr.executor.module.cache.Cache.CACHE1;
import static com.example.m11skowr.executor.module.cache.Cache.CACHE2;

@RequiredArgsConstructor
public class ModuleFacade {

    private final ModuleService moduleService;

    @Cacheable(CACHE1)
    public Set<String> getAnimalsCacheableUnsynced() {
        return moduleService.getAnimals();
    }

    @Cacheable(value = CACHE2, sync = true)
    public Set<String> getAnimalsCacheableSynced() {
        return moduleService.getAnimals();
    }

    public Set<String> getAnimals() {
        return moduleService.getAnimals2();
    }

}
