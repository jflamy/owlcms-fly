package ca.lerta.fly.data.service;

import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import ca.lerta.fly.data.entity.FlyApplication;
import ca.lerta.fly.inmemory.InMemoryJpaRepository;
import ca.lerta.fly.inmemory.UUIDPrimaryKeyGenerator;

@Component
@Primary
public class FlyApplicationRepository extends InMemoryJpaRepository<FlyApplication,UUID> {

    public FlyApplicationRepository() {
        this.setPrimaryKeyGenerator(new UUIDPrimaryKeyGenerator());
    }
    
}
