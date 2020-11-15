package com.agenson.cinema.room;

import com.agenson.cinema.movie.MovieDTO;
import com.agenson.cinema.ticket.TicketSeatDTO;
import com.agenson.cinema.ticket.seat.Seat;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
public class RoomDTO {

    private final UUID uuid;
    private final int number;
    private final int nbRows;
    private final int nbCols;
    private final MovieDTO movie;
    private final List<TicketSeatDTO> tickets;

    public RoomDTO(RoomDB room) {
        this.uuid = room.getUuid();
        this.number = room.getNumber();
        this.nbRows = room.getNbRows();
        this.nbCols = room.getNbCols();
        this.movie = (room.getMovie() != null) ? new MovieDTO(room.getMovie()) : null;
        this.tickets = room.getTickets().stream()
                .map(TicketSeatDTO::new)
                .collect(Collectors.toList());
    }

    public int getCapacity() {
        return this.nbRows * this.nbCols;
    }

    public int getPotentialIncome() {
        int capacity = this.getCapacity();

        return (capacity > 50) ? ((capacity / 2) * (10 + 12)) : (capacity * 10);
    }

    public int getIncome() {
        return tickets.stream()
                .map(ticket -> this.getPrice(ticket.getSeat()))
                .reduce(0, Integer::sum);
    }

    public int getPrice(Seat seat) {
        if (this.getCapacity() > 50)
            if (seat.getRow() <= this.nbRows / 2)
                return 12;

        return 10;
    }
}
