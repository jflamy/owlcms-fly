package ca.lerta.fly.data.service;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import ca.lerta.fly.data.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

}