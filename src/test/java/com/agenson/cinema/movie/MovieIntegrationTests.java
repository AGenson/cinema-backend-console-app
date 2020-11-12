package com.agenson.cinema.movie;

import com.agenson.cinema.security.SecurityContext;
import com.agenson.cinema.security.UserRole;
import com.agenson.cinema.utils.CallableOneArgument;
import com.agenson.cinema.utils.StaffSecurityAssertion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MovieIntegrationTests implements MovieConstants {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private SecurityContext securityContext;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieService movieService;

    @BeforeEach
    public void defaultLogin() {
        this.loginAs(UserRole.STAFF);
    }

    @AfterEach
    public void logout() {
        this.securityContext.logout();
    }

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
    public void createMovie_ShouldNotPersistMovie_WhenGivenInvalidTitle() {
        this.assertShouldThrowInvalidMovieException_WhenGivenInvalidTitle(title -> {
            this.movieService.createMovie(title);
        });
    }

    @Test
    public void createMovie_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.movieService.createMovie(NORMAL_TITLE),
                () -> this.loginAs(UserRole.CUSTOMER),
                () -> this.logout()
        );
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
    public void updateMovieTitle_ShouldNotPersistMovie_WhenGivenInvalidTitle() {
        UUID uuid = this.movieRepository.save(new MovieDB(NORMAL_TITLE)).getUuid();

        this.assertShouldThrowInvalidMovieException_WhenGivenInvalidTitle(title -> {
            this.movieService.updateMovieTitle(uuid, title);
        });
    }

    @Test
    public void updateMovieTitle_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.movieService.updateMovieTitle(UUID.randomUUID(), UNKNOWN_TITLE),
                () -> this.loginAs(UserRole.CUSTOMER),
                () -> this.logout()
        );
    }

    @Test
    public void removeMovie_ShouldRemoveMovie_WhenGivenUuid() {
        UUID uuid = this.movieRepository.save(new MovieDB(NORMAL_TITLE)).getUuid();

        this.movieService.removeMovie(uuid);

        assertThat(this.movieRepository.findByUuid(uuid).isPresent()).isFalse();
    }

    @Test
    public void removeMovie_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.movieService.removeMovie(UUID.randomUUID()),
                () -> this.loginAs(UserRole.CUSTOMER),
                () -> this.logout()
        );
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

    private void loginAs(UserRole role) {
        this.securityContext.login(UUID.randomUUID(), role);
    }
}
