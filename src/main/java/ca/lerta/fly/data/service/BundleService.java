package ca.lerta.fly.data.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import ca.lerta.fly.data.entity.Bundle;

@Service
@SessionScope
/**
 * In-memory version.  One per session so different simultaneous users don't see
 * each others's applications.
 */
public class BundleService {

    private final BundleRepository repository;

    public BundleRepository getRepository() {
        return repository;
    }

    @Autowired
    public BundleService(BundleRepository bundleRepository) {
        this.repository = bundleRepository;
    }

    public Optional<Bundle> get(UUID id) {
        return repository.findById(id);
    }

    public Bundle update(Bundle entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<Bundle> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
