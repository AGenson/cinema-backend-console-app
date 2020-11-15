package com.agenson.cinema.room;

import com.agenson.cinema.movie.MovieDB;
import com.agenson.cinema.movie.MovieRepository;
import com.agenson.cinema.security.SecurityService;
import com.agenson.cinema.security.SecurityRole;
import com.agenson.cinema.user.UserDB;
import com.agenson.cinema.utils.CallableOneArgument;
import com.agenson.cinema.utils.CallableTwoArguments;
import com.agenson.cinema.utils.StaffSecurityAssertion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RoomIntegrationTests implements RoomConstants {

    private static final List<Map.Entry<Integer, Integer>> INVALID_ROOM_CAPACITIES = Arrays.asList(
            new AbstractMap.SimpleEntry<>(NEGATIVE_ROWS, NORMAL_COLS),
            new AbstractMap.SimpleEntry<>(ZERO_ROWS, NORMAL_COLS),
            new AbstractMap.SimpleEntry<>(NORMAL_ROWS, NEGATIVE_COLS),
            new AbstractMap.SimpleEntry<>(NORMAL_ROWS, ZERO_COLS)
    );

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RoomService roomService;

    private UserDB defaultUser;

    @BeforeEach
    public void setup() {
        if (this.defaultUser == null) {
            UserDB user = new UserDB("username", this.encoder.encode("password"));

            this.entityManager.persist(user);
            this.defaultUser = user;
        }

        this.loginAs(SecurityRole.STAFF);
    }

    @AfterEach
    public void logout() {
        this.securityService.logout();
    }

    @Test
    public void findRoom_ShouldRReturnPersistedRoom_WhenGivenUuidOrRoomNumber() {
        RoomDB room = this.roomRepository.save(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS));

        RoomDTO expected = new RoomDTO(room);
        Optional<RoomDTO> actual = this.roomService.findRoom(room.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);

        actual = this.roomService.findRoom(room.getNumber());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findRoom_ShouldReturnNull_WhenNotFoundWithUuidOrRoomNumber() {
        assertThat(this.roomRepository.findByUuid(UUID.randomUUID()).isPresent()).isFalse();
        assertThat(this.roomRepository.findByNumber(UNKNOWN_NUMBER).isPresent()).isFalse();
    }

    @Test
    public void findRooms_ShouldReturnMovieList() {
        List<RoomDB> roomList = Arrays.asList(
                new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS),
                new RoomDB(NORMAL_NUMBER+1, NORMAL_ROWS+10, NORMAL_COLS+20)
        );

        assertThat(this.roomRepository.findAll().size()).isZero();

        this.roomRepository.saveAll(roomList);

        List<RoomDTO> actual = this.roomService.findRooms();
        List<RoomDTO> expected = roomList.stream().map(RoomDTO::new).collect(Collectors.toList());

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void findRooms_ShouldReturnFilteredRoomList_WhenGivenMovieUuid() {
        RoomDB room1 = new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS);
        RoomDB room2 = new RoomDB(NORMAL_NUMBER+1, NORMAL_ROWS+10, NORMAL_COLS+20);
        MovieDB movie = this.movieRepository.save(new MovieDB("A NORMAL TITLE"));

        room1.setMovie(movie);
        this.roomRepository.saveAll(Arrays.asList(room1, room2));
        this.entityManager.refresh(movie);

        List<RoomDTO> actual = this.roomService.findRooms(movie.getUuid());
        List<RoomDTO> expected = Collections.singletonList(new RoomDTO(room1));

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void createRoom_ShouldReturnPersistedRoom_WhenGivenRoomProperties() {
        RoomDTO expected = this.roomService.createRoom(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS);
        Optional<RoomDTO> actual = this.roomRepository.findByUuid(expected.getUuid()).map(RoomDTO::new);

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void createRoom_ShouldNotPersistRoom_WhenGivenInvalidRoomProperties() {
        this.assertShouldNotPersistRoom_WhenGivenInvalidRoomNumber(number -> {
            this.roomService.createRoom(number, NORMAL_ROWS, NORMAL_COLS);
        });

        this.assertShouldNotPersistRoom_WhenGivenInvalidRoomCapacity((nbRows, nbCols) -> {
            this.roomService.createRoom(NORMAL_NUMBER, nbRows, nbCols);
        });
    }

    @Test
    public void createRoom_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.roomService.createRoom(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS),
                () -> this.loginAs(SecurityRole.CUSTOMER),
                () -> this.logout()
        );
    }

    @Test
    public void updateRoomNumber_ShouldReturnModifiedRoom_WhenGivenUuidAndRoomNumber() {
        UUID uuid = this.roomRepository.save(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS)).getUuid();

        Optional<RoomDTO> expected = this.roomService.updateRoomNumber(uuid, NORMAL_NUMBER+1);
        Optional<RoomDTO> actual = this.roomRepository.findByUuid(uuid).map(RoomDTO::new);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void updateRoomNumber_ShouldNotPersistRoom_WhenGivenInvalidRoomNumber() {
        UUID uuid = this.roomRepository.save(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS)).getUuid();

        this.assertShouldNotPersistRoom_WhenGivenInvalidRoomNumber(number -> {
            this.roomService.updateRoomNumber(uuid, number);
        });
    }

    @Test
    public void updateRoomNumber_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.roomService.updateRoomNumber(UUID.randomUUID(), UNKNOWN_NUMBER),
                () -> this.loginAs(SecurityRole.CUSTOMER),
                () -> this.logout()
        );
    }

    @Test
    public void updateRoomCapacity_ShouldReturnModifiedRoom_WhenGivenUuidAndRoomCapacity() {
        UUID uuid = this.roomRepository.save(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS)).getUuid();

        Optional<RoomDTO> expected = this.roomService.updateRoomCapacity(uuid, NORMAL_ROWS+10, NORMAL_COLS+20);
        Optional<RoomDTO> actual = this.roomRepository.findByUuid(uuid).map(RoomDTO::new);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void updateRoomCapacity_ShouldNotPersistRoom_WhenGivenInvalidRoomCapacity() {
        UUID uuid = this.roomRepository.save(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS)).getUuid();

        this.assertShouldNotPersistRoom_WhenGivenInvalidRoomCapacity((nbRows, nbCols) -> {
            this.roomService.updateRoomCapacity(uuid, nbRows, nbCols);
        });
    }

    @Test
    public void updateRoomCapacity_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.roomService.updateRoomCapacity(UUID.randomUUID(), NEGATIVE_ROWS, NEGATIVE_COLS),
                () -> this.loginAs(SecurityRole.CUSTOMER),
                () -> this.logout()
        );
    }

    @Test
    public void updateRoomMovie_ShouldReturnModifiedRoom_WhenGivenUuidAndMovieUuid() {
        RoomDB room = this.roomRepository.save(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS));
        MovieDB movie = this.movieRepository.save(new MovieDB("A NORMAL TITLE"));

        room.setMovie(movie);
        RoomDTO expected = new RoomDTO(room);
        Optional<RoomDTO> actual = this.roomService.updateRoomMovie(room.getUuid(), movie.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void updateRoomMovie_ShouldNotPersistRoom_WhenGivenInvalidMovieUuid() {
        RoomDB room = new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS);
        MovieDB movie = this.movieRepository.save(new MovieDB("A NORMAL TITLE"));

        room.setMovie(movie);
        RoomDB expected = this.roomRepository.save(room);

        assertThatExceptionOfType(InvalidRoomException.class)
                .isThrownBy(() -> this.roomService.updateRoomMovie(room.getUuid(), UUID.randomUUID()));

        Optional<RoomDB> actual = this.roomRepository.findByUuid(room.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void updateRoomMovie_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.roomService.updateRoomMovie(UUID.randomUUID(), UUID.randomUUID()),
                () -> this.loginAs(SecurityRole.CUSTOMER),
                () -> this.logout()
        );
    }

    @Test
    public void removeRoom_ShouldRemoveRoom_WhenGivenUuid() {
        RoomDB room = this.roomRepository.save(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS));

        this.roomService.removeRoom(room.getUuid());
        Optional<RoomDB> actual = this.roomRepository.findByUuid(room.getUuid());

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void removeRoom_ShouldThrowSecurityException_WhenNotLoggedInAsStaff() {
        StaffSecurityAssertion.assertShouldThrowSecurityException(
                () -> this.roomService.removeRoom(UUID.randomUUID()),
                () -> this.loginAs(SecurityRole.CUSTOMER),
                () -> this.logout()
        );
    }

    private void assertShouldNotPersistRoom_WhenGivenInvalidRoomNumber(CallableOneArgument<Integer> callable) {
        if (!this.roomRepository.findByNumber(NORMAL_NUMBER+1).isPresent())
            this.roomRepository.save(new RoomDB(NORMAL_NUMBER+1, NORMAL_ROWS, NORMAL_COLS));

        List<RoomDB> expected = this.roomRepository.findAll();

        for (int number : Arrays.asList(NEGATIVE_NUMBER, ZERO_NUMBER, NORMAL_NUMBER+1)) {
            assertThatExceptionOfType(InvalidRoomException.class)
                    .isThrownBy(() -> callable.call(number));

            List<RoomDB> actual = this.roomRepository.findAll();

            assertThat(actual.size()).isEqualTo(expected.size());
            assertThat(actual).containsOnlyOnceElementsOf(expected);
        }
    }

    private void assertShouldNotPersistRoom_WhenGivenInvalidRoomCapacity(
            CallableTwoArguments<Integer, Integer> callable
    ) {
        List<RoomDB> expected = this.roomRepository.findAll();

        for (Map.Entry<Integer, Integer> entry : INVALID_ROOM_CAPACITIES) {
            assertThatExceptionOfType(InvalidRoomException.class)
                    .isThrownBy(() -> callable.call(entry.getKey(), entry.getValue()));

            List<RoomDB> actual = this.roomRepository.findAll();

            assertThat(actual.size()).isEqualTo(expected.size());
            assertThat(actual).containsOnlyOnceElementsOf(expected);
        }
    }

    private void loginAs(SecurityRole role) {
        this.defaultUser.setRole(role);
        this.entityManager.persist(this.defaultUser);
        this.securityService.login("username", "password");
    }
}
