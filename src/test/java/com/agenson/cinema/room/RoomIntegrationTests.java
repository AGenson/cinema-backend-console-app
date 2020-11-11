package com.agenson.cinema.room;

import com.agenson.cinema.movie.MovieDB;
import com.agenson.cinema.movie.MovieRepository;
import com.agenson.cinema.utils.CallableOneArgument;
import com.agenson.cinema.utils.CallableTwoArguments;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class RoomIntegrationTests implements RoomConstants {

    private static final List<Map.Entry<Integer, Integer>> INVALID_ROOM_CAPACITIES = Arrays.asList(
            new AbstractMap.SimpleEntry<>(NEGATIVE_ROWS, NORMAL_COLS),
            new AbstractMap.SimpleEntry<>(ZERO_ROWS, NORMAL_COLS),
            new AbstractMap.SimpleEntry<>(NORMAL_ROWS, NEGATIVE_COLS),
            new AbstractMap.SimpleEntry<>(NORMAL_ROWS, ZERO_COLS)
    );

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RoomService roomService;

    @Test
    public void findRoom_ShouldRReturnPersistedRoom_WhenGivenUuid() {
        RoomDB room = this.roomRepository.save(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS));

        RoomDTO expected = this.mapper.map(room, RoomDTO.class);
        Optional<RoomDTO> actual = this.roomService.findRoom(room.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);

        actual = this.roomService.findRoom(room.getNumber());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findMovie_ShouldReturnNull_WhenNotFoundWithUuidOrNumber() {
        assertThat(this.roomRepository.findByUuid(UUID.randomUUID()).isPresent()).isFalse();
        assertThat(this.roomRepository.findByNumber(UNKNOWN_NUMBER).isPresent()).isFalse();
    }

    @Test
    public void findMovies_ShouldReturnMovieList() {
        List<RoomDB> roomList = Arrays.asList(
                new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS),
                new RoomDB(NORMAL_NUMBER+1, NORMAL_ROWS+10, NORMAL_COLS+20)
        );

        assertThat(this.roomRepository.findAll().size()).isZero();

        this.roomRepository.saveAll(roomList);

        List<RoomDTO> actual = this.roomService.findRooms();
        List<RoomDTO> expected = roomList.stream()
                .map(room -> this.mapper.map(room, RoomDTO.class))
                .collect(Collectors.toList());

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void findMovies_ShouldReturnFilteredRoomList_WhenGivenMovieUuid() {
        RoomDB room1 = new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS);
        RoomDB room2 = new RoomDB(NORMAL_NUMBER+1, NORMAL_ROWS+10, NORMAL_COLS+20);
        MovieDB movie = this.movieRepository.save(new MovieDB("A NORMAL TITLE"));

        room1.setMovie(movie);
        this.roomRepository.saveAll(Arrays.asList(room1, room2));
        this.entityManager.refresh(movie);

        List<RoomDTO> actual = this.roomService.findRooms(movie.getUuid());
        List<RoomDTO> expected = Collections.singletonList(this.mapper.map(room1, RoomDTO.class));

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void createRoom_ShouldReturnPersistedRoom_WhenGivenRoomProperties() {
        RoomDTO expected = this.roomService.createRoom(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS);
        Optional<RoomDTO> actual = this.roomRepository.findByUuid(expected.getUuid())
                .map(room -> this.mapper.map(room, RoomDTO.class));

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
    public void updateRoomNumber_ShouldReturnModifiedRoom_WhenGivenUuidAndRoomNumber() {
        UUID uuid = this.roomRepository.save(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS)).getUuid();

        Optional<RoomDTO> expected = this.roomService.updateRoomNumber(uuid, NORMAL_NUMBER+1);
        Optional<RoomDTO> actual = this.roomRepository.findByUuid(uuid)
                .map(room -> this.mapper.map(room, RoomDTO.class));

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
    public void updateRoomCapacity_ShouldReturnModifiedRoom_WhenGivenUuidAndRoomCapacity() {
        UUID uuid = this.roomRepository.save(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS)).getUuid();

        Optional<RoomDTO> expected = this.roomService.updateRoomCapacity(uuid, NORMAL_ROWS+10, NORMAL_COLS+20);
        Optional<RoomDTO> actual = this.roomRepository.findByUuid(uuid)
                .map(room -> this.mapper.map(room, RoomDTO.class));

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
    public void updateRoomMovie_ShouldReturnModifiedRoom_WhenGivenUuidAndMovieUuid() {
        RoomDB room = this.roomRepository.save(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS));
        MovieDB movie = this.movieRepository.save(new MovieDB("A NORMAL TITLE"));

        room.setMovie(movie);
        RoomDTO expected = this.mapper.map(room, RoomDTO.class);
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
    public void removeRoom_ShouldRemoveRoom_WhenGivenUuid() {
        RoomDB room = this.roomRepository.save(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS));

        this.roomService.removeMovie(room.getUuid());
        Optional<RoomDB> actual = this.roomRepository.findByUuid(room.getUuid());

        assertThat(actual.isPresent()).isFalse();
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
}
