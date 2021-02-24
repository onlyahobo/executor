package com.example.m11skowr.executor.module;

import java.util.Set;

interface ModuleRepository {

    Set<String> getCats();

    Set<String> getDogs();

}

class ModuleRepositoryImpl implements ModuleRepository {

    @Override
    public Set<String> getCats() {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public Set<String> getDogs() {
        throw new RuntimeException("not yet implemented");
    }

}
