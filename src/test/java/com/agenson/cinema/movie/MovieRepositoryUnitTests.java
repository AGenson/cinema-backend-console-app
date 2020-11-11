package com.agenson.cinema.movie;

import com.agenson.cinema.room.RoomDB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MovieRepositoryUnitTests implements MovieConstants {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MovieRepository movieRepository;

    private MovieDB expected;

    @BeforeEach
    public void setup() {
        expected = this.entityManager.persist(new MovieDB(NORMAL_TITLE));
    }

    @Test
    public void findByUuid_ShouldReturnMovie_WhenGivenPersistedUuid() {
        Optional<MovieDB> actual = this.movieRepository.findByUuid(this.expected.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(this.expected);
    }

    @Test
    public void findByUuid_ShouldReturnNull_WhenGivenUnknownUuid() {
        Optional<MovieDB> actual = this.movieRepository.findByUuid(UUID.randomUUID());

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void findByTitle_ShouldReturnMovie_WhenGivenPersistedTitle() {
        Optional<MovieDB> actual = this.movieRepository.findByTitle(this.expected.getTitle());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(this.expected);
    }

    @Test
    public void findByTitle_ShouldReturnNull_WhenGivenUnknownTitle() {
        Optional<MovieDB> actual = this.movieRepository.findByTitle(UNKNOWN_TITLE);

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void deleteByUuid_ShouldDeleteMovie_WhenGivenUuid() {
        this.movieRepository.deleteByUuid(this.expected.getUuid());

        Optional<MovieDB> actual = this.movieRepository.findById(this.expected.getId());

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void deleteByUuid_ShouldSetForeignKeysToNull_WhenGivenUuid() {
        RoomDB room = new RoomDB(1, 10, 20);

        room.setMovie(this.expected);
        room = this.entityManager.persist(room);

        this.entityManager.refresh(this.expected);
        this.entityManager.refresh(room);

        assertThat(this.expected.getRooms()).containsOnly(room);
        assertThat(room.getMovie()).isEqualTo(this.expected);

        this.movieRepository.deleteByUuid(this.expected.getUuid());
        this.entityManager.flush();
        this.entityManager.refresh(room);

        assertThat(room.getMovie()).isNull();
    }
}
