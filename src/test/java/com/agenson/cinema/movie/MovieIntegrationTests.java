package com.agenson.cinema.movie;

import com.agenson.cinema.utils.CallableOneArgument;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
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
public class MovieIntegrationTests implements MovieConstants {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieService movieService;

    @Test
    public void findMovie_ShouldReturnPersistedMovie_WhenGivenUuidOrTitle() {
        MovieDB movie = this.movieRepository.save(new MovieDB(NORMAL_TITLE));

        MovieDTO expected = this.mapper.map(movie, MovieDTO.class);
        Optional<MovieDTO> actual = this.movieService.findMovie(movie.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);

        actual = this.movieService.findMovie(movie.getTitle());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findMovie_ShouldReturnNull_WhenNotFoundWithUuidOrTitle() {
        this.movieRepository.save(new MovieDB(NORMAL_TITLE));

        assertThat(this.movieService.findMovie(UUID.randomUUID()).isPresent()).isFalse();
        assertThat(this.movieService.findMovie(UNKNOWN_TITLE).isPresent()).isFalse();
    }

    @Test
    public void findMovies_ShouldReturnMovieList() {
        List<MovieDB> movieList = Arrays.asList(
                new MovieDB(NORMAL_TITLE),
                new MovieDB(ANOTHER_TITLE));

        assertThat(this.movieRepository.findAll().size()).isZero();

        this.movieRepository.saveAll(movieList);

        List<MovieDTO> actual = this.movieService.findMovies();
        List<MovieDTO> expected = movieList.stream()
                .map(movie -> this.mapper.map(movie, MovieDTO.class))
                .collect(Collectors.toList());

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void createMovie_ShouldReturnPersistedMovie_WhenGivenTitle() {
        MovieDTO expected = this.movieService.createMovie(NORMAL_TITLE);
        Optional<MovieDTO> actual = this.movieRepository.findByUuid(expected.getUuid())
                .map(movie -> this.mapper.map(movie, MovieDTO.class));

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void createOrUpdateMovie_ShouldNotPersistMovie_WhenGivenInvalidTitle() {
        this.assertShouldThrowInvalidMovieException_WhenGivenInvalidTitle(title -> {
            this.movieService.createMovie(title);
        });
    }

    @Test
    public void updateMovieTitle_ShouldReturnPersistedMovie_WhenGivenTitle() {
        UUID uuid = this.movieRepository.save(new MovieDB(NORMAL_TITLE)).getUuid();

        Optional<MovieDTO> expected = this.movieService.updateMovieTitle(uuid, ANOTHER_TITLE);
        Optional<MovieDTO> actual = this.movieRepository.findByUuid(uuid)
                .map(movie -> this.mapper.map(movie, MovieDTO.class));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void updateMovie_ShouldNotPersistMovie_WhenGivenInvalidTitle() {
        UUID uuid = this.movieRepository.save(new MovieDB(NORMAL_TITLE)).getUuid();

        this.assertShouldThrowInvalidMovieException_WhenGivenInvalidTitle(title -> {
            this.movieService.updateMovieTitle(uuid, title);
        });
    }

    @Test
    public void removeMovie_ShouldRemoveMovie_WhenGivenUuid() {
        MovieDB movie = this.movieRepository.save(new MovieDB(NORMAL_TITLE));

        this.movieService.removeMovie(movie.getUuid());
        Optional<MovieDB> actual = this.movieRepository.findByUuid(movie.getUuid());

        assertThat(actual.isPresent()).isFalse();
    }

    private void assertShouldThrowInvalidMovieException_WhenGivenInvalidTitle(CallableOneArgument<String> callable) {
        this.movieRepository.save(new MovieDB(ANOTHER_TITLE));

        List<MovieDB> expected = this.movieRepository.findAll();

        for (String title : Arrays.asList(null, EMPTY_TITLE, MAX_SIZE_TITLE, ANOTHER_TITLE)) {
            assertThatExceptionOfType(InvalidMovieException.class)
                    .isThrownBy(() -> callable.call(title));

            List<MovieDB> actual = this.movieRepository.findAll();

            assertThat(actual.size()).isEqualTo(expected.size());
            assertThat(actual).containsOnlyOnceElementsOf(expected);
        }
    }
}
