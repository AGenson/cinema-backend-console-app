package com.agenson.cinema.ticket;

import com.agenson.cinema.ticket.seat.Seat;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class TicketSeatDTO {

    private final Seat seat;

    public TicketSeatDTO(TicketDB ticket) {
        this.seat = ticket.getSeat();
    }
}
