package com.agenson.cinema.ticket;

import com.agenson.cinema.room.RoomDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TicketDetailsDTO extends TicketSeatDTO {

    private final UUID uuid;
    private final RoomDTO room;

    public TicketDetailsDTO(TicketDB ticket) {
        super(ticket);
        this.uuid = ticket.getUuid();
        this.room = new RoomDTO(ticket.getRoom());
    }
}
