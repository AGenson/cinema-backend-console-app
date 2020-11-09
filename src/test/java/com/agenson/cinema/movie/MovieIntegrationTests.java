package com.agenson.cinema.movie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
public class MovieIntegrationTests implements TitleConstants {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieService movieService;

    @Test
    public void createOrUpdateMovie_ShouldReturnPersistedMovie_WhenGivenTitle() {
        MovieDTO expected = this.movieService.createOrUpdateMovie(null, NORMAL_TITLE);
        Optional<MovieDTO> actual = this.movieRepository.findByUuid(expected.getUuid()).map(MovieDTO::from);

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void createOrUpdateMovie_ShouldNotPersistMovie_WhenGivenInvalidTitle() {
        for (String title : Arrays.asList(null, EMPTY_TITLE, MAX_SIZE_TITLE)) {
            assertThatExceptionOfType(InvalidMovieException.class)
                    .isThrownBy(() -> this.movieService.createOrUpdateMovie(null, title));

            Optional<MovieDB> actual = this.movieRepository.findByTitle(title);

            assertThat(actual.isPresent()).isFalse();
        }
    }

    @Test
    public void findMovie_ShouldReturnPersistedMovie_WhenGivenUuid() {
        MovieDB movie = movieRepository.save(new MovieDB(NORMAL_TITLE));

        MovieDTO expected = MovieDTO.from(movie);
        Optional<MovieDTO> actual = movieService.findMovie(movie.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findMovie_ShouldReturnPersistedMovie_WhenGivenTitle() {
        MovieDB movie = movieRepository.save(new MovieDB(NORMAL_TITLE));

        MovieDTO expected = MovieDTO.from(movie);
        Optional<MovieDTO> actual = movieService.findMovie(movie.getTitle());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findMovie_ShouldReturnNull_WhenNotFoundWithUuidOrTitle() {
        assertThat(movieService.findMovie(UUID.randomUUID()).isPresent()).isFalse();
        assertThat(movieService.findMovie(UNKNOWN_TITLE).isPresent()).isFalse();
    }

    @Test
    public void findMovies_ShouldReturnMovieList() {
        List<MovieDB> movieList = Arrays.asList(
                new MovieDB(NORMAL_TITLE),
                new MovieDB(ANOTHER_TITLE));

        assertThat(this.movieRepository.findAll().size()).isZero();

        this.movieRepository.saveAll(movieList);

        List<MovieDTO> expected = movieList.stream().map(MovieDTO::from).collect(Collectors.toList());;
        List<MovieDTO> actual = this.movieService.findMovies();

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void removeMovie_ShouldRemoveMovie_WhenGivenUuid() {
        MovieDB movie = this.movieRepository.save(new MovieDB(NORMAL_TITLE));

        movieService.removeMovie(movie.getUuid());
        Optional<MovieDB> actual = movieRepository.findByUuid(movie.getUuid());

        assertThat(actual.isPresent()).isFalse();
    }
}
