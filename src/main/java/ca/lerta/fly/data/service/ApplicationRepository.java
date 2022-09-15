package ca.lerta.fly.data.service;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import ca.lerta.fly.data.entity.FlyApplication;

public interface ApplicationRepository extends JpaRepository<FlyApplication, UUID> {

}