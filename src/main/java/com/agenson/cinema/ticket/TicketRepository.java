package com.agenson.cinema.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<TicketDB, Long> {

    void deleteByUuid(UUID uuid);
}
