package com.agenson.cinema.room;

import com.agenson.cinema.movie.MovieDB;
import com.agenson.cinema.movie.MovieRepository;
import com.agenson.cinema.order.OrderRepository;
import com.agenson.cinema.security.restriction.RestrictToStaff;
import com.agenson.cinema.ticket.TicketDB;
import com.agenson.cinema.ticket.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final TicketRepository ticketRepository;

    private final OrderRepository orderRepository;

    public Optional<RoomDTO> findRoom(UUID uuid) {
        return this.roomRepository.findByUuid(uuid).map(RoomDTO::new);
    }

    public Optional<RoomDTO> findRoom(int number) {
        return this.roomRepository.findByNumber(number).map(RoomDTO::new);
    }

    public List<RoomDTO> findRooms() {
        return this.roomRepository.findAll().stream().map(RoomDTO::new).collect(Collectors.toList());
    }

    public List<RoomDTO> findRooms(UUID movieUuid) {
        return this.movieRepository.findByUuid(movieUuid).map(MovieDB::getRooms)
                .map(rooms -> rooms.stream().map(RoomDTO::new).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @RestrictToStaff
    public RoomDTO createRoom(int number, int nbRows, int nbCols) {
        this.validateNumber(null, number);
        this.validateCapacity(nbRows, nbCols);

        return new RoomDTO(this.roomRepository.save(new RoomDB(number, nbRows, nbCols)));
    }

    @RestrictToStaff
    public Optional<RoomDTO> updateRoomNumber(UUID uuid, int number) {
        return this.roomRepository.findByUuid(uuid).map(movie -> {
            this.validateNumber(movie.getUuid(), number);
            movie.setNumber(number);

            return new RoomDTO(this.roomRepository.save(movie));
        });
    }

    @RestrictToStaff
    public Optional<RoomDTO> updateRoomCapacity(UUID uuid, int nbRows, int nbCols) {
        return this.roomRepository.findByUuid(uuid).map(movie -> {
            this.validateCapacity(nbRows, nbCols);
            movie.setNbRows(nbRows);
            movie.setNbCols(nbCols);

            return new RoomDTO(this.roomRepository.save(movie));
        });
    }

    @RestrictToStaff
    @Transactional
    public Optional<RoomDTO> updateRoomMovie(UUID uuid, UUID movieUuid) {
        return this.roomRepository.findByUuid(uuid).map(room -> {
            Optional<MovieDB> movie = this.movieRepository.findByUuid(movieUuid);

            if (movieUuid == null || movie.isPresent()) {
                room.setMovie((movieUuid == null) ? null : movie.get());
                room.getTickets().stream()
                        .peek(ticket -> this.ticketRepository.deleteByUuid(ticket.getUuid()))
                        .map(TicketDB::getOrder).distinct().forEach(order -> {
                            this.orderRepository.deleteByUuid(order.getUuid());
                        });
                room.setTickets(Collections.emptyList());

                return new RoomDTO(this.roomRepository.save(room));
            } else throw new InvalidRoomException(InvalidRoomException.Type.MOVIE);
        });
    }

    @RestrictToStaff
    public void removeRoom(UUID uuid) {
        this.roomRepository.deleteByUuid(uuid);
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
