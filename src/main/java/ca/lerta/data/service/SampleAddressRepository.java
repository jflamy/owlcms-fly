package ca.lerta.data.service;

import ca.lerta.data.entity.SampleAddress;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleAddressRepository extends JpaRepository<SampleAddress, UUID> {

}