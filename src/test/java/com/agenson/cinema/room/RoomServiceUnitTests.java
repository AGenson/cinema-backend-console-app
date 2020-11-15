package com.agenson.cinema.room;

import com.agenson.cinema.movie.MovieDB;
import com.agenson.cinema.movie.MovieDTO;
import com.agenson.cinema.movie.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoomServiceUnitTests implements RoomConstants {

    private static final HashMap<Map.Entry<Integer, Integer>, InvalidRoomException.Type> INVALID_ROOM_CAPACITIES =
            new HashMap<Map.Entry<Integer, Integer>, InvalidRoomException.Type>() {{
                put(new SimpleEntry<>(NEGATIVE_ROWS, NORMAL_COLS), InvalidRoomException.Type.NB_ROWS);
                put(new SimpleEntry<>(ZERO_ROWS, NORMAL_COLS), InvalidRoomException.Type.NB_ROWS);
                put(new SimpleEntry<>(NORMAL_ROWS, NEGATIVE_COLS), InvalidRoomException.Type.NB_COLS);
                put(new SimpleEntry<>(NORMAL_ROWS, ZERO_COLS), InvalidRoomException.Type.NB_COLS);
            }};

    private static final HashMap<Integer, InvalidRoomException.Type> INVALID_ROOM_NUMBERS =
            new HashMap<Integer, InvalidRoomException.Type>() {{
                put(NEGATIVE_NUMBER, InvalidRoomException.Type.NUMBER);
                put(ZERO_NUMBER, InvalidRoomException.Type.NUMBER);
                put(NORMAL_NUMBER, InvalidRoomException.Type.EXISTS);
            }};

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    public void findRoom_ShouldReturnRoom_WhenGivenUuid() {
        RoomDB room = new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS);

        when(this.roomRepository.findByUuid(room.getUuid())).thenReturn(Optional.of(room));

        RoomDTO expected = new RoomDTO(room);
        Optional<RoomDTO> actual = this.roomService.findRoom(room.getUuid());

        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findRoom_ShouldReturnNull_WhenGivenUnknownUuid() {
        when(this.roomRepository.findByUuid(any(UUID.class))).thenReturn(Optional.empty());

        assertThat(this.roomService.findRoom(UUID.randomUUID())).isEmpty();
        assertThat(this.roomService.findRoom(null)).isEmpty();
    }

    @Test
    public void findMovies_ShouldReturnRoomList() {
        List<RoomDB> roomList = Arrays.asList(
                new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS),
                new RoomDB(NORMAL_NUMBER+1, NORMAL_ROWS+10, NORMAL_COLS+20)
        );

        when(this.roomRepository.findAll()).thenReturn(roomList);

        List<RoomDTO> actual = this.roomService.findRooms();
        List<RoomDTO> expected = roomList.stream().map(RoomDTO::new).collect(Collectors.toList());

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).containsOnlyOnceElementsOf(expected);
    }

    @Test
    public void createRoom_ShouldReturnNewRoom_WhenGivenRoomProperties() {
        when(this.roomRepository.save(any(RoomDB.class))).then(returnsFirstArg());
        when(this.roomRepository.findByNumber(anyInt())).thenReturn(Optional.empty());

        RoomDTO actual = this.roomService.createRoom(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS);

        assertThat(actual.getUuid()).isNotNull();
        assertThat(actual.getNumber()).isEqualTo(NORMAL_NUMBER);
        assertThat(actual.getNbRows()).isEqualTo(NORMAL_ROWS);
        assertThat(actual.getNbCols()).isEqualTo(NORMAL_COLS);
    }

    @Test
    public void createRoom_ShouldThrowAssociatedInvalidRoomException_WhenGivenInvalidRoomNumber() {
        when(this.roomRepository.findByNumber(anyInt())).thenAnswer(invocation -> {
            RoomDB roomWithSameNumber = new RoomDB(invocation.getArgument(0), NORMAL_ROWS, NORMAL_COLS);

            return Optional.of(roomWithSameNumber);
        });

        for(Map.Entry<Integer, InvalidRoomException.Type> pair : INVALID_ROOM_NUMBERS.entrySet())
            assertThatExceptionOfType(InvalidRoomException.class)
                    .isThrownBy(() -> this.roomService.createRoom(pair.getKey(), NORMAL_ROWS, NORMAL_COLS))
                    .withMessage(pair.getValue().toString());
    }

    @Test
    public void createRoom_ShouldThrowAssociatedInvalidRoomException_WhenGivenInvalidRoomCapacity() {
        when(this.roomRepository.findByNumber(anyInt())).thenReturn(Optional.empty());

        for(Map.Entry<Map.Entry<Integer, Integer>, InvalidRoomException.Type> pair : INVALID_ROOM_CAPACITIES.entrySet())
            assertThatExceptionOfType(InvalidRoomException.class)
                    .isThrownBy(() -> {
                        int nbRows = pair.getKey().getKey();
                        int nbCols = pair.getKey().getValue();

                        this.roomService.createRoom(NORMAL_NUMBER, nbRows, nbCols);
                    })
                    .withMessage(pair.getValue().toString());
    }

    @Test
    public void updateRoomMovie_ShouldReturnModifiedRoom_WhenGivenUuidAndMovieUuid() {
        RoomDB room = new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS);
        MovieDB movie = new MovieDB("A NORMAL TITLE");

        when(this.roomRepository.save(any(RoomDB.class))).then(returnsFirstArg());
        when(this.roomRepository.findByUuid(room.getUuid())).thenReturn(Optional.of(room));
        when(this.movieRepository.findByUuid(movie.getUuid())).thenReturn(Optional.of(movie));

        Optional<RoomDTO> actual = this.roomService.updateRoomMovie(room.getUuid(), movie.getUuid());
        MovieDTO expected = new MovieDTO(movie);

        assertThat(actual).isNotEmpty();
        assertThat(actual.get().getMovie()).isEqualTo(expected);
    }

    @Test
    public void updateRoomMovie_ShouldThrowAssociatedInvalidRoomException_WhenGivenInvalidMovieUuid() {
        RoomDB room = new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS);

        when(this.roomRepository.findByUuid(room.getUuid())).thenReturn(Optional.of(room));
        when(this.movieRepository.findByUuid(any(UUID.class))).thenReturn(Optional.empty());

        assertThatExceptionOfType(InvalidRoomException.class)
                .isThrownBy(() -> this.roomService.updateRoomMovie(room.getUuid(), UUID.randomUUID()))
                .withMessage(InvalidRoomException.Type.MOVIE.toString());
    }
}
