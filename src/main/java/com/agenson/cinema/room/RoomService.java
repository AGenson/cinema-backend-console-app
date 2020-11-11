package com.agenson.cinema.room;

import com.agenson.cinema.movie.MovieDB;
import com.agenson.cinema.movie.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    private final MovieRepository movieRepository;

    private final ModelMapper mapper;

    public Optional<RoomDTO> findRoom(UUID uuid) {
        return this.roomRepository.findByUuid(uuid).map(this::toDTO);
    }

    public Optional<RoomDTO> findRoom(int number) {
        return this.roomRepository.findByNumber(number).map(this::toDTO);
    }

    public List<RoomDTO> findRooms() {
        return this.roomRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<RoomDTO> findRooms(UUID movieUuid) {
        return this.movieRepository.findByUuid(movieUuid).map(MovieDB::getRooms)
                .map(rooms -> rooms.stream().map(this::toDTO).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public RoomDTO createRoom(int number, int nbRows, int nbCols) {
        this.validateNumber(null, number);
        this.validateCapacity(nbRows, nbCols);

        return this.toDTO(this.roomRepository.save(new RoomDB(number, nbRows, nbCols)));
    }

    public Optional<RoomDTO> updateRoomNumber(UUID uuid, int number) {
        return this.roomRepository.findByUuid(uuid).map(movie -> {
            this.validateNumber(movie.getUuid(), number);
            movie.setNumber(number);

            return this.toDTO(this.roomRepository.save(movie));
        });
    }

    public Optional<RoomDTO> updateRoomCapacity(UUID uuid, int nbRows, int nbCols) {
        return this.roomRepository.findByUuid(uuid).map(movie -> {
            this.validateCapacity(nbRows, nbCols);
            movie.setNbRows(nbRows);
            movie.setNbCols(nbCols);

            return this.toDTO(this.roomRepository.save(movie));
        });
    }

    public Optional<RoomDTO> updateRoomMovie(UUID uuid, UUID movieUuid) {
        return this.roomRepository.findByUuid(uuid).map(room -> {
            Optional<MovieDB> movie = this.movieRepository.findByUuid(movieUuid);

            if (movie.isPresent()) {
                room.setMovie(movie.get());

                return this.toDTO(this.roomRepository.save(room));
            } else throw new InvalidRoomException(InvalidRoomException.Type.MOVIE);
        });
    }

    public void removeMovie(UUID uuid) {
        this.roomRepository.deleteByUuid(uuid);
    }

    private RoomDTO toDTO(RoomDB room) {
        return this.mapper.map(room, RoomDTO.class);
    }

    private void validateNumber(UUID uuid, int number) {
        if (number < 1) throw new InvalidRoomException(InvalidRoomException.Type.NUMBER);
        else {
            this.roomRepository.findByNumber(number).ifPresent(roomWithSameNumber -> {
                if (uuid == null || roomWithSameNumber.getUuid() != uuid)
                    throw new InvalidRoomException(InvalidRoomException.Type.EXISTS);
            });
        }
    }

    private void validateCapacity(int nbRows, int nbCols) {
        if (nbRows < 1) throw new InvalidRoomException(InvalidRoomException.Type.NB_ROWS);
        else if (nbCols < 1) throw new InvalidRoomException(InvalidRoomException.Type.NB_COLS);
    }
}