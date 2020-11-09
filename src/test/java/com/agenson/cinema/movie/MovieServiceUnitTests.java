package com.agenson.cinema.movie;

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
class MovieServiceUnitTests implements TitleConstants {

    private static final HashMap<String, InvalidMovieException.Type> INVALID_MOVIE_EXCEPTION_SCENARIOS =
            new HashMap<String, InvalidMovieException.Type>() {{
                put(null, InvalidMovieException.Type.MANDATORY);
                put(TitleConstants.EMPTY_TITLE, InvalidMovieException.Type.MANDATORY);
                put(TitleConstants.MAX_SIZE_TITLE, InvalidMovieException.Type.MAXSIZE);
                put(TitleConstants.NORMAL_TITLE, InvalidMovieException.Type.EXISTS);
            }};

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    @Test
    public void findMovie_ShouldReturnMovie_WhenGivenUuid() {
        MovieDB movie = new MovieDB(NORMAL_TITLE);

        when(this.movieRepository.findByUuid(movie.getUuid())).thenReturn(Optional.of(movie));

        MovieDTO expected = MovieDTO.from(movie);
        Optional<MovieDTO> actual = this.movieService.findMovie(movie.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findMovie_ShouldReturnNull_WhenGivenUnknownUuid() {
        when(this.movieRepository.findByUuid(any(UUID.class))).thenReturn(Optional.empty());

        assertThat(this.movieService.findMovie(UUID.randomUUID()).isPresent()).isFalse();
        assertThat(this.movieService.findMovie((UUID) null).isPresent()).isFalse();
    }

    @Test
    public void findMovie_ShouldReturnMovie_WhenGivenTitle() {
        MovieDB movie = new MovieDB(NORMAL_TITLE);

        when(this.movieRepository.findByTitle(movie.getTitle())).thenReturn(Optional.of(movie));

        MovieDTO expected = MovieDTO.from(movie);
        Optional<MovieDTO> actual = this.movieService.findMovie(movie.getTitle());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findMovie_ShouldReturnNull_WhenGivenUnknownTitle() {
        when(this.movieRepository.findByTitle(anyString())).thenReturn(Optional.empty());

        assertThat(this.movieService.findMovie(UNKNOWN_TITLE).isPresent()).isFalse();
        assertThat(this.movieService.findMovie((String) null).isPresent()).isFalse();
    }

    @Test
    public void findMovies_ShouldReturnMovieList() {
        List<MovieDB> movieList = Arrays.asList(
                new MovieDB(NORMAL_TITLE),
                new MovieDB(ANOTHER_TITLE));

        when(this.movieRepository.findAll()).thenReturn(movieList);

        List<MovieDTO> expected = movieList.stream().map(MovieDTO::from).collect(Collectors.toList());
        List<MovieDTO> actual = this.movieService.findMovies();

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void createOrUpdateMovie_ShouldReturnNewMovieWithUppercaseTitle_WhenGivenLowercaseTitle() {
        when(this.movieRepository.save(any(MovieDB.class))).then(returnsFirstArg());
        when(this.movieRepository.findByUuid(null)).thenReturn(Optional.empty());
        when(this.movieRepository.findByTitle(NORMAL_TITLE)).thenReturn(Optional.empty());

        MovieDTO actual = this.movieService.createOrUpdateMovie(null, NORMAL_TITLE.toLowerCase());

        assertThat(actual.getUuid()).isNotNull();
        assertThat(actual.getTitle()).isEqualTo(NORMAL_TITLE);
    }

    @Test
    public void createOrUpdateMovie_ShouldReturnUnmodifiedMovie_WhenGivenUuidAndLowercaseTitle() {
        MovieDB movie = new MovieDB(NORMAL_TITLE);

        when(this.movieRepository.save(any(MovieDB.class))).then(returnsFirstArg());
        when(this.movieRepository.findByUuid(movie.getUuid())).thenReturn(Optional.of(movie));
        when(this.movieRepository.findByTitle(movie.getTitle())).thenReturn(Optional.of(movie));

        MovieDTO expected = MovieDTO.from(movie);
        MovieDTO actual = this.movieService.createOrUpdateMovie(expected.getUuid(), NORMAL_TITLE.toLowerCase());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void createOrUpdateMovie_ShouldThrowAssociatedInvalidMovieException_WhenGivenInvalidTitle() {
        when(this.movieRepository.findByUuid(null)).thenReturn(Optional.empty());
        when(this.movieRepository.findByTitle(anyString())).thenAnswer(invocation -> {
            MovieDB movieWithSameTitle = new MovieDB(invocation.getArgument(0));
            return Optional.of(movieWithSameTitle);
        });

        for (Map.Entry<String, InvalidMovieException.Type> entry : INVALID_MOVIE_EXCEPTION_SCENARIOS.entrySet())
            assertThatExceptionOfType(InvalidMovieException.class)
                    .isThrownBy(() -> this.movieService.createOrUpdateMovie(null, entry.getKey()))
                    .withMessage(entry.getValue().toString());
    }
}
