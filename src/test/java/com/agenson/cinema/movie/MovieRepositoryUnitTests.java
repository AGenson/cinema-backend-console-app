package com.agenson.cinema.movie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MovieRepositoryUnitTests implements TitleConstants {

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
    public void findByTitle_ShouldReturnMovie_WhenGivenPersistedTitle() {
        Optional<MovieDB> actual = this.movieRepository.findByTitle(expected.getTitle());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findByTitle_ShouldReturnNull_WhenGivenUnknownTitle() {
        Optional<MovieDB> actual = this.movieRepository.findByTitle(UNKNOWN_TITLE);

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void findByUuid_ShouldReturnMovie_WhenGivenPersistedUuid() {
        Optional<MovieDB> actual = this.movieRepository.findByUuid(expected.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findByUuid_ShouldReturnNull_WhenGivenUnknownUuid() {
        Optional<MovieDB> actual = this.movieRepository.findByUuid(UUID.randomUUID());

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void deleteByUuid_ShouldDeleteMovie_WhenGivenUuid() {
        this.movieRepository.deleteByUuid(expected.getUuid());

        Optional<MovieDB> actual = movieRepository.findByUuid(expected.getUuid());

        assertThat(actual.isPresent()).isFalse();
    }
}
