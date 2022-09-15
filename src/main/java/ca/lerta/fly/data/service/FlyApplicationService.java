package ca.lerta.fly.data.service;

import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ca.lerta.fly.data.entity.FlyApplication;

@Service
public class FlyApplicationService {

    private final ApplicationRepository repository;

    @Autowired
    public FlyApplicationService(ApplicationRepository repository) {
        this.repository = repository;
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
