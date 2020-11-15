package com.agenson.cinema.movie;

import com.agenson.cinema.utils.CallableOneArgument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceUnitTests implements MovieConstants {

    private static final HashMap<String, InvalidMovieException.Type> INVALID_MOVIE_TITLES =
            new HashMap<String, InvalidMovieException.Type>() {{
                put(null, InvalidMovieException.Type.MANDATORY);
                put(EMPTY_TITLE, InvalidMovieException.Type.MANDATORY);
                put(MAX_SIZE_TITLE, InvalidMovieException.Type.MAXSIZE);
                put(ANOTHER_TITLE, InvalidMovieException.Type.EXISTS);
            }};

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    @Test
    public void findMovie_ShouldReturnMovie_WhenGivenUuid() {
        MovieDB movie = new MovieDB(NORMAL_TITLE);

        when(this.movieRepository.findByUuid(movie.getUuid())).thenReturn(Optional.of(movie));

        MovieDTO expected = new MovieDTO(movie);
        Optional<MovieDTO> actual = this.movieService.findMovie(movie.getUuid());

        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findMovie_ShouldReturnNull_WhenGivenUnknownUuid() {
        when(this.movieRepository.findByUuid(any(UUID.class))).thenReturn(Optional.empty());

        assertThat(this.movieService.findMovie(UUID.randomUUID())).isEmpty();
        assertThat(this.movieService.findMovie(null)).isEmpty();
    }

    @Test
    public void findMovies_ShouldReturnMovieList() {
        List<MovieDB> movieList = Arrays.asList(
                new MovieDB(NORMAL_TITLE),
                new MovieDB(ANOTHER_TITLE));

        when(this.movieRepository.findAll()).thenReturn(movieList);

        List<MovieDTO> actual = this.movieService.findMovies();
        List<MovieDTO> expected = movieList.stream().map(MovieDTO::new).collect(Collectors.toList());

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void createMovie_ShouldReturnMovieWithUppercaseTitle_WhenGivenLowercaseTitle() {
        when(this.movieRepository.findByTitle(NORMAL_TITLE)).thenReturn(Optional.empty());
        when(this.movieRepository.save(any(MovieDB.class))).then(returnsFirstArg());

        MovieDTO actual = this.movieService.createMovie(NORMAL_TITLE.toLowerCase());

        assertThat(actual.getUuid()).isNotNull();
        assertThat(actual.getTitle()).isEqualTo(NORMAL_TITLE);
    }

    @Test
    public void createMovie_ShouldThrowAssociatedInvalidMovieException_WhenGivenInvalidTitle() {
        this.assertShouldThrowInvalidMovieException_WhenGivenInvalidTitle(title -> {
            this.movieService.createMovie(title);
        });
    }

    @Test
    public void updateMovieTitle_ShouldReturnUnmodifiedMovie_WhenGivenUuidAndLowercaseTitle() {
        MovieDB movie = new MovieDB(NORMAL_TITLE);

        when(this.movieRepository.save(any(MovieDB.class))).then(returnsFirstArg());
        when(this.movieRepository.findByUuid(movie.getUuid())).thenReturn(Optional.of(movie));
        when(this.movieRepository.findByTitle(movie.getTitle())).thenReturn(Optional.of(movie));

        MovieDTO expected = new MovieDTO(movie);
        Optional<MovieDTO> actual = this.movieService.updateMovieTitle(expected.getUuid(), NORMAL_TITLE.toLowerCase());

        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void updateMovieTitle_ShouldThrowAssociatedInvalidMovieException_WhenGivenInvalidTitle() {
        MovieDB movie = new MovieDB(NORMAL_TITLE);

        when(this.movieRepository.findByUuid(movie.getUuid())).thenReturn(Optional.of(movie));

        this.assertShouldThrowInvalidMovieException_WhenGivenInvalidTitle(title -> {
            this.movieService.updateMovieTitle(movie.getUuid(), title);
        });
    }

    private void assertShouldThrowInvalidMovieException_WhenGivenInvalidTitle(CallableOneArgument<String> callable) {
        when(this.movieRepository.findByTitle(anyString())).thenAnswer(invocation -> {
            MovieDB movieWithSameTitle = new MovieDB(invocation.getArgument(0));

            return Optional.of(movieWithSameTitle);
        });

        for (Map.Entry<String, InvalidMovieException.Type> pair : INVALID_MOVIE_TITLES.entrySet())
            assertThatExceptionOfType(InvalidMovieException.class)
                    .isThrownBy(() -> callable.call(pair.getKey()))
                    .withMessage(pair.getValue().toString());
    }
}
