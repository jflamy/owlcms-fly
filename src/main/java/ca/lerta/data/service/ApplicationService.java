package ca.lerta.data.service;

import ca.lerta.data.entity.Application;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    private final ApplicationRepository repository;

    @Autowired
    public ApplicationService(ApplicationRepository repository) {
        this.repository = repository;
    }

    public Optional<Application> get(UUID id) {
        return repository.findById(id);
    }

    public Application update(Application entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public Page<Application> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
