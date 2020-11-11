package com.agenson.cinema.movie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<MovieDB, Long> {

    Optional<MovieDB> findByUuid(UUID uuid);

    Optional<MovieDB> findByTitle(String title);

    @Transactional
    void deleteByUuid(UUID uuid);
}
