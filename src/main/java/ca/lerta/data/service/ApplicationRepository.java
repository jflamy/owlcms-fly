package ca.lerta.data.service;

import ca.lerta.data.entity.Application;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

}