package com.agenson.cinema.room;

import com.agenson.cinema.movie.MovieDTO;
import com.agenson.cinema.utils.CallableOneArgument;
import com.agenson.cinema.utils.CallableTwoArguments;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
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
    private ModelMapper mapper;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    @BeforeEach
    public void setup() {
        lenient().when(this.mapper.map(any(RoomDB.class), ArgumentMatchers.<Class<RoomDTO>>any()))
                .thenAnswer(invocation -> {
                    RoomDB room = invocation.getArgument(0);

                    return new RoomDTO(
                            room.getUuid(),
                            room.getNumber(),
                            room.getNbRows(),
                            room.getNbCols(),
                            room.getMovie() != null
                                    ? new MovieDTO(room.getMovie().getUuid(), room.getMovie().getTitle())
                                    : null);
                });
    }

    @Test
    public void findRoom_ShouldReturnRoom_WhenGivenUuidOrRoomNumber() {
        RoomDB room = new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS);

        when(this.roomRepository.findByUuid(room.getUuid())).thenReturn(Optional.of(room));
        when(this.roomRepository.findByNumber(room.getNumber())).thenReturn(Optional.of(room));

        RoomDTO expected = this.mapper.map(room, RoomDTO.class);
        Optional<RoomDTO> actual = this.roomService.findRoom(room.getUuid());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);

        actual = this.roomService.findRoom(room.getNumber());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void findRoom_ShouldReturnNull_WhenGivenUnknownUuidOrRoomNumber() {
        when(this.roomRepository.findByUuid(any(UUID.class))).thenReturn(Optional.empty());
        when(this.roomRepository.findByNumber(anyInt())).thenReturn(Optional.empty());

        assertThat(this.roomService.findRoom(UUID.randomUUID()).isPresent()).isFalse();
        assertThat(this.roomService.findRoom(null).isPresent()).isFalse();

        assertThat(this.roomService.findRoom(UNKNOWN_NUMBER).isPresent()).isFalse();
    }

    @Test
    public void findMovies_ShouldReturnRoomList() {
        List<RoomDB> roomList = Arrays.asList(
                new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS),
                new RoomDB(NORMAL_NUMBER+1, NORMAL_ROWS+10, NORMAL_COLS+20)
        );

        when(this.roomRepository.findAll()).thenReturn(roomList);

        List<RoomDTO> actual = this.roomService.findRooms();
        List<RoomDTO> expected = roomList.stream()
                .map(room -> this.mapper.map(room, RoomDTO.class))
                .collect(Collectors.toList());

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
    public void createRoom_ShouldThrowAssociatedInvalidRoomException_WhenGivenInvalidRoomProperties() {
        this.assertThrowsInvalidRoomException_WhenGivenInvalidRoomNumber(number -> {
            this.roomService.createRoom(number, NORMAL_ROWS, NORMAL_COLS);
        });

        when(this.roomRepository.findByNumber(anyInt())).thenReturn(Optional.empty());
        this.assertThrowsInvalidRoomException_WhenGivenInvalidRoomCapacity((nbRows, nbCols) -> {
            this.roomService.createRoom(NORMAL_NUMBER, nbRows, nbCols);
        });
    }

    @Test
    public void updateRoomNumber_ShouldReturnModifiedRoom_WhenGivenUuidAndRoomNumber() {
        RoomDB room = new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS);

        when(this.roomRepository.save(any(RoomDB.class))).then(returnsFirstArg());
        when(this.roomRepository.findByUuid(room.getUuid())).thenReturn(Optional.of(room));
        when(this.roomRepository.findByNumber(anyInt())).thenReturn(Optional.empty());

        RoomDTO expected = this.mapper.map(room, RoomDTO.class);
        expected.setNumber(expected.getNumber()+1);

        Optional<RoomDTO> actual = this.roomService.updateRoomNumber(room.getUuid(), expected.getNumber());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void updateRoomNumber_ShouldThrowAssociatedInvalidRoomException_WhenGivenInvalidProperties() {
        when(this.roomRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.of(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS)));

        this.assertThrowsInvalidRoomException_WhenGivenInvalidRoomNumber(number -> {
            this.roomService.updateRoomNumber(UUID.randomUUID(), number);
        });
    }

    @Test
    public void updateRoomCapacity_ShouldReturnModifiedRoom_WhenGivenUuidAndRoomCapacity() {
        RoomDB room = new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS);

        when(this.roomRepository.save(any(RoomDB.class))).then(returnsFirstArg());
        when(this.roomRepository.findByUuid(room.getUuid())).thenReturn(Optional.of(room));

        RoomDTO expected = this.mapper.map(room, RoomDTO.class);
        expected.setNbRows(expected.getNbRows()+10);
        expected.setNbCols(expected.getNbCols()+20);

        Optional<RoomDTO> actual = this.roomService
                .updateRoomCapacity(room.getUuid(), expected.getNbRows(), expected.getNbCols());

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void updateRoomCapacity_ShouldThrowAssociatedInvalidRoomException_WhenGivenInvalidProperties() {
        when(this.roomRepository.findByUuid(any(UUID.class)))
                .thenReturn(Optional.of(new RoomDB(NORMAL_NUMBER, NORMAL_ROWS, NORMAL_COLS)));

        this.assertThrowsInvalidRoomException_WhenGivenInvalidRoomCapacity((nbRows, nbCols) -> {
            this.roomService.updateRoomCapacity(UUID.randomUUID(), nbRows, nbCols);
        });
    }

    private void assertThrowsInvalidRoomException_WhenGivenInvalidRoomNumber(CallableOneArgument<Integer> callable) {
        when(this.roomRepository.findByNumber(anyInt())).thenAnswer(invocation -> {
            RoomDB room = new RoomDB(invocation.getArgument(0), NORMAL_ROWS, NORMAL_COLS);
            return Optional.of(room);
        });

        for(Map.Entry<Integer, InvalidRoomException.Type> pair : INVALID_ROOM_NUMBERS.entrySet())
            assertThatExceptionOfType(InvalidRoomException.class)
                    .isThrownBy(() -> callable.call(pair.getKey()))
                    .withMessage(pair.getValue().toString());
    }

    private void assertThrowsInvalidRoomException_WhenGivenInvalidRoomCapacity(
            CallableTwoArguments<Integer, Integer> callable
    ) {
        for(Map.Entry<Map.Entry<Integer, Integer>, InvalidRoomException.Type> pair : INVALID_ROOM_CAPACITIES.entrySet())
            assertThatExceptionOfType(InvalidRoomException.class)
                    .isThrownBy(() -> {
                        int nbRows = pair.getKey().getKey();
                        int nbCols = pair.getKey().getValue();

                        callable.call(nbRows, nbCols);
                    })
                    .withMessage(pair.getValue().toString());
    }
}
