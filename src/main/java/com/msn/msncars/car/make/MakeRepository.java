package com.msn.msncars.car.make;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MakeRepository extends JpaRepository<Make, Long> {
    Optional<Make> findByName(String name);
}
