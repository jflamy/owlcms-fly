package ca.lerta.fly.data.service;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import ca.lerta.fly.data.entity.FlyApplication;
import ca.lerta.fly.inmemory.InMemoryJpaRepository;
import ca.lerta.fly.inmemory.UUIDPrimaryKeyGenerator;

@Service
@SessionScope
/**
 * In-memory version.  One per session so different simultaneous users don't see
 * each others's applications.
 */
public class FlyApplicationService {

    private final FlyApplicationRepository repository;

    @Autowired
    public FlyApplicationService(FlyApplicationRepository repository) {
        System.err.println("******* FlyApplicationService *******");
        this.repository = new FlyApplicationRepository();
    }

    public Optional<FlyApplication> get(UUID id) {
        return repository.findById(id);
    }

    public FlyApplication update(FlyApplication entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<FlyApplication> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
