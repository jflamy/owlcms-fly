package ca.lerta.fly.data.service;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import ca.lerta.fly.data.entity.SampleAddress;

public interface SampleAddressRepository extends JpaRepository<SampleAddress, UUID> {

}