package com.agenson.cinema.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<RoomDB, Long> {

    Optional<RoomDB> findByUuid(UUID uuid);

    Optional<RoomDB> findByNumber(int number);

    @Transactional
    void deleteByUuid(UUID uuid);
}
