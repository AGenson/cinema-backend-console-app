package com.agenson.cinema.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderDB, Long> {

    Optional<OrderDB> findByUuid(UUID uuid);

    void deleteByUuid(UUID uuid);
}
