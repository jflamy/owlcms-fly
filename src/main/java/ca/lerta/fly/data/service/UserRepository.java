package ca.lerta.fly.data.service;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import ca.lerta.fly.data.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByUsername(String username);
}