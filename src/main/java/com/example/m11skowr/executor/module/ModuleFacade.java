package com.example.m11skowr.executor.module;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;

import java.util.Set;

/**
 * Copyright (c) Asseco Business Solutions S.A. All rights reserved.
 */

@RequiredArgsConstructor
public class ModuleFacade {

    private final ModuleService moduleService;

    @Cacheable("NAME")
    public Set<String> getAnimals() {
        return moduleService.getAnimals();
    }

}
