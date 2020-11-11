package com.agenson.cinema.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserDB, Long> {

    Optional<UserDB> findByUuid(UUID uuid);

    Optional<UserDB> findByUsername(String username);

    void deleteByUuid(UUID uuid);
}
