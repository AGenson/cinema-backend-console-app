package com.agenson.cinema.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<RoomDB, Long> {

    Optional<RoomDB> findByUuid(UUID uuid);

    Optional<RoomDB> findByNumber(int number);

    void deleteByUuid(UUID uuid);
}
